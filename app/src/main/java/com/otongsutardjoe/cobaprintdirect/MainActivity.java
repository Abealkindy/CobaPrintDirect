package com.otongsutardjoe.cobaprintdirect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.otongsutardjoe.cobaprintdirect.databinding.ActivityMainBinding;
import com.tom_roush.pdfbox.io.IOUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.alexbykov.nopermission.PermissionHelper;

public class MainActivity extends AppCompatActivity implements CustomPrinterService.PrintServiceListener, CustomPrinterServiceBack.PrintServiceListener {
    ActivityMainBinding mainBinding;

    public static final String PREFIX = "stream2file";
    public static final String SUFFIX = ".pdf";
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        permissionHelper = new PermissionHelper(this);
        initEvent();
    }

    private void initEvent() {
        mainBinding.buttonPrintWithSocket.setOnClickListener(v -> fixedPDFDirectPrint());
        mainBinding.buttonPrintWithPrintHelper.setOnClickListener(v -> {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                permissionHelper.check(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .setDialogPositiveButtonColor(android.R.color.holo_orange_dark)
                        .onSuccess(this::onSuccessAskPermission)
                        .onDenied(this::onDeniedPermission)
                        .onNeverAskAgain(this::onNeverAskAgainPermission)
                        .run();

            } else {
                fileToBase64();
            }

        });
    }

    private void fileToBase64() {
        String encodedBase64;
        try {
            File originalFile = stream2file(getAssets().open("cobaprint.pdf"));
            FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
            byte[] bytes = new byte[(int) originalFile.length()];
            fileInputStreamReader.read(bytes);
            encodedBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            Log.e("Hasil Convert ", base64toPDF(encodedBase64));
        } catch (Exception e) {
            Log.e("Convert Error ", e.getMessage());
            e.printStackTrace();
        }
    }

    public String base64toPDF(String base64Text) {
        FileOutputStream fos;
        String path = "";
        try {
            fos = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "test.pdf");
            fos.write(Base64.decode(base64Text, Base64.NO_WRAP));
            fos.close();
            path = fos.getFD().toString();
        } catch (Exception e) {
            Log.e("Error Convert ", e.getMessage());
            e.printStackTrace();
        }
        return path;
    }

    protected void onNeverAskAgainPermission() {
        Toast.makeText(this, "Never!", Toast.LENGTH_SHORT).show();
    }

    protected void onSuccessAskPermission() {
        fileToBase64();
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
    }


    protected void onDeniedPermission() {
        Toast.makeText(this, "Denied!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void fixedPDFDirectPrint() {
        if (!mainBinding.editTextPort.getText().toString().isEmpty() && !mainBinding.editTextIp.getText().toString().isEmpty()) {
            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
            try {
                CustomPrinterServiceBack customPrinterService = new CustomPrinterServiceBack(
                        mainBinding.editTextIp.getText().toString(),
                        Integer.parseInt(mainBinding.editTextPort.getText().toString()),
                        stream2file(getAssets().open("cobaprint.pdf"))
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