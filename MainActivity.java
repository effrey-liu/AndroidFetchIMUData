package com.example.myapp6;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.collection.ArraySet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity{

    private TextView time;
    private TextView LinearX;
    private TextView LinearY;
    private TextView LinearZ;
    private TextView RotationX;
    private TextView RotationY;
    private TextView RotationZ;
    private TextView RotationV;


    private SensorManager sensorManager;
    public boolean isRecord;

    public String time_value;
    public double LinearX_value;
    public double LinearY_value;
    public double LinearZ_value;
    public double RotationX_value;
    public double RotationY_value;
    public double RotationZ_value;
    public double RotationV_value;
    private List<Double> LS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        LinearX = (TextView) findViewById(R.id.LinearX);
//        LinearY = (TextView) findViewById(R.id.LinearY);
//        LinearZ = (TextView) findViewById(R.id.LinearZ);
//
//        RotationX = (TextView) findViewById(R.id.RotationX);
//        RotationY = (TextView) findViewById(R.id.RotationY);
//        RotationZ = (TextView) findViewById(R.id.RotationZ);
//        RotationV = (TextView) findViewById(R.id.RotationV);
        LS = new ArrayList<>();
        //获取传感器管理对象
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //获取传感器类型，三个传感器分别为加速度传感器、方向传感器、角速度传感器
        Sensor sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        Sensor sensor3 = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        //注册传感器监听器
        sensorManager.registerListener(listener1, sensor1, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener2, sensor2, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(listener3, sensor3, SensorManager.SENSOR_DELAY_GAME);

        addData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener1);
            sensorManager.unregisterListener(listener2);
        }
    }

    private SensorEventListener listener1 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            getTime(); //时间相关，放在这里调用保证时间更新

            double xValue = event.values[0];
            double yValue = event.values[1];
            double zValue = event.values[2];

//            LinearX.setText("线性加速度X: " + xValue);
//            LinearY.setText("线性加速度Y: " + yValue);
//            LinearZ.setText("线性加速度Z: " + zValue);

            LinearX_value = xValue;
            LinearY_value = yValue;
            LinearZ_value = zValue;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorEventListener listener2 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            double xValue = event.values[0];
            double yValue = event.values[1];
            double zValue = event.values[2];
            double vValue = event.values[3];

//            RotationX.setText("旋转矢量X: " + xValue);
//            RotationY.setText("旋转矢量Y: " + yValue);
//            RotationZ.setText("旋转矢量Z: " + zValue);
//            RotationV.setText("旋转标量值V: " + zValue);

            RotationX_value = xValue;
            RotationY_value = yValue;
            RotationZ_value = zValue;
            RotationV_value = vValue;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    //时间相关方法
    private void getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss.SSS");
        Date mDate = new Date(System.currentTimeMillis());
        String myTime = formatter.format(mDate);
//        time = (TextView) findViewById(R.id.myTime);
//        time.setText("当前时间：" + myTime);
        time_value = formatter.format(mDate);
    }

    //添加数据方法
    private void addData() {
        Button start = (Button) findViewById(R.id.addData_start);
        Button end = (Button) findViewById(R.id.addData_end);
        Button clear = (Button) findViewById(R.id.clear);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "开始保存", Toast.LENGTH_SHORT).show();
                isRecord = true;
                if (isRecord == true) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Double[] dbs = new Double[]{LinearX_value, LinearY_value, LinearY_value,
                                    RotationX_value, RotationY_value, RotationZ_value};
                            Collections.addAll(LS, dbs);
                            writeLS(LS);
                            LS.clear();
                        }
                    }, 0, 100);
                }
            }
        });
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = false;
                Toast.makeText(MainActivity.this, "结束保存", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void endData(){
        Button end = (Button) findViewById(R.id.addData_end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = false;
                Toast.makeText(MainActivity.this, "结束保存", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clear(){
        Button clear = (Button) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "清除完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final String TAG = "ACCCollection:";

    public void writeLS(List<Double> LS) {
        try {
            String path= getFilesDir().getAbsolutePath();

//            String path = "data/data/com.example.myapp4/";
            File folder=new File(path);
            Log.i(TAG, "write: -------1");

            Log.i(TAG, folder.getPath());
            if (!folder.exists() || !folder.isDirectory())
            {

                Log.i(TAG, "write: --------2");
                folder.mkdirs();
            }
            File file=new File(path,"dataLR.csv");

            Log.i(TAG, file.getPath());

            if(!file.exists())
            {
                file.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            int i;
            for(i=0; i<LS.size(); i++)
            {
                bw.write(String.valueOf(LS.get(i)));
                if(i < LS.size() - 1){
                    bw.write(",");
                } else {
                    bw.newLine();// 行换行
                }
            }
            bw.close();
            Log.i(TAG, "write: ---------done!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @TargetApi(23)
    public static boolean checkPermission(Activity activity) {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //如果没有写sd卡权限
                isGranted = false;
            }
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            if (activity.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            if (!isGranted) {
                activity.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
                                .ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                                Manifest.permission.VIBRATE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.CAMERA},
                        102);
            }
        }
        return isGranted;
    }
}



