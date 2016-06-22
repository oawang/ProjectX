package am.project.x.activities.util.printer.test;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import am.project.x.R;
import am.project.x.activities.util.printer.data.PrinterData;

/**
 * 蓝牙打印机测试器
 * Created by Alex on 2016/6/22.
 */
public class BluetoothPrinterTester {


    private Activity activity;
    private BluetoothDevice mDevice;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//蓝牙打印UUID
    private int type;
    private AlertDialog dlgInfo;

    public BluetoothPrinterTester(Activity activity) {
        this.activity = activity;
    }

    public void startTest(BluetoothDevice device, int type) {
        mDevice = device;
        this.type = type;
        this.type = type;
        if (dlgInfo == null) {
            dlgInfo = new AlertDialog.Builder(activity).create();
        }
        new PrintTask(dlgInfo).execute();
    }

    private class PrintTask extends AsyncTask<Void, Integer, Integer> implements
            DialogInterface.OnCancelListener, DialogInterface.OnClickListener {

        private BluetoothSocket socket;
        private OutputStream out;
        private AlertDialog dlgInfo;

        public PrintTask(AlertDialog dlgInfo) {
            this.dlgInfo = dlgInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlgInfo.setOnCancelListener(this);
            dlgInfo.setButton(DialogInterface.BUTTON_NEGATIVE,
                    activity.getString(R.string.printer_cancel), this);
            dlgInfo.setMessage(activity.getString(R.string.printer_test_message_0));
            dlgInfo.show();
            dlgInfo.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.printer_cancel);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            publishProgress(0);
            byte[] data;
            try {
                if (type == PrinterData.TYPE_80)
                    data = PrinterData.getPrintData80(activity.getResources());
                else
                    data = PrinterData.getPrintData58(activity.getResources());
            } catch (Exception e) {
                return -1;
            }
            publishProgress(1);
            try {
                socket = mDevice.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
            } catch (Exception e) {
                destroy();
                return -2;
            }
            publishProgress(2);
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                destroy();
                return -3;
            }
            publishProgress(3);
            try {
                out.write(data);
                out.flush();
            } catch (IOException e) {
                destroy();
                return -4;
            }
            publishProgress(4);
            destroy();
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values == null || values.length < 1)
                return;
            switch (values[0]) {
                case 0:
                    dlgInfo.setMessage(activity.getString(R.string.printer_test_message_1));
                    break;
                case 1:
                    dlgInfo.setMessage(activity.getString(R.string.printer_test_message_2));
                    break;
                case 2:
                    dlgInfo.setMessage(activity.getString(R.string.printer_test_message_3));
                    break;
                case 3:
                    dlgInfo.setMessage(activity.getString(R.string.printer_test_message_4));
                    break;
                case 4:
                    dlgInfo.setMessage(activity.getString(R.string.printer_test_message_5));
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            destroy();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            dlgInfo.setOnCancelListener(null);
            if (integer != null) {
                switch (integer) {
                    case 0:
                        dlgInfo.setMessage(activity.getString(R.string.printer_result_message_1));
                        break;
                    case -1:
                        dlgInfo.setMessage(activity.getString(R.string.printer_result_message_2));
                        break;
                    case -2:
                        dlgInfo.setMessage(activity.getString(R.string.printer_result_message_3));
                        break;
                    case -3:
                        dlgInfo.setMessage(activity.getString(R.string.printer_result_message_4));
                        break;
                    case -4:
                        dlgInfo.setMessage(activity.getString(R.string.printer_result_message_5));
                        break;
                }
                dlgInfo.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setText(R.string.printer_determine);
            }
        }

        public void destroy() {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            cancel(true);
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (getStatus() == Status.RUNNING) {
                cancel(true);
            }
        }
    }
}