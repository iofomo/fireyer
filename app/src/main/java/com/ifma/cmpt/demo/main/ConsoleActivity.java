package com.ifma.cmpt.demo.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ifma.cmpt.demo.fireyer.R;
import com.ifma.cmpt.demo.module.ClipboadData;
import com.ifma.cmpt.demo.test.FireyerCaseConsts;
import com.ifma.cmpt.demo.test.FireyerPackageCase;
import com.ifma.cmpt.demo.test.FireyerRuntimeCase;
import com.ifma.cmpt.testin.env.TstConsts;
import com.ifma.cmpt.testin.env.TstHandler;
import com.ifma.cmpt.testin.module.TstRunner;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsoleActivity extends Activity {
    private TextView mTextConsole;
    private ScrollView mScrollView;
    protected BroadcastReceiver mBR;
    private Handler mHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        initView();
        initData();

        if (FireyerCaseConsts.isVirtualMode()) {
            showInputDialog();// got info from clipboard
        } else {
            initTest();
        }
    }

    protected void initView() {
        mHandler = new Handler();
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
                    int cnt = ClipboadData.loadFromClipboard(getApplicationContext());
                    addMessage("Load info from Clipboard: " + cnt);
                } else {
                    FireyerRuntimeCase.dumpThread();
                    FireyerPackageCase.dumpApk(getApplicationContext());
                }
            }

            @Override
            public void onTstDone() {
                if (FireyerCaseConsts.isSourceMode()) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showInputDialog();
                        }
                    }, 1000);
                }
                addMessage("[Info] passed " + TstRunner.getPassedCaseCount() + " cases");
                addMessage("[Info] failed " + TstRunner.getFailedCaseCount() + " cases");
            }

            @Override
            public void onTstPrint(String s) {
                addMessage(s);
            }
        });
    }

    private final List<String> mMsgList = new CopyOnWriteArrayList<>();

    private Runnable mMsgRunner = new Runnable() {
        @Override
        public void run() {
            doPrint();
        }
    };

    protected void addMessage(String msg) {
        mMsgList.add(msg);
        if (TstHandler.isMainThread()) {
            doPrint();
        } else {
            TstHandler.postUi(mMsgRunner);
        }
    }

    public static final String TEXT_PRE_OK = "[O";
    public static final String TEXT_PRE_FAIL = "[F";
    public static final String TEXT_PRE_ERROR = "[E";
    public static final String TEXT_PRE_WARN = "[W";
    public static final String TEXT_PRE_INFO = "[I";

    private synchronized void doPrint() {
        while (!mMsgList.isEmpty()) {
            doPrint(mMsgList.remove(0));
        }
    }

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
                builder.setSpan(new ForegroundColorSpan(Color.WHITE), 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    private void showInputDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View dialogView = inflater.inflate(R.layout.dialog_input, null);
        final EditText editText = dialogView.findViewById(R.id.editText);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Got Focus")
                .setView(dialogView)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                editText.requestFocus();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (FireyerCaseConsts.isVirtualMode()) {
                            ClipboadData.loadFromClipboard(getApplicationContext());
                            initTest();// got info, then start to test
                        } else {
                            ClipboadData.saveToClipboard(getApplicationContext());
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }, 1000);
            }
        });

        dialog.show();
    }
}
