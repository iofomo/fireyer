package com.ifma.cmpt.demo.sub;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;


public class DocumentReceiverActivity extends Activity {
    private static final String TAG = "DocumentReceiverActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_document_receiver);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("application/")) {
                handleSendDocument(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("application/")) {
                handleSendMultipleDocuments(intent);
            }
        }
    }

    void handleSendDocument(Intent intent) {
        Uri documentUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (documentUri != null) {
            int len = readFileContent(documentUri);
            if (0 < len) {
                Toast.makeText(getApplicationContext(), "Got file size: " + len, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(getApplicationContext(), "Receive file fail", Toast.LENGTH_SHORT).show();
    }

    void handleSendMultipleDocuments(Intent intent) {
        ArrayList<Uri> documentUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (documentUris != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Got ").append(documentUris.size()).append(" files");
            for (Uri documentUri : documentUris) {
                int len = readFileContent(documentUri);
                sb.append(", ").append(len);
            }
            Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Receive file fail", Toast.LENGTH_LONG).show();
        }
    }

    int readFileContent(Uri documentUri) {
        ContentResolver contentResolver = getContentResolver();
        try (InputStream inputStream = contentResolver.openInputStream(documentUri)) {
            if (inputStream != null) {
                byte[] buffer = new byte[4096];

                int fileSize = 0;
                int len;
                while((len = inputStream.read(buffer, 0, 4096)) != -1) {
                    if (len > 0) {
                        fileSize += len;
                    }
                }
                return fileSize;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            // 处理读取文件时的其他I/O错误
        }
        return -1;
    }

}
