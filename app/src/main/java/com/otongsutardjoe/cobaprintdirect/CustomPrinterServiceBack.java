package com.otongsutardjoe.cobaprintdirect;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class CustomPrinterServiceBack extends AsyncTask<Void, Void, Boolean> {

    public enum PaperSize {
        A4,
        A5
    }

    private static final String TAG = "CustomPrinterService";

    private PrintServiceListener mPrintServiceListener;

    private String mPrinterIP;
    private String mFilename;

    private int mPrinterPort;
    private int mNumberOfCopies;

    private File mFile;
    private PaperSize mPaperSize;

    public CustomPrinterServiceBack(
            final String printerIP,
            final int printerPort,
            final File file,
            final String filename,
            final PaperSize paperSize,
            final int copies) {
        mPrinterIP = printerIP;
        mPrinterPort = printerPort;
        mFile = file;
        mFilename = filename;
        mPaperSize = paperSize;
        mNumberOfCopies = copies;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Boolean result = null;
        Socket clientSocket = null;
        FileInputStream fis;
        BufferedInputStream bis = null;

        try {
            clientSocket = new Socket(mPrinterIP, mPrinterPort);
        } catch (Exception e) {
            if (mPrintServiceListener != null) {
                mPrintServiceListener.onNetworkError(e.getMessage());
            }

            Log.e("Error ", e.toString());
            result = false;
            e.printStackTrace();
        }

        byte[] mybytearray = new byte[(int) mFile.length()];
        try {
            fis = new FileInputStream(mFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            OutputStream os = clientSocket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
        } catch (Exception e) {
            if (mPrintServiceListener != null) {
                mPrintServiceListener.onNetworkError(e.getMessage());
            }

            Log.e("Error ", e.toString());
            result = false;
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                bis.close();
                if (result == null) {
                    result = true;
                }
            } catch (Exception e) {
                if (mPrintServiceListener != null) {
                    mPrintServiceListener.onNetworkError(e.getMessage());
                }

                Log.e("Error ", e.toString());
                result = false;
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (mPrintServiceListener != null) {
                mPrintServiceListener.onPrintCompleted();
            }
        } else {
            if (mPrintServiceListener != null) {
                mPrintServiceListener.onNetworkError("result false!");
            }
        }
    }

    public void setPrintServiceListener(PrintServiceListener listener) {
        mPrintServiceListener = listener;
    }

    public interface PrintServiceListener {

        void onPrintCompleted();

        void onNetworkError(String message);
    }
}
