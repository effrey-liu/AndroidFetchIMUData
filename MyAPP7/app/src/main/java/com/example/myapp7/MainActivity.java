package com.example.myapp7;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private TextView time;
    public String time_value;
    private SensorManager sensorManager;
    private Toast fylToast;
    public double LinearX_value;
    public double LinearY_value;
    public double LinearZ_value;
    public double RotationX_value;
    public double RotationY_value;
    public double RotationZ_value;
    public double RotationV_value;
//    private Interpreter tflite;
    boolean load_result;
    private static final String TAG = "assets:";
    private float[][] sensorData;
    Classifier fier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        time =(TextView)findViewById(R.id.myTime);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //获取传感器类型，三个传感器分别为加速度传感器、方向传感器、角速度传感器
        Sensor sensor1 = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        Sensor sensor3 = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        //注册传感器监听器
        sensorManager.registerListener(listener1, sensor1, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener2, sensor2, SensorManager.SENSOR_DELAY_NORMAL);


        InputStreamReader is = null;
        String line = "";
        String SplitBy = "\t";
        String[] values;

        sensorData = new float[20][6];
        int row = 0;
        try {
            is = new InputStreamReader(getAssets().open("sensor-data1.csv"));
            BufferedReader reader = new BufferedReader(is);
            int tmp = 0;
            while ((line = reader.readLine()) != null) {
                values = line.split(SplitBy);
//                if(tmp == 0){
//                    Log.e("1111","output:" + values.length);
//                    tmp++;
//                }
                for(int i = 0; i < values.length; i++){
                    sensorData[row][i] = Float.parseFloat(values[i]);
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Log.e("1111","output:" + row);

//        fier = new Classifier(getAssets());

    }


    @Override
    public void onStart() {
        super.onStart();
//        Button start = (Button) findViewById(R.id.addData_start);
//        Button end = (Button) findViewById(R.id.addData_end);
//
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "显示", Toast.LENGTH_SHORT).show();
//
//                getTime();
//                time.setText("当前时间：" + time_value);
//            }
//        });
////        if(LinearX_value > 0.2  && LinearY_value > 0.2 && LinearZ_value > 0.2){
////            getTime();
////            time.setText("当前时间：" + time_value);
////        }
//        getTime();
//        time.setText("当前时间：" + time_value);

        String MODEL_FILE = "converted_model.tflite";
        Interpreter tfLite = null;
        try {
            tfLite = new Interpreter(loadModelFile(getAssets(), MODEL_FILE));
        }catch(IOException e){
            e.printStackTrace();
        }

        int[] shape = tfLite.getOutputTensor(0).shape();  // shape.length = 4
        /*
            input_shape = (1, 20, 6, 1)
            output_shape = (1, 9)
            Log.e("1111","output:" + shape.length);
            Log.e("1111","output:" + shape[0] + "  " + shape[1]);
            Log.e("1111","output:" + shape[0] + "  " + shape[1] + "  " + shape[2] + "  " + shape[3]);
         */






        int net_input_sz = 2;   //        input_shape=(1, 20, 6, 1)


        ByteBuffer inputData1;
        inputData1 = ByteBuffer.allocateDirect(1 * 20 * 6 * 1 * 4);//4表示一个浮点占4byte
        inputData1.order(ByteOrder.nativeOrder());
        inputData1.rewind();
        inputData1.putFloat(1.0f);
        inputData1.putFloat(1.0f);
        inputData1.putFloat(1.0f);
        inputData1.putFloat(1.0f);

        ByteBuffer inputData2;
        inputData2 = ByteBuffer.allocateDirect(net_input_sz * net_input_sz * 4);//4表示一个浮点占4byte
        inputData2.order(ByteOrder.nativeOrder());
        inputData2.rewind();
        inputData2.putFloat(0.0f);
        inputData2.putFloat(0.0f);
        inputData2.putFloat(0.0f);
        inputData2.putFloat(0.0f);

        Object[] inputArray = {inputData1, inputData2};

        float[] output1, output2;
        output1 = new float[1];
        output2 = new float[1];
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, output1);
        outputMap.put(1, output2);

        float[][][][] input = getInputMatrix();
        float[][] output = new float[1][9];
//        float output = -1;

        int catalog = -1;
        float maxPossibility = -1;
        tfLite.run(input, output);
        for(int i = 0; i < 9; i++){
            Log.e("1111","output:" + i + "  " + output[0][i]);
            if(output[0][i] > maxPossibility){
                maxPossibility = output[0][i];
                catalog = i;
            }
        }
        Log.e("result","output:" + catalog + "  " + maxPossibility);

    }


    /**
     * 获取一组传感器数据
     * input_shape = (20, 6, 1)
     */
    public float[][][][] getInputMatrix() {
        //新建一个1*30*160*3的四维数组
        float[][][][] inFloat = new float[1][20][6][1];

        for (int i = 0; i < 20; ++i) {
            for (int j = 0; j < 6; ++j) {
                inFloat[0][i][j][0] = sensorData[i][j];
            }
        }
        return inFloat;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int Action = event.getAction();
        float X = event.getX();
        float Y = event.getY();
//        if(LinearX_value > 0.2  && LinearY_value > 0.2 && LinearZ_value > 0.2){
//            getTime();
//            time.setText("当前时间：" + time_value);
//        }
//        Toast.makeText(MainActivity.this, "检测到点击", Toast.LENGTH_SHORT).show();

        fylToast = Toast.makeText(getApplicationContext(),"检测到点击",Toast.LENGTH_LONG);
        fylToast.setGravity(Gravity.BOTTOM,0,0);
        fylToast.show();
        getTime();
        time.setText("当前时间：" + time_value);
        return super.onTouchEvent(event);

    }

    MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new
                FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private MappedByteBuffer loadModelFile(String model) throws IOException {
        AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd(model + ".tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss.SSS");
        Date mDate = new Date(System.currentTimeMillis());
        String myTime = formatter.format(mDate);
//        time = (TextView) findViewById(R.id.myTime);
//        time.setText("当前时间：" + myTime);
        time_value = formatter.format(mDate);
    }


    private SensorEventListener listener1 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            LinearX_value = event.values[0];
            LinearY_value = event.values[1];
            LinearZ_value = event.values[2];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorEventListener listener2 = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            RotationX_value = event.values[0];
            RotationY_value = event.values[1];
            RotationZ_value = event.values[2];
            RotationV_value = event.values[3];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}


