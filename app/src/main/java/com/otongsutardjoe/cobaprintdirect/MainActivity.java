package com.otongsutardjoe.cobaprintdirect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.otongsutardjoe.cobaprintdirect.databinding.ActivityMainBinding;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFTextFormat;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.text.PDFTextStripperByArea;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.util.PDFHighlighter;

import net.sf.andpdf.nio.ByteBuffer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import io.github.jonathanlink.PDFLayoutTextStripper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CustomPrinterService.PrintServiceListener {
    ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        PDFBoxResourceLoader.init(MainActivity.this);
        initEvent();
    }

    private void initEvent() {

        mainBinding.buttonPrintWithSocket.setOnClickListener(v -> {
            if (!mainBinding.editTextIp.getText().toString().isEmpty()) {
                Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
                try {
                    CustomPrinterService customPrinterService = new CustomPrinterService(
                            mainBinding.editTextIp.getText().toString(),
                            9100,
                            stream2file(getAssets().open("cobaprint.pdf")),
                            "stream2file.pdf",
                            CustomPrinterService.PaperSize.A4,
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
//            Toast.makeText(this, "Start!", Toast.LENGTH_SHORT).show();
//            new MyTask("url", MainActivity.this).execute();
//            try {
            //with iText
//                StringBuilder parsedText = new StringBuilder();
//                PdfReader reader = new PdfReader(getAssets().open("cobaprint.pdf"));
//                int n = reader.getNumberOfPages();
//                for (int i = 0; i < n; i++) {
//                    parsedText.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim()); //Extracting the content from the different pages
//                }
//                Log.e("Hasil parse", parsedText.toString());
//                reader.close();
            //with PDFBox Lib
//                PDDocument document = PDDocument.load(getAssets().open("cobaprint.pdf"));
//                PDFTextStripper s = new PDFTextStripper();
//                s.setStartPage(0);
//                String content = s.getText(document);
//                Log.e("Hasil parse", content);
//            } catch (Exception e) {
//                Log.e("Error!", e.getMessage());
//            }

        });
        mainBinding.buttonPrintWithPrintHelper.setOnClickListener(v -> {
            //photo print
//            try {
            //photo
//                doPhotoPrint(renderToBitmap(MainActivity.this, getAssets().open("cobaprint.pdf")));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            printPDF();
        });
    }

    public static final String PREFIX = "stream2file";
    public static final String SUFFIX = ".pdf";

    public static File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

    private void printPDF() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(MainActivity.this, getAssets().open("cobaprint.pdf"));
            printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
        }
    }

    @Nullable
    public static Bitmap renderToBitmap(Context context, InputStream inStream) {
        Bitmap bi = null;
        try {
            byte[] decode = IOUtils.toByteArray(inStream);
            ByteBuffer buf = ByteBuffer.wrap(decode);
            PDFPage mPdfPage = new PDFFile(buf).getPage(0);
            float width = mPdfPage.getWidth();
            float height = mPdfPage.getHeight();
            RectF clip = null;
            bi = mPdfPage.getImage((int) (width), (int) (height), clip, true,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                // do nothing because the stream has already been closed
            }
        }
        return bi;
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


    @SuppressLint("StaticFieldLeak")
    private class MyTask extends AsyncTask<Void, Void, Void> {
        String url;
        Context context;

        public MyTask(String url, Context context) {
            this.url = url;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            Socket socket;
            try {
//                socket = new Socket("10.69.46.231", 9100);
//                PrintWriter outputStream = new PrintWriter(socket.getOutputStream());
                StringBuilder parsedText = new StringBuilder();
                PdfReader reader = new PdfReader(getAssets().open("cobaprint.pdf"));
                int n = reader.getNumberOfPages();
                for (int i = 0; i < n; i++) {
                    parsedText.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim()).append("\n"); //Extracting the content from the different pages
                }
                Log.e("Hasil parse \n", parsedText.toString());
//                org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(getAssets().open("cobaprint.pdf"));
//                PDFTextStripper s = new PDFLayoutTextStripper();
//                s.setStartPage(0);
//                String content = s.getText(document);
//                outputStream.print("Test Print");
                reader.close();
//                outputStream.close();
//                socket.close();
//                if (outputStream.checkError()) {
//                    Log.e("berhasil ", "no");
//                } else {
//                    Log.e("berhasil ", "yes");
//                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e("Error! ", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Finished!", Toast.LENGTH_SHORT).show();
        }
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

    private void doPhotoPrint(Bitmap bitmap) {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.setOrientation(PrintHelper.ORIENTATION_PORTRAIT);
        photoPrinter.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
        photoPrinter.printBitmap("error.png - test print", bitmap, () -> Toast.makeText(MainActivity.this, "Kelar wey!", Toast.LENGTH_SHORT).show());
    }
}