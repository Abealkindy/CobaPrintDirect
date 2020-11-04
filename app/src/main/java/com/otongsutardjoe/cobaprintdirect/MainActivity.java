package com.otongsutardjoe.cobaprintdirect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.otongsutardjoe.cobaprintdirect.databinding.ActivityMainBinding;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CustomPrinterService.PrintServiceListener, CustomPrinterServiceBack.PrintServiceListener {
    ActivityMainBinding mainBinding;

    public static final String PREFIX = "stream2file";
    public static final String SUFFIX = ".pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        PDFBoxResourceLoader.init(MainActivity.this);
        initEvent();
    }

    private void initEvent() {
        mainBinding.buttonPrintWithSocket.setOnClickListener(v -> fixedPDFDirectPrint());
        mainBinding.buttonPrintWithPrintHelper.setOnClickListener(v -> {
        });
    }

    private void fixedPDFDirectPrint() {
        if (!mainBinding.editTextIp.getText().toString().isEmpty()) {
            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
            try {
                CustomPrinterServiceBack customPrinterService = new CustomPrinterServiceBack(
                        mainBinding.editTextIp.getText().toString(),
                        9100,
                        stream2file(getAssets().open("cobaprint.pdf")),
                        "stream2file.pdf",
                        CustomPrinterServiceBack.PaperSize.A4,
                        1
                );
                customPrinterService.setPrintServiceListener(MainActivity.this);
                customPrinterService.execute();
            } catch (Exception e) {
                Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Masih Kosong ey", Toast.LENGTH_SHORT).show();
        }
    }

    public static File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

    @Override
    public void onPrintCompleted() {
        Log.e("result", "Finished!");
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Result : Finished!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onNetworkError(String message) {
        Log.e("result", "Error : " + message);
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Result : Error : " + message, Toast.LENGTH_SHORT).show());
    }

    public static InputStream downloadFileThrowing(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Download not successful.response:" + response);
        } else {
            return response.body().byteStream();
        }
    }
}