package com.huantansheng.cameralibrary.state;

import android.view.Surface;
import android.view.SurfaceHolder;

import com.huantansheng.cameralibrary.CameraInterface;
import com.huantansheng.cameralibrary.JCameraView;
import com.huantansheng.cameralibrary.util.LogUtil;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/9/8
 * 描    述：
 * =====================================
 */
public class BorrowVideoState implements State {
    private final String TAG = "BorrowVideoState";
    private CameraMachine machine;

    public BorrowVideoState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
        machine.setState(machine.getPreviewState());
    }

    @Override
    public void stop() {

    }

    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {

    }


    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {

    }

    @Override
    public void record(Surface surface, float screenProp) {

    }

    @Override
    public void stopRecord(boolean isShort, long time) {

    }

    @Override
    public void cancel(SurfaceHolder holder, float screenProp) {
        machine.getView().resetState(JCameraView.TYPE_VIDEO);
        machine.setState(machine.getPreviewState());
    }

    @Override
    public void confirm() {
        machine.getView().confirmState(JCameraView.TYPE_VIDEO);
        machine.setState(machine.getPreviewState());
    }

    @Override
    public void zoom(float zoom, int type) {
        LogUtil.i(TAG, "zoom");
    }

    @Override
    public void flash(String mode) {

    }
}