class Classifier{
    //mnist.tflite: 来自kotlin DEMO, 识别率很低, 文件小.
    //mnist_big.tflite: 来自JAVA DEMO, 识别率高,文件大
    final String MODEL = "converted_model.tflite";
    //插入器
    Interpreter interpreter;
    //输入识别的图像尺寸
    int bmWidth, bmHeight;
    //用于读取tflite文件
    AssetManager asset;
    //用于创建ByteBuffer
    int modelInputSize;
    Classifier(AssetManager asset){
        this.asset = asset;
        //创建插入器.
        Interpreter.Options op = new Interpreter.Options();
        op.setUseNNAPI(true);
        interpreter = new Interpreter(loadModel(), op);
        //获取输入信息
        int[] shape = interpreter.getInputTensor(0).shape();
        bmWidth = shape[1];
        bmHeight = shape[2];

        Log.e("1111","output:" + shape);

        //计算ByteBuffer大小.
        int FLOAT_TYPE_SIZE = 4;
        int PIXEL_SIZE = 1;
        modelInputSize = FLOAT_TYPE_SIZE * bmWidth * bmHeight * PIXEL_SIZE;

    }

    //加载模型
    ByteBuffer loadModel(){
        try {
            AssetFileDescriptor fd = asset.openFd(MODEL);
            FileInputStream is = new FileInputStream(fd.getFileDescriptor());
            FileChannel channel = is.getChannel();
            long startOffset = fd.getStartOffset();
            long declareLength = fd.getDeclaredLength();
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //执行分类
    String classifier(Bitmap bm){
        //缩放图片到指定尺寸.(28*28)
        Bitmap nbm = Bitmap.createScaledBitmap(bm, bmWidth, bmHeight, true);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(nbm);

        //Kotlin中的代码:
        //  val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        //  平时不用它, 看这代码头痛了好久.
        //若创建的数组不对, 如用float[10], 或float[2][10]
        //则会导致异常(Google 百度都不知道):
        /**  2020-09-10 10:55:18.213 14429-14429/com.ansondroider.digitclassifierbytfl E/AndroidRuntime: FATAL EXCEPTION: main
         Process: com.ansondroider.digitclassifierbytfl, PID: 14429
         java.lang.IllegalArgumentException: Cannot copy from a TensorFlowLite tensor (softmax_tensor) with shape [1, 10] to a Java object with shape [2, 10].
         at org.tensorflow.lite.Tensor.throwIfDstShapeIsIncompatible(Tensor.java:482)
         at org.tensorflow.lite.Tensor.copyTo(Tensor.java:252)
         at org.tensorflow.lite.NativeInterpreterWrapper.run(NativeInterpreterWrapper.java:170)
         at org.tensorflow.lite.Interpreter.runForMultipleInputsOutputs(Interpreter.java:347)
         at org.tensorflow.lite.Interpreter.run(Interpreter.java:306)
         at com.ansondroider.digitclassifierbytfl.DigitClassifierByTFL$Classifier.classifier(DigitClassifierByTFL.java:98)
         at com.ansondroider.digitclassifierbytfl.DigitClassifierByTFL$1.onWriteDone(DigitClassifierByTFL.java:33)
         at com.ansondroider.digitclassifierbytfl.PaintView$1.run(PaintView.java:86)
         at android.os.Handler.handleCallback(Handler.java:883)
         at android.os.Handler.dispatchMessage(Handler.java:100)
         at android.os.Looper.loop(Looper.java:214)
         at android.app.ActivityThread.main(ActivityThread.java:7356)
         at java.lang.reflect.Method.invoke(Native Method)
         at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
         at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:930)**/
        float[][] result = new float[1][10];
        //执行.
        interpreter.run(byteBuffer, result);
        //格式化输出
        return getOutputSting(result);
    }

    String getOutputSting(float[][] floats){
        //Kotlin 代码:
        //  val maxIndex = output.indices.maxBy { output[it] } ?: -1
        //  return "Prediction Result: %d\nConfidence: %2f".format(maxIndex, output[maxIndex])
        //在float[10]数组中, 存放了推算的结果, 下标分别对应的是[0,9]的数字.
        //只需要遍历10个数中, 找出最大的值即可.
        StringBuilder result = new StringBuilder("Result:\n");
        float[] res = floats[0];
        float max = -1;
        int v = -1;
        for(int i = 0; i < res.length; i ++){
            result.append("[" + i + "]=" + res[i]).append("\n");
            if(max < res[i]){
                max = res[i];
                v = i;
            }
        }
        result.append("BEST: " + v);
        return result.toString();
    }

    ByteBuffer convertBitmapToByteBuffer(Bitmap bm){
        //刚开始, 用错了函数接口: ByteBuffer.allocate
        //这样会导致推算的结果不管输入如何变化, 都输出固定的float[10]
        //在打开后一直不变, 而在调试过程中, 也出现过多次生新运行都显示同样的结果.
        ByteBuffer bf = ByteBuffer.allocateDirect(modelInputSize);
        bf.order(ByteOrder.nativeOrder());
        int[] pixels = new int[bmWidth * bmHeight];
        bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
        for(int i = 0; i < pixels.length; i ++){
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;
            float normalizePixelValue = (r + g + b) / 3f / 255f;
            bf.putFloat(normalizePixelValue);
        }
        return bf;
    }

    void close(){
        interpreter.close();
    }
}

