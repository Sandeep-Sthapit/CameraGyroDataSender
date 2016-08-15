package com.example.sandeep.majorproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener {
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    TextView tv_gyro;
    EditText et_server;

    long start_time;
    long end_time;
    long diff_time_cam;

    Camera.PictureCallback jpegCallback;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyro;
    private Sensor m;

    float gx;
    float gy;
    float gz;


    float timestamp;
    float timestampOldCalibrated;

    static Integer im_count = 1;
    static String data_write = "asd";
    int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_time = System.currentTimeMillis();

        count = 0;
        timestamp = 10;
        timestampOldCalibrated = 1;

        tv_gyro = (TextView) findViewById(R.id.tv_gyro);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        jpegCallback = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream fOut = null;
                OutputStreamWriter myOutWriter;
                File myFile;
                String filename;
                gx = 0;
                gy = 0;
                gz = 0;
                try {
                    end_time = System.currentTimeMillis();
                    diff_time_cam = end_time - start_time;
                    filename = im_count.toString() + ";" + data_write + ".jpg";
                    im_count++;
                    String path = Environment.getExternalStorageDirectory().toString();
                    File appDirectory = new File(path + "/" + "MajorProject" +
                            "");
                    appDirectory.mkdirs();
                    myFile = new File(path + "/" + "MajorProject", filename);
                    myFile.createNewFile();

                    //to write to sensor file
                    //f_out_data.close();

                    fOut = new FileOutputStream(myFile);
                    fOut.write(data);
                    myOutWriter =
                            new OutputStreamWriter(fOut);
                    myOutWriter.close();
                    fOut.close();
                    Toast.makeText(getApplicationContext(), "Done writing '" + filename + "'", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }

                //Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_LONG).show();
                refreshCamera();
            }
        };
    }

    public void captureImage(View v) throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters param;
        param = camera.getParameters();
        param.setPreviewSize(352, 288);
        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        Sensor sensor = event.sensor;
        timestamp = event.timestamp;

        float x;
        float y;
        float z;

        if (sensor.equals(mGyro)) {

            float dT = (timestamp - timestampOldCalibrated) / 1000000000;

            // Axis of the rotation sample, not normalized yet.
            float X = event.values[0] * dT;
            float Y = event.values[1] * dT;
            float Z = event.values[2] * dT;

            if (count > 2) {
                gx += X;
                gy += Y;
                gz += Z;
            }
            String data2 = "gx = " + gx + "\ngy = " + gy + "\ngz = " + gz;
            data_write = gx + ";" + gy + ";" + gz + ";";

            tv_gyro.setText(data2);
        }
        timestampOldCalibrated = timestamp;
        count++;

    }
    // Do something with this sensor value.

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }



    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

    public void onCompress(View view){

        String path = Environment.getExternalStorageDirectory().toString() + "/" + "MajorProject";
        String path2 = Environment.getExternalStorageDirectory().toString() + "/" + "MajorProject.zip";
        zipFileAtPath(path, path2);

        Intent intent = new Intent(MainActivity.this, SendActivity.class);

        Toast.makeText(view.getContext(), "Preparing to Send", Toast.LENGTH_SHORT).show();
        MainActivity.this.startActivity(intent);
    }
}