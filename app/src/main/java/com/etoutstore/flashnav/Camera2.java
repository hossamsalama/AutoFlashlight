package com.etoutstore.flashnav;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

class Camera2 implements CameraType{
    private Context mContext;
    private static int freq;
    private static Thread t;
    private static StroboRunner sr;
    private static CameraManager mCameraManager;
    private static String mCameraId;

    Camera2(Context context) {
        mContext = context;
    }

    public void setFreq(int freq) {
        Camera2.freq = freq;
    }

    @Override
    public void switchFlashLight(boolean status) {
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
            } else {
                if (t != null) {
                    t.interrupt(); // this. kill previous thread.
                }
                try {
                    mCameraManager.setTorchMode(mCameraId, status);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!status) {
            if (t != null) {
                sr.stopRunning = true;
                try {
                    mCameraManager.setTorchMode(mCameraId, status);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                t.interrupt(); // this. kill previous thread.
                t = null;
                return;
            } else {
                try {
                    mCameraManager.setTorchMode(mCameraId, status);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public void initializeCamer2() {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static class StroboRunner implements Runnable {

        int freq;
        boolean stopRunning = false;

        @Override
        public void run() {
            try {
                while (!stopRunning) {
                    try {
                        mCameraManager.setTorchMode(mCameraId, true);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    // We make the thread sleeping
                    Thread.sleep(500 - freq * 50);
                    try {
                        mCameraManager.setTorchMode(mCameraId, false);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(500 - freq * 50);
                }
            } catch (Throwable t) {
            }
        }
    }
}

