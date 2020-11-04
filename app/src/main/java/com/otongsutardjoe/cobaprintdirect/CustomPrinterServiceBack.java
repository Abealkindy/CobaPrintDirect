package com.otongsutardjoe.cobaprintdirect;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class CustomPrinterServiceBack extends AsyncTask<Void, Void, Boolean> {

    private PrintServiceListener mPrintServiceListener;

    private String mPrinterIP;

    private int mPrinterPort;

    private File mFile;

    public CustomPrinterServiceBack(final String printerIP, final int printerPort, final File file) {
        mPrinterIP = printerIP;
        mPrinterPort = printerPort;
        mFile = file;
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
            OutputStream os;
            if (clientSocket != null) {
                os = clientSocket.getOutputStream();
                if (os != null) {
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();
                }
            }
        } catch (Exception e) {
            if (mPrintServiceListener != null) {
                mPrintServiceListener.onNetworkError(e.getMessage());
            }

            Log.e("Error ", e.toString());
            result = false;
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (bis != null) {
                    bis.close();
                }
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
