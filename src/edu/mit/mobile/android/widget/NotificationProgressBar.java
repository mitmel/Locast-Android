package edu.mit.mobile.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.mit.mobile.android.locast.ver2.R;

public class NotificationProgressBar extends LinearLayout {
	String textID;
	private ProgressBar mProgressBar;
	private TextView mTextView;
	private TextView mNoDataTextView;
	public static boolean online=true;
	public static String INTENT_UPDATE_NETWORK_STATUS="edu.mit.mobile.android.widget.NotificationProgressBar.update";
	
	public NotificationProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
		View v=null;
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null){       
            v=inflater.inflate(R.layout.notification_progress_bar,this);
        }
		mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
		mNoDataTextView=(TextView)findViewById(R.id.noDataTextView);
		mTextView=(TextView)findViewById(R.id.emptyTextView);
		mTextView.setText(textID);
		updateNetworkStatus();
	}
	public NotificationProgressBar(Context context) {
		super(context);
		View.inflate(context, R.layout.notification_progress_bar, this);
		mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
		mTextView=(TextView)findViewById(R.id.emptyTextView);
	}

	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.NotificationProgressBar);
		textID = a.getString(R.styleable.NotificationProgressBar_android_text);
		a.recycle();
	}
	public synchronized void showProgressBar(boolean b){
		mProgressBar.setVisibility((b?View.VISIBLE:View.GONE));
		mTextView.setVisibility(((!b)?View.VISIBLE:View.GONE));
		mNoDataTextView.setVisibility((View.GONE));
		postInvalidate();
	}
	public synchronized void setNetworkStatus(boolean online){
		NotificationProgressBar.online=online;
		updateNetworkStatus();
	}
	public void updateNetworkStatus(){
		if(!online){
			mProgressBar.setVisibility(View.GONE);
			mTextView.setVisibility(View.GONE);
			mNoDataTextView.setVisibility((View.VISIBLE));
		}
		else{
			if(mProgressBar.getVisibility()==View.GONE && mTextView.getVisibility()==View.GONE){
				mProgressBar.setVisibility(View.VISIBLE);
				mTextView.setVisibility(View.GONE);
				mNoDataTextView.setVisibility((View.GONE));
			}
		}
		postInvalidate();
	}
}
