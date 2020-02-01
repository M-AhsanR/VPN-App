package com.watchmyapps.freevpn.ui.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.daimajia.numberprogressbar.NumberProgressBar;

import com.watchmyapps.freevpn.R;
import com.watchmyapps.freevpn.localdata.model.Server;
import com.watchmyapps.freevpn.utils.PropertiesService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoadingActivity extends BaseActivity {

    private NumberProgressBar progressBar;
    private TextView commentsText;

    private Handler updateHandler;

    private final int LOAD_ERROR = 0;
    private final int DOWNLOAD_PROGRESS = 1;
    private final int PARSE_PROGRESS = 2;
    private final int LOADING_SUCCESS = 3;
    private final int SWITCH_TO_RESULT = 4;
    private final String BASE_URL = "http://www.vpngate.net/api/iphone/";
    private final String BASE_FILE_NAME = "vpngate.csv";
    private int percentDownload = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.number_progress_bar);
        commentsText = findViewById(R.id.commentsText);

        progressBar.setMax(100);

        updateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.arg1) {
                    case LOAD_ERROR: {
                        commentsText.setText(msg.arg2);
                        progressBar.setProgress(100);
                    } break;
                    case DOWNLOAD_PROGRESS: {
                        commentsText.setText(R.string.downloading_csv_text);
                        progressBar.setProgress(msg.arg2);

                    } break;
                    case PARSE_PROGRESS: {
                        commentsText.setText(R.string.parsing_csv_text);
                        progressBar.setProgress(msg.arg2);
                    } break;
                    case LOADING_SUCCESS: {
                        commentsText.setText(R.string.successfully_loaded);
                        progressBar.setProgress(100);
                        Message end = new Message();
                        end.arg1 = SWITCH_TO_RESULT;
                        updateHandler.sendMessageDelayed(end,500);
                    } break;
                    case SWITCH_TO_RESULT: {
                        if (PropertiesService.getConnectOnStart()) {
                            Server randomServer = getRandomServer();
                            if (randomServer != null) {
                                newConnecting(randomServer, true, true);
                            } else {
                                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                            }
                        } else {
                            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                        }
                    }
                }
                return true;
            }
        });
        progressBar.setProgress(0);


    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadCSVFile(BASE_URL, BASE_FILE_NAME);
    }
    @Override
    protected boolean useMenu() {
        return false;
    }

    private void downloadCSVFile(String url, String fileName) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.download(url, getCacheDir().getPath(), fileName)
                .setTag("downloadCSV")
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        if(totalBytes <= 0) {
                            totalBytes = 1000000;
                        }
                            percentDownload = (int)((100 * bytesDownloaded) / totalBytes);

                        Message msg = new Message();
                        msg.arg1 = DOWNLOAD_PROGRESS;
                        msg.arg2 = percentDownload;
                        updateHandler.sendMessage(msg);
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {

                            parseCSVFile(BASE_FILE_NAME);
                    }
                    @Override
                    public void onError(ANError error) {
                        Message msg = new Message();
                        msg.arg1 = LOAD_ERROR;
                        msg.arg2 = R.string.network_error;
                        updateHandler.sendMessage(msg);
                    }
                });
    }

    private void parseCSVFile(String fileName) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(getCacheDir().getPath().concat("/").concat(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.arg1 = LOAD_ERROR;
            msg.arg2 = R.string.csv_file_error;
            updateHandler.sendMessage(msg);
        }
        if (reader != null) {
            try {
                int startLine = 2;
                int type = 0;
                    dataBaseHelper.clearTable();

                int counter = 0;
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (counter >= startLine) {
                        dataBaseHelper.putLine(line, type);
                    }
                    counter++;
                        Message msg = new Message();
                        msg.arg1 = PARSE_PROGRESS;
                        msg.arg2 = counter;
                        updateHandler.sendMessage(msg);
                }
                    Message end = new Message();
                    end.arg1 = LOADING_SUCCESS;
                    updateHandler.sendMessageDelayed(end,200);

            } catch (Exception e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.arg1 = LOAD_ERROR;
                msg.arg2 = R.string.csv_file_error_parsing;
                updateHandler.sendMessage(msg);
            }
        }
    }
}
