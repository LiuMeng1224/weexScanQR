package org.weex.plugin.weexscanqr;

import android.content.Intent;

import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

/**
 * @author liumeng
 * 扫码功能
 *
 */
public class WeexScanQR extends WXModule {

    /**
     * @author liumeng
     *调用摄像头扫码
     * @param title 标题
     * @param callback  异步回调实例
     */
    @JSMethod(uiThread = false)
    public void scanQR(String title, JSCallback callback) {

        ScanActivity.myCallback = callback;
        Intent intent = new Intent(mWXSDKInstance.getContext(),ScanActivity.class);
        intent.putExtra("title",title);
        mWXSDKInstance.getContext().startActivity(intent);
    }
}