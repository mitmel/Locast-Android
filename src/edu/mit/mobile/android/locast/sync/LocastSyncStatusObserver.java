package edu.mit.mobile.android.locast.sync;
/*
 * Copyright (C) 2011  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.os.Handler;
import android.util.Log;
import edu.mit.mobile.android.locast.accounts.Authenticator;
import edu.mit.mobile.android.locast.data.MediaProvider;

/**
 * A implementation of a SyncStatusObserver that will be used to monitor 
 * the current account synchronization and notify to a specific handler
 * when the process is ended
 *
 * @author Cristian Piacente
 *
 */
public class LocastSyncStatusObserver implements SyncStatusObserver{
	
	Context mContext;
	Handler mHandler;
	
	public static final int
	MSG_SET_REFRESHING = 100,
	MSG_SET_NOT_REFRESHING = 101;
	
	private static final String TAG = LocastSyncStatusObserver.class.getSimpleName();
	
	public LocastSyncStatusObserver(Context context,Handler handler) {
		this.mContext=context;
		this.mHandler=handler;
	}
	@Override
	public void onStatusChanged(int which) {
		notifySyncStatusToHandler(mContext, mHandler);
	}
	public static void notifySyncStatusToHandler(Context context,Handler handler){
		Account a = Authenticator.getFirstAccount(context);
        if (!ContentResolver.isSyncActive(a, MediaProvider.AUTHORITY) &&
                !ContentResolver.isSyncPending(a, MediaProvider.AUTHORITY)) {
            Log.d(TAG, "Sync finished, should refresh now!!");
            handler.sendEmptyMessage(MSG_SET_NOT_REFRESHING);
        }
        else{
        	Log.d(TAG, "Sync Active or Pending!!");
        	handler.sendEmptyMessage(MSG_SET_REFRESHING);
        }
	}
}
