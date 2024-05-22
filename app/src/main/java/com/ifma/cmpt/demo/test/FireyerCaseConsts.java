package com.ifma.cmpt.demo.test;

public final class FireyerCaseConsts {
    public static final String PACKAGE_NAME = "com.ifma.cmpt.demo.fireyer";

    public static final int MODE_SOURCE = 1;// origin install env
    public static final int MODE_KONKER = 2;// konker env
    public static final int MODE_PACKER = 3;// pack env

    public static final String KEY_MODE = "key-mode";

    private static int sMode = MODE_SOURCE;
    public static void setMode(int mode) { sMode = mode; }
    public static int getMode() { return sMode; }
    public static boolean isSourceMode() { return sMode == MODE_SOURCE; }
    public static boolean isKonkerMode() { return sMode == MODE_KONKER; }
    public static boolean isPackerMode() { return sMode == MODE_PACKER; }

    public static boolean isVirtualMode() { return sMode == MODE_KONKER || sMode == MODE_PACKER; }

    public static final String KEY_BINDER_ARG_A = PACKAGE_NAME;
    public static final String KEY_BINDER_ARG_B = PACKAGE_NAME;
    public static final String KEY_BINDER_ARG_C = PACKAGE_NAME;
    public static final String KEY_BINDER_ARG_D = PACKAGE_NAME;

    public static final String KEY_PRE = "mts.konker.l2.";
}
