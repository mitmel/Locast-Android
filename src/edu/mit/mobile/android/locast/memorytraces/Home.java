package edu.mit.mobile.android.locast.memorytraces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.data.Itinerary;
import edu.mit.mobile.android.locast.ver2.R;
import edu.mit.mobile.android.locast.ver2.casts.LocatableListWithMap;
import edu.mit.mobile.android.widget.TextViewUtils;
import edu.mit.mobile.android.widget.TypefaceSwitcher;

public class Home extends FragmentActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.mt_home);

		TypefaceSwitcher.setTypeface(this, TracesConstants.TYPEFACE_TITLE, R.id.welcome_message,
				R.id.stories, R.id.themes, R.id.nearby);

		TextViewUtils.makeUppercase(this, R.id.welcome_message, R.id.stories, R.id.themes, R.id.nearby);

		findViewById(R.id.stories).setOnClickListener(this);
		findViewById(R.id.themes).setOnClickListener(this);
		findViewById(R.id.nearby).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.stories:
				startActivity(new Intent(Intent.ACTION_VIEW, Itinerary.CONTENT_URI));
				break;

			case R.id.themes:
				startActivity(new Intent(Intent.ACTION_VIEW, Cast.getTagListUri(Cast.CONTENT_URI)));
				break;

			case R.id.nearby:
				startActivity(new Intent(LocatableListWithMap.ACTION_SEARCH_NEARBY,
						Cast.CONTENT_URI));
				break;
		}

	}

}
