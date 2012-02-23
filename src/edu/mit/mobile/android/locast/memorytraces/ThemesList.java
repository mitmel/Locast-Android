package edu.mit.mobile.android.locast.memorytraces;

import android.os.Bundle;
import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.sync.LocastSyncService;
import edu.mit.mobile.android.locast.tags.TagList;
import edu.mit.mobile.android.locast.ver2.R;
import edu.mit.mobile.android.widget.TextViewUtils;
import edu.mit.mobile.android.widget.TypefaceSwitcher;

public class ThemesList extends TagList {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		LocastSyncService.startExpeditedAutomaticSync(this, Cast.CONTENT_URI);

		setTitle(getTitle());

		TypefaceSwitcher.setTypeface(this, TracesConstants.TYPEFACE_TITLE, android.R.id.title);
		TextViewUtils.makeUppercase(this, android.R.id.title);
	}

	@Override
	public int getTagItemLayout() {
		return R.layout.themes_item;
	}
}