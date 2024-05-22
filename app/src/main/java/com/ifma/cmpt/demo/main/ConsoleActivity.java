package com.ifma.cmpt.demo.main;

import android.app.Activity;

import com.ifma.cmpt.demo.module.ClipboadData;
import com.ifma.cmpt.demo.test.FireyerCaseConsts;
import com.ifma.cmpt.testin.env.TstConsts;
import com.ifma.cmpt.testin.env.TstHandler;
import com.ifma.cmpt.testin.module.TstRunner;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ifma.cmpt.demo.fireyer.R;

public class ConsoleActivity extends Activity {
    private TextView mTextConsole;
    private ScrollView mScrollView;
    protected BroadcastReceiver mBR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        initView();
        initData();
        initTest();
    }

    protected void initView() {
        mTextConsole = (TextView) findViewById(R.id.console);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mTextConsole.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    protected void initData() {
        addMessage("Console started ...");
        if (FireyerCaseConsts.isSourceMode()) {
            addMessage("From source env");
        } else if (FireyerCaseConsts.isKonkerMode()) {
            addMessage("From konker env");
        } else if (FireyerCaseConsts.isPackerMode()) {
            addMessage("From packer env");
        }
    }

    protected void initTest() {
        final Bundle args = getIntent().getExtras();
        if (!TextUtils.equals(TstConsts.VALUE_UNITTEST, args.getString(TstConsts.KEY_TYPE))) return;

        TstRunner.runTest(args, new TstRunner.ITstPrinter() {
            @Override
            public void onTstStart() {
                if (FireyerCaseConsts.isVirtualMode()) {
                    ClipboadData.loadFromClipboard(getApplicationContext());
                }
            }

            @Override
            public void onTstDone() {
                if (FireyerCaseConsts.isSourceMode()) {
                    ClipboadData.saveToClipboard(getApplicationContext());
                }
                addMessage(TEXT_PRE_INFO + " passed " + TstRunner.getPassedCaseCount() + " cases");
                addMessage(TEXT_PRE_INFO  + " failed " + TstRunner.getFailedCaseCount() + " cases");
            }

            @Override
            public void onTstPrint(String s) {
                addMessage(s);
            }
        });
    }

    protected void addMessage(final String msg) {
        if (TstHandler.isMainThread()) {
            doPrint(msg);
        } else {
            TstHandler.postUi(new Runnable() {
                @Override
                public void run() {
                    doPrint(msg);
                }
            });
        }
    }

    public static final String TEXT_PRE_OK = "[O";
    public static final String TEXT_PRE_FAIL = "[F";
    public static final String TEXT_PRE_ERROR = "[E";
    public static final String TEXT_PRE_WARN = "[W";
    public static final String TEXT_PRE_INFO = "[I";

    private void doPrint(String msg) {
        if (TextUtils.isEmpty(msg)) {
            mTextConsole.append("\n");
            return;
        }
        try {
            SpannableStringBuilder builder = new SpannableStringBuilder(msg);
            if (msg.startsWith(TEXT_PRE_OK)) {
                builder.setSpan(new ForegroundColorSpan(0xAA00FF00), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (msg.startsWith(TEXT_PRE_FAIL) || msg.startsWith(TEXT_PRE_ERROR)) {
                builder.setSpan(new ForegroundColorSpan(0xAAFF0000), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (msg.startsWith(TEXT_PRE_WARN)) {
                builder.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (msg.startsWith(TEXT_PRE_INFO)) {
                builder.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.setSpan(new ForegroundColorSpan(0xAA000000), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mTextConsole.append(builder);
            mTextConsole.append("\n");
            int offset = mTextConsole.getMeasuredHeight() - mScrollView.getMeasuredHeight();
            if (offset < 0) {
                mScrollView.scrollTo(0, 0);
            } else {
                mScrollView.scrollTo(0, offset);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        if (null != mBR) {
            try {
                unregisterReceiver(mBR);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                mBR = null;
            }
        }
        TstRunner.cancel();
        super.onDestroy();
    }
}
