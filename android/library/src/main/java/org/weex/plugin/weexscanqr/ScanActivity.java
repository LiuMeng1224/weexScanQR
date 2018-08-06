package org.weex.plugin.weexscanqr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.taobao.weex.bridge.JSCallback;

import org.weex.plugin.weexscanqr.camera.CameraManager;
import org.weex.plugin.weexscanqr.camera.CaptureActivityHandler;
import org.weex.plugin.weexscanqr.camera.InactivityTimer;
import org.weex.plugin.weexscanqr.camera.ViewfinderView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * 二维码扫描
 * 
 * @author Ryan.Tang
 */
public class ScanActivity extends Activity implements Callback {

	private ImageView iv_back;
	private TextView tv_activity_title;

	private TextView tv_type;

	private ViewfinderView viewfinderView;

	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;

	private CaptureActivityHandler handler;
	public static JSCallback myCallback = null;

	private String title = "";//标题

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		CameraManager.init(this);
		init();
	}

	public void init() {

		title = getIntent().getStringExtra("title");
		iv_back = (ImageView)findViewById(R.id.iv_back);
		viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
		tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		if(title!=null){
			tv_activity_title.setText(title);
		}

		iv_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map<String, Object> map = new HashMap<>();
				map.put("status", "cancel");
				map.put("msg", "取消扫描");
				map.put("result", "");

				if(myCallback!=null){
					myCallback.invoke(map);
				}
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		Map<String, Object> map = new HashMap<>();
		map.put("status", "cancel");
		map.put("msg", "取消扫描");
		map.put("result", "");

		if(myCallback!=null){
			myCallback.invoke(map);
		}
		super.onBackPressed();

	}

	@Override
	protected void onResume() {
		super.onResume();
		reSet();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	private void reSet() {
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
	}

	// 刷新
	public void reflush() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		reSet();
	}

	/**
	 * 处理扫描结果
	 * 
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		String resultString = result.getText();
		if (!TextUtils.isEmpty(resultString)) {

//			status: error 失败 success 成功 cancel取消
//			msg:错误信息
//			result:扫描结果，失败错误没有该字段。

			Map<String, Object> map = new HashMap<>();
			map.put("status", "success");
			map.put("msg", "扫描成功！");
			map.put("result", resultString);

			if(myCallback!=null){
				myCallback.invoke(map);
			}

			ScanActivity.this.finish();
		} else {

			reflush();
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	// private static final long VIBRATE_DURATION = 200L;

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	@SuppressWarnings("unused")
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				finish();
			}
		}
		return super.dispatchKeyEvent(event);
	}

}