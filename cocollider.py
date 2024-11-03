#!/usr/bin/env python
# -*- coding:utf-8 -*-
# @Brief:  mk
# @Date:   2024.11.08 16:52:52

import sys, os, re, time
from datetime import datetime

g_env_path = os.getcwd()
g_this_file = os.path.realpath(sys.argv[0])
g_this_path = os.path.dirname(g_this_file)

COCO_TYPE_CLASS     = 10
COCO_TYPE_NATIVE    = 20

COCO_TYPE_CLASS_NAME     = 0
COCO_TYPE_CLASS_METHOD   = 1
COCO_TYPE_CLASS_FIELD    = 2
COCO_TYPE_NATIVE_METHOD  = 3
COCO_TYPE_NATIVE_FIELD   = 4

COCO_KEY = '@CoCollider'
COCO_KEY_LENGTH = len(COCO_KEY)

TARGET_PACKAGE = "com.ifma.cmpt.demo.fireyer"

g_target_file_types = ['.h','.inl','.cpp','.c','.java','.kt','.aidl']
# --------------------------------------------------------------------------------------------------------------------------
# init project env
g_wing_path = os.path.expanduser("~") + os.sep + '.wing/wing' #  such as: /Users/${username}/.wing/wing
sys.path.append(g_wing_path)
from utils.utils_import import ImportUtils
g_space_path = ImportUtils.initSpaceEnv(g_env_path)

from utils.utils_file import FileUtils
from utils.utils_adb import AdbUtils
from utils.utils_logger import LoggerUtils
# --------------------------------------------------------------------------------------------------------------------------
class CocoMember:# fields ande methods

    def __init__(self, _name, _typ):
        self.name = _name
        self.typ = _typ
        self.lines = []

    def addLine(self, _fname, _lineNum):
        self.lines.append(_fname + (',%d' % _lineNum))

    def println(self, writer):
        # + method name
        # > /home/demo.java,124
        # - field name
        # > /home/demo.java,124
        if self.typ == COCO_TYPE_NATIVE_METHOD or self.typ == COCO_TYPE_CLASS_METHOD:
            writer.write('+ ' + self.name + '\n')
        elif self.typ == COCO_TYPE_CLASS_FIELD:
            writer.write('- ' + self.name + '\n')
        elif self.typ == COCO_TYPE_CLASS:
            writer.write('= ' + self.name + '\n')
        elif self.typ == COCO_TYPE_NATIVE:
            writer.write('~ ' + self.name + '\n')
        else:
            assert 0, 'Unknown type: %d' % self.typ
        # self.lines = sorted(self.lines)
        self.lines.sort()
        for line in self.lines:
            writer.write('> ' + line + '\n')
        writer.write('\n')


class CocoClass:
    def __init__(self, _name, _typ):
        self.members = {}
        self.members[' '] = CocoMember(_name, _typ)

    def addClass(self, _fname, _lineNum):# only for class or lib name
        item = self.members.get(' ')
        item.addLine(_fname, _lineNum)

    def addItem(self, _fname, _lineNum, _member, _memberType):
        item = self.members.get(_member)
        if item is None:
            item = CocoMember(_member, _memberType)
            self.members[_member] = item
        item.addLine(_fname, _lineNum)

    def println(self, writer):
        keys = list(self.members.keys())
        keys.sort()
        for key in keys: self.members[key].println(writer)
        writer.write('\n')


class CocoGroup:
    def __init__(self):
        self.classes = {}

    def addClass(self, _fname, _lineNum, _cname, _ctype):
        cls = self.classes.get(_cname)
        if cls is None:
            cls = CocoClass(_cname, _ctype)
            self.classes[_cname] = cls
        cls.addClass(_fname, _lineNum)

    def addItem(self, _fname, _lineNum, _cname, _ctype, _mname, _mtype):
        cls = self.classes.get(_cname)
        if cls is None:
            cls = CocoClass(_cname, _ctype)
            self.classes[_cname] = cls
        cls.addItem(_fname, _lineNum, _mname, _mtype)

    def println(self, writer):
        keys = list(self.classes.keys())
        keys.sort()

        for key in keys:
            print('> ' + key)
            writer.write('############################################################\n')
            self.classes[key].println(writer)
        writer.write('\n')
        print('\n%d targets found.' % len(keys))


def getMatch(index, matches, isClass):
    while index < len(matches):
        item =  matches[index].strip()
        index += 1
        if len(item) <= 0: continue
        if isClass and item.find('.') < 0: continue
        return index, item
    return index, None

