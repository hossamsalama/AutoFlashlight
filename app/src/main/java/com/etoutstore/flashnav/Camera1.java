package com.etoutstore.flashnav;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;


public class Camera1 implements CameraType{
    private Context mContext;
    private static int freq;
    private static Thread t;
    private static StroboRunner sr;
    private static Camera camera;
    private static Camera.Parameters params;


    Camera1(Context applicationContext) {
        mContext = applicationContext;
    }

    public void setFreq(int freq) {
        Camera1.freq = freq;
    }

    static void startCamera(){
        if (camera == null) {
            camera = Camera.open();
            params = camera.getParameters();

        }
        else {
            Log.e(String.valueOf((R.string.app_name)), "failed to open Camera");
        }
    }
    static void releaseCameraAndPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private  boolean isCameraUseByApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }

    @Override
    public void switchFlashLight(boolean status) {
        startCamera();
        if (status) {
            if (freq != 0) {
                sr = new StroboRunner();
                sr.freq = freq;

                if (t != null) {
                    t.interrupt(); // this. kill previous thread.
                }

                t = new Thread(sr);
                t.start();
                return;
            } else
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }

        if (!status) {
            if (t != null) {
                sr.stopRunning = true;
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                t.interrupt(); // this. kill previous thread.
                t = null;
                return;
            } else
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);


        }
        camera.setParameters(params);
        camera.startPreview();

    }

    private  class StroboRunner implements Runnable {

        int freq;
        boolean stopRunning = false;

        @Override
        public void run() {
            if (!isCameraUseByApp()) {
                startCamera();
            }
            Camera.Parameters paramsOn = camera.getParameters();
            Camera.Parameters paramsOff = params;
            paramsOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            paramsOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            try {
                while (!stopRunning) {
                    camera.setParameters(paramsOn);
                    camera.startPreview();
                    // We make the thread sleeping
                    Thread.sleep(1000 - freq * 90);
                    camera.setParameters(paramsOff);
                    camera.startPreview();
                    Thread.sleep(1000 - freq * 90);
                }
            } catch (Throwable t) {
            }
        }
    }
}
