package com.ifma.cmpt.demo;

public class NativeCall {
    static {
        try {
            System.loadLibrary("fireyer-jni");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public native Object callNative(int i, Object args);
}
