package edu.mit.mobile.android.locast.memorytraces;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.locast.data.Tag;
import edu.mit.mobile.android.locast.ver2.R;

public class ThemesList extends FragmentActivity implements LoaderCallbacks<Cursor> {

	private static final String[] FROM = new String[] { Tag._NAME };

	private static final String[] PROJECTION = ArrayUtils.concat(FROM, new String[] { Tag._ID });

	private static final int[] TO = new int[] { android.R.id.text1 };

	private final SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this,
			android.R.layout.simple_list_item_1, null, FROM, TO, 0);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list_activity);

		final ListView lv = (ListView) findViewById(android.R.id.list);

		lv.setAdapter(mAdapter);

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, Tag.CONTENT_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		mAdapter.swapCursor(c);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);

	}

}
