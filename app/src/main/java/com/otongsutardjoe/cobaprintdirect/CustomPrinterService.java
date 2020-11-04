package com.otongsutardjoe.cobaprintdirect;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class CustomPrinterService extends AsyncTask<Void, Void, Boolean> {

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

    public CustomPrinterService(
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
        Socket socket = null;
        DataOutputStream out = null;
        FileInputStream inputStream = null;
        try {
            socket = new Socket(mPrinterIP, mPrinterPort);
            out = new DataOutputStream(socket.getOutputStream());
//            DataInputStream input = new DataInputStream(socket.getInputStream());
            inputStream = new FileInputStream(mFile);
            //old
//            byte[] buffer = new byte[3000];
            //experimental
            byte[] buffer = new byte[3000];
            final char ESC = 0x1b;
            final String UEL = ESC + "%-12345X";
            final String ESC_SEQ = ESC + "%-12345\r\n";

            out.writeBytes(UEL);
            out.writeBytes("@PJL \r\n");
            out.writeBytes("@PJL JOB NAME = '" + mFilename + "' \r\n");
            out.writeBytes("@PJL SET PAPER=" + mPaperSize.name());
            out.writeBytes("@PJL SET COPIES=" + mNumberOfCopies);
            out.writeBytes("@PJL ENTER LANGUAGE = PDF\r\n");
            while (inputStream.read(buffer) != -1)
                out.write(buffer);
            out.writeBytes(ESC_SEQ);
            out.writeBytes("@PJL \r\n");
            out.writeBytes("@PJL RESET \r\n");
            out.writeBytes("@PJL EOJ NAME = '" + mFilename + "'");
            out.writeBytes(UEL);

            out.flush();
        } catch (Exception exception) {
            if (mPrintServiceListener != null) {
                mPrintServiceListener.onNetworkError(exception.getMessage());
            }

            Log.e("Error ", exception.toString());
            result = false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
                if (result == null) {
                    result = true;
                }
            } catch (Exception exception) {
                if (mPrintServiceListener != null) {
                    mPrintServiceListener.onNetworkError(exception.getMessage());
                }

                Log.e("Error ", exception.toString());
                result = false;
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
