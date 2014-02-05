package edu.mit.mobile.android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class NetworkStatusBroadcastReceiver extends BroadcastReceiver{
	Context mContext;
	Handler mHandler;
	
	public static final int MSG_NETWORK_STATE=400;

	private static final String TAG = NetworkStatusBroadcastReceiver.class.getSimpleName();

	public NetworkStatusBroadcastReceiver(Context context,Handler handler) {
		this.mContext=context;
		this.mHandler=handler;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		mHandler.sendEmptyMessage(MSG_NETWORK_STATE);
	}

}
