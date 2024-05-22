package com.ifma.cmpt.demo.module;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.ifma.cmpt.testin.utils.TstClassPrinter;
import com.ifma.cmpt.utils.FileUtils;
import com.ifma.cmpt.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassDumper {
    private static final String TAG = "ClassDumper";

    public static void handleCallEvent(Context ctx, Bundle inData, Bundle replyData) {
        final String fileOrClass = inData.getString(Consts.KEY_IN_ARG1);
        final String outPath = inData.getString(Consts.KEY_IN_ARG2);
        final String mode = inData.getString(Consts.KEY_IN_ARG3);

        int failCnt;
        if (fileOrClass.startsWith("/") || fileOrClass.startsWith("\\")) {
            List<String> lines = FileUtils.readLines(new File(fileOrClass));
            if (null == lines) {
                replyData.putInt(Consts.KEY_CODE, -1);
                replyData.putString(Consts.KEY_MSG, "Error: file not found " + fileOrClass);
                return;
            }
            if (lines.isEmpty()) {
                replyData.putInt(Consts.KEY_CODE, -2);
                replyData.putString(Consts.KEY_MSG, "Error: empty file");
                return;
            }
            failCnt = doParse(lines, outPath, mode);
        } else {
            failCnt = doParseClass(fileOrClass, outPath, mode) ? 0 : 1;
        }
        if (failCnt == 0) {
            replyData.putInt(Consts.KEY_CODE, 0);
            replyData.putString(Consts.KEY_MSG, "success");
        } else {
            replyData.putInt(Consts.KEY_CODE, failCnt);
            replyData.putString(Consts.KEY_MSG, "Error: " + failCnt + " class dump fail");
        }
    }
    private static final String TARGET_PRE = "target,";
    private static final String ONEWAY_PRE = "oneway,";
    public static int doParse(List<String> lines, String outPath, String mode) {
//        target,android.graphicsenv.IGpuService,1,2,3
//        oneway,com.android.internal.telephony.IPhoneStateListener,10,6
        int failCnt = 0;
        for (String line : lines) {
            if (line.startsWith(TARGET_PRE)) {
                if (!doParseClass(line.substring(TARGET_PRE.length()), outPath, mode)) {
                    failCnt += 1;
                }
            } else if (line.startsWith(ONEWAY_PRE)) {
                if (!doParseClass(line.substring(ONEWAY_PRE.length()), outPath, mode)) {
                    failCnt += 1;
                }
            }
        }
        return failCnt;
    }

    private static boolean doParseClass(String classLine, String outPath, String mode) {
        List<Integer> targetValues = new ArrayList();
        String[] items = classLine.split(",");
        if (!TextUtils.equals(mode, "all")) {
            if (null != items && items.length >= 2) {
                for (int i = 1; i < items.length; ++i) {
                    targetValues.add(Integer.valueOf(items[i]));
                }
            }
        }

        int pos = items[0].lastIndexOf('.');
        String pathName = 0 < pos ? items[0].substring(0, pos) : "";
        String className = 0 < pos ? items[0].substring(pos+1) : items[0];

        OutputStream os = null;
        File file = new File(outPath + File.separator + className + ".java");
        FileUtils.mkParentDirs(file);
        try {
            os = new FileOutputStream(file);
            DumperWriter writer = new DumperWriter(pathName, className, os);
            TstClassPrinter.setCallback(writer);

            writer.writeHeader();
            if (targetValues.isEmpty()) {
                TstClassPrinter.printStub(items[0]);
            } else {
                TstClassPrinter.printStub(items[0], targetValues);
            }
            if (0 < writer.getCount()) {
                writer.writeTailer();
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeQuietly(os);
        }
        file.delete();

        Logger.e(TAG, "dump fail: " + items[0]);
        return false;
    }

    static class DumperWriter implements TstClassPrinter.IClassPrinter {
        private int mCount = 0;
        private final String mPathName, mClassName;
        private final OutputStream mStream;

        public DumperWriter(String pathName, String clsName, OutputStream stream) {
            mPathName = pathName;
            mClassName = clsName;
            mStream = stream;
        }

        public int getCount() { return mCount; }

        public void writeHeader() throws Throwable {
            mStream.write(("package " + mPathName + ";").getBytes());
            mStream.write("\n\n".getBytes());
            mStream.write(("interface " + mClassName + " {").getBytes());
        }

        public void writeTailer() throws Throwable {
            mStream.write("\n}".getBytes());
        }

        @Override
        public void onTstClassPrinter(TstClassPrinter.TRANSACTION_Item item) {
            mCount += 1;
            try {
                mStream.write(("\n    " + item.funcName + ";// " + item.code).getBytes());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
