package com.example.sandeep.majorproject;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
public class SendActivity extends AppCompatActivity {
    EditText et_server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        et_server = (EditText) findViewById(R.id.et_server);
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";
        String msgToServer;

        MyClientTask() {
            dstAddress = "192.168.64.1";
            dstPort = 8080;
            msgToServer = "";
        }

        MyClientTask(String host) {
            dstAddress = host;
            dstPort = 8080;
            msgToServer = "";
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                String path = Environment.getExternalStorageDirectory().toString();
                File file = new File(path, "MajorProject.zip");
                long length = file.length();
                byte[] bytes = new byte[16 * 1024];
                InputStream in = new FileInputStream(file);
                OutputStream out = socket.getOutputStream();

                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }

                out.close();
                in.close();
                socket.close();

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //textResponse.setText(response);
            super.onPostExecute(result);
        }


    }

    public void onSendButtonClicked(View view) {
        et_server = (EditText) findViewById(R.id.et_server);
        MyClientTask myClientTask = new MyClientTask(et_server.getText().toString());
        myClientTask.execute();
        Toast.makeText(view.getContext(), "Data Sent", Toast.LENGTH_LONG).show();
    }

}
