package org.void1898.www.agilebuddy;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.feiyang.agilebuddy.client.control.MultiplePlayerController;

/**
 * main activity of AgileBuddy
 *
 * @author void1898@gamil.com
 */
public class AgileBuddyActivity extends Activity {

    public AgileBuddyView mAgileBuddyView;

    private SensorManager mSensorManager;

    private Sensor mSensor;

    private SensorEventListener mSensorEventListener;

    private MultiplePlayerController controller;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 禁止屏幕休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 全屏幕
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        mAgileBuddyView = (AgileBuddyView) findViewById(R.id.agile_buddy);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent e) {
                if (controller.isMyTurn()) {
                    mAgileBuddyView.handleMoving(e.values[SensorManager.DATA_X]);
                }
            }

            public void onAccuracyChanged(Sensor s, int accuracy) {
            }
        };
        // 注册重力感应监听
        mSensorManager.registerListener(mSensorEventListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);

        controller = MultiplePlayerController.getInstance();
        controller.setActivity(this);
        controller.gameStart();
    }

    @Override
    public void finish() {
        // 注销重力感应监听
        mSensorManager.unregisterListener(mSensorEventListener, mSensor);
        controller.stop();
        super.finish();
    }
}