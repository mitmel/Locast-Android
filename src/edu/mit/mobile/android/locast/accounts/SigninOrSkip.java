package edu.mit.mobile.android.locast.accounts;
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import edu.mit.mobile.android.locast.net.NetworkClient;
import edu.mit.mobile.android.locast.ver2.R;

public class SigninOrSkip extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth_signin_or_skip);

		findViewById(R.id.sign_in).setOnClickListener(this);
		findViewById(R.id.skip).setOnClickListener(this);

	}

	private void skip(){
		//final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//prefs.edit().putBoolean(Authenticator.PREF_SKIP_AUTH, true).commit();
		Authenticator.addDemoAccount(this);
		setResult(RESULT_OK);
		finish();
	}
	public static final int REQUEST_SIGNIN = 100;

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.sign_in:
			startActivityForResult(new Intent(this, AuthenticatorActivity.class), REQUEST_SIGNIN);
			break;

		case R.id.skip:
			skip();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_SIGNIN:
			if (resultCode == RESULT_OK){
				setResult(RESULT_OK);
				finish();
			}
			break;
		}
	}

	/**
	 * @return true if this seems to be the first time running the app
	 */
	public static final boolean checkFirstTime(Context context){
		//final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//final boolean skip = prefs.getBoolean(Authenticator.PREF_SKIP_AUTH, false);

		final NetworkClient nc = NetworkClient.getInstance(context);

		return !nc.isAuthenticated() && !Authenticator.isDemoMode(context);
	}

	public static final void startSignin(Activity context, int requestCode){
		context.startActivityForResult(new Intent(context, SigninOrSkip.class), requestCode);
	}
}
