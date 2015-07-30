package net.majorkernelpanic.example3;

import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.rtsp.RtspServer.LocalBinder;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A straightforward example of how to use the RTSP server included in
 * libstreaming.
 */
public class MainActivity1 extends Activity {

	private final static String TAG = "MainActivity";

	private SurfaceView mSurfaceView;

	private TextView rtspAddressTv;
	
	private static final int CUSTOME_PORT = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main1);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		rtspAddressTv = (TextView) findViewById(R.id.rtsp_address_id);

		// Sets the port of the RTSP server to 1234
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString(RtspServer.KEY_PORT, String.valueOf(CUSTOME_PORT));
		editor.commit();

		// Configures the SessionBuilder
		SessionBuilder.getInstance().setSurfaceView(mSurfaceView)
				.setPreviewOrientation(90).setContext(getApplicationContext())
				.setAudioEncoder(SessionBuilder.AUDIO_AAC)
				.setVideoEncoder(SessionBuilder.VIDEO_H264).build();
		
		// Starts the RTSP server
//		this.startService(new Intent(this, RtspServer.class));
		bindRtspServer();

		displayIpAddress();// Display address
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unbindRtspServer();
	}
	
	private void bindRtspServer() {
		this.bindService(new Intent(this, RtspServer.class), connection, Service.BIND_AUTO_CREATE);
	}
	
	private void unbindRtspServer() {
		unbindService(connection);
	} 
	
	private RtspServer rtspServer = null;
	
	private ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder local = (LocalBinder) service;
			rtspServer = local.getService();
		}
	};

	private void displayIpAddress() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		// 获取IP地址，注意获取的结果是整数
		int address = wifiInfo.getIpAddress();
		String rtspAddress = "rtsp://" + intToIp(address) + ":" + CUSTOME_PORT;
		Toast.makeText(this, rtspAddress, Toast.LENGTH_LONG).show();// 用toast打印地址
		rtspAddressTv.setText(rtspAddress);
		System.out.println("rtspAddress--->>>" + rtspAddress);
	}

	// 整形转IP
	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}
}