def doParseLine(group, line, fname, lineNum):
    # @CoCollider android.utils.Abc,-mFile
    # @CoCollider android.utils.Abc,-mFile,mName
    # @CoCollider android/utils/Abc,-mFile
    # @CoCollider android.utils.Abc,+getFile,-mName
    # @CoCollider android/utils/Abc,+getFile,+getName,-mName
    # Class.forName("android.utils.Abc");// @CoCollider
    # ReflectUtils.getStaticField("android.utils.Abc", "mName");// @CoCollider ,-
    # @CoCollider ~libc.so,+open
    # @CoCollider ~/system/lib/libc.so,+open
    # @CoCollider ~/system/lib/libc.so,+open,+close
    # utils_dlsym("libc.so", "open");// @CoCollider ~,+
    # utils_dlsym("open");// @CoCollider ~libc.so,+
    pos = line.find(COCO_KEY)
    if pos < 0: return
    fmt = line[pos + COCO_KEY_LENGTH:].strip()
    items = fmt.split(',')
    matches = re.findall(r'"(.*?)"', line[:pos])

    i, j = 1, 0
    cname = items[0]
    ctype = COCO_TYPE_CLASS
    if len(cname) <= 0:
        j, cname = getMatch(j, matches, True)
    if len(cname) <= 0: return
    if cname.startswith('~'):
        cname = cname[1:]
        ctype = COCO_TYPE_NATIVE
        if len(cname) <= 0:
            j, cname = getMatch(j, matches, False)
            assert cname is not None, "Invalid member: " + line
    elif cname.startswith('='):
        cname = cname[1:]
        ctype = COCO_TYPE_CLASS
        if len(cname) <= 0:
            j, cname = getMatch(j, matches, True)
            assert cname is not None, "Invalid member: " + line
    if ctype == COCO_TYPE_CLASS:
        cname = cname.replace('/', '.')
        cname = cname.replace('\\', '.')

    l = len(items)
    if l <= i:
        group.addClass(fname, lineNum, cname, ctype)
        return

    gotItem = False
    while i < l:
        item = items[i].strip()
        i += 1
        if len(item) <= 0: continue
        if item.startswith('-'):
            if '-' == item:
                j, item = getMatch(j, matches, False)
                assert item is not None, "Invalid member: " + line
            else:
                item = item[1:].strip()
            gotItem = True
            group.addItem(fname, lineNum, cname, ctype, item, COCO_TYPE_CLASS_FIELD if ctype == COCO_TYPE_CLASS else COCO_TYPE_NATIVE_FIELD)
        elif item.startswith('+'):
            if '+' == item:
                j, item = getMatch(j, matches, False)
                assert item is not None, "Invalid member: " + line
            else:
                item = item[1:].strip()
            gotItem = True
            group.addItem(fname, lineNum, cname, ctype, item, COCO_TYPE_CLASS_METHOD if ctype == COCO_TYPE_CLASS else COCO_TYPE_NATIVE_METHOD)
        else:
            assert 0, 'unknown: ' + line
    if not gotItem: print('Warn: ' + line)


def isTargetFile(f):
    pos = f.rfind('.')
    return 0 < pos and f[pos:] in g_target_file_types


def doScan(path, group):
    print('do scan ...')
    l = len(path)
    minLen = COCO_KEY_LENGTH + 2
    for root, dirs, files in os.walk(path):
        for name in files:
            if not isTargetFile(name): continue
            fileName = os.path.join(root, name)
            fname = fileName[l+1:]
            lineNum = 0
            with open(fileName, 'r') as f:
                while True:
                    line = f.readline()
                    if None == line or len(line) <= 0: break
                    lineNum += 1
                    line = line.strip()
                    if len(line) < minLen: continue
                    doParseLine(group, line, fname, lineNum)


def doPrintln(path, group):
    # cocollider-20241023-112044.txt
    if not os.path.isdir(path): os.makedirs(path)

    now = datetime.now()
    fname = now.strftime('cocollider-scan-%Y%m%d-%H%M%S.txt')
    fileName = path + os.sep + fname
    with open(fileName, 'w') as f:
        group.println(f)
    print('>>> ' + fileName)


def doCall(extras):
    LoggerUtils.println("call provider: " + TARGET_PACKAGE)
    AdbUtils.launchApp(TARGET_PACKAGE)
    ret = AdbUtils.callProvider(TARGET_PACKAGE + ".MainProvider",
                                "coco",
                                "coco",
                                extras
                                )
    LoggerUtils.println(ret)
    return ret


def doRunner(inFile, outPath):
    if not os.path.isfile(inFile):
        LoggerUtils.e("Invalid scan file")
        return
    targetFile = "/data/local/tmp/cocollider-scan.txt"
    runOutFile = "/sdcard/Android/data/" + TARGET_PACKAGE + "/cocollider-run.txt"
    AdbUtils.doAdbCmd('shell rm -f ' + targetFile)
    AdbUtils.doAdbCmd('shell rm -f ' + runOutFile)
    AdbUtils.push(inFile, targetFile)
    extras = {
        "key-in" : targetFile
    }
    ret = doCall(extras)
    LoggerUtils.println(ret)

    LoggerUtils.println('waiting ...')

    now = datetime.now()
    fname = now.strftime('cocollider-run-%Y%m%d-%H%M%S.txt')
    outFile = outPath + "/" + fname

    cnt = 0
    while cnt < 3:
        cnt += 1
        time.sleep(1)
        AdbUtils.pull(runOutFile, outFile)
        if os.path.isfile(outFile):
            LoggerUtils.println('> ' + outFile)
            LoggerUtils.light('success')
            return
    LoggerUtils.w('CoCollider run fail')


def run():
    # ./cocollider.py scan
    # ./cocollider.py scan ${path}
    # ./cocollider.py run ${file}
    if len(sys.argv) < 2:
        print('Invalid args.')
        print('    ./cocollider.py scan')
        print('    ./cocollider.py scan ${path}')
        print('    ./cocollider.py run ${file}')
        return

    cmd = sys.argv[1]
    if cmd == 'scan':
        group = CocoGroup()
        path = sys.argv[2] if 2 < len(sys.argv) else g_env_path
        doScan(path, group)
        doPrintln(g_env_path, group)
        print('done.')
        return
    if cmd == 'run':
        doRunner(sys.argv[2], g_env_path)
    else:
        print('Invalid cmd: ' + cmd)


if __name__ == "__main__":
    run()