package com.ifma.cmpt.demo;

import android.media.CreateRecordResponse;
import android.media.CreateRecordRequest;

interface IBinderTest {
    int callBinderTest1(String a, IBinder c, int d);
    int callBinderTest2(int a, IBinder b, String c);
    int callBinderTest(String a, String b, IBinder c, int d, String e, IBinder f, String g);
    CreateRecordResponse createRecord(in CreateRecordRequest request);
}
