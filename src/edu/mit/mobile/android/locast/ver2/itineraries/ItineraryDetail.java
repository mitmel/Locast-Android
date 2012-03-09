package edu.mit.mobile.android.locast.ver2.itineraries;

/*
 * Copyright (C) 2011-2012  MIT Mobile Experience Lab
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

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4_map.app.LoaderManager;
import android.support.v4_map.app.MapFragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.locast.casts.CastCursorAdapter;
import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.data.Itinerary;
import edu.mit.mobile.android.locast.maps.CastsIconOverlay;
import edu.mit.mobile.android.locast.sync.LocastSyncService;
import edu.mit.mobile.android.locast.sync.LocastSyncStatusObserver;
import edu.mit.mobile.android.locast.ver2.R;
import edu.mit.mobile.android.maps.PathOverlay;
import edu.mit.mobile.android.widget.NotificationProgressBar;
import edu.mit.mobile.android.widget.RefreshButton;

public class ItineraryDetail extends MapFragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener {
	private static final String TAG = ItineraryDetail.class.getSimpleName();

	/**
	 * If the layout for this activity doesn't need / use a map, set this to false. This activity
	 * will attempt to hide the map if it's disabled.
	 */
	private static final boolean USE_MAP = false;

	private MapView mMapView;
	private MapController mMapController;

	private ListView mCastView;
	private CastCursorAdapter mCastAdapter;

	private ImageCache mImageCache;

	private Uri mUri;
	private Uri mCastsUri;

	private CastsIconOverlay mCastsOverlay;
	private PathOverlay mPathOverlay;
	private static final int UNKNOWN_COUNT = -1;
	private int mItineraryCastCount = UNKNOWN_COUNT;

	CursorLoader itinLoader;
	CursorLoader castLoader;

	private boolean mFirstLoadSync = true;

	private static final String[] ITINERARY_PROJECTION = new String[] { Itinerary._ID,
			Itinerary._DESCRIPTION, Itinerary._TITLE, Itinerary._CASTS_COUNT, Itinerary._PATH };

	private RefreshButton mRefresh;

	private Object mSyncHandle;

	private NotificationProgressBar mProgressBar;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LocastSyncStatusObserver.MSG_SET_REFRESHING:
					if (Constants.DEBUG) {
						Log.d(TAG, "refreshing...");
					}
					mProgressBar.showProgressBar(true);
					mRefresh.setRefreshing(true);
					break;

				case LocastSyncStatusObserver.MSG_SET_NOT_REFRESHING:
					if (Constants.DEBUG) {
						Log.d(TAG, "done loading.");
					}
					mProgressBar.showProgressBar(false);
					mRefresh.setRefreshing(false);
					break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle icicle) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(icicle);
		setContentView(R.layout.itinerary_detail);

		mProgressBar = (NotificationProgressBar) (findViewById(R.id.progressNotification));

		mImageCache = ImageCache.getInstance(this);

		mCastView = (ListView) findViewById(R.id.casts);
		findViewById(R.id.refresh).setOnClickListener(this);
		findViewById(R.id.home).setOnClickListener(this);

		final LayoutInflater layoutInflater = getLayoutInflater();

		mCastView.addHeaderView(
				layoutInflater.inflate(R.layout.itinerary_detail_list_header, mCastView, false),
				null, false);
		mCastView.addFooterView(layoutInflater.inflate(R.layout.list_footer, null), null, false);
		mCastView.setEmptyView(findViewById(R.id.empty2));
		mRefresh = (RefreshButton) findViewById(R.id.refresh);
		mRefresh.setOnClickListener(this);

		final View addCast = findViewById(R.id.add_cast);
		addCast.setOnClickListener(this);
		if (Constants.CAN_CREATE_CASTS) {
			addCast.setVisibility(View.VISIBLE);
		}
		mCastView.setOnItemClickListener(this);

		mCastView.setAdapter(null);
		registerForContextMenu(mCastView);

		if (USE_MAP) {
			mMapView = (MapView) findViewById(R.id.map);
			mMapController = mMapView.getController();
		} else {
			final View map = findViewById(R.id.map_container);
			if (map != null) {
				map.setVisibility(View.GONE);
			}
		}

		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (Intent.ACTION_VIEW.equals(action)) {
			mUri = intent.getData();

			mCastsUri = Itinerary.getCastsUri(mUri);

			final LoaderManager lm = getSupportLoaderManager();
			Bundle args = new Bundle();
			args.putParcelable(LOADER_ARG_DATA, mUri);
			lm.initLoader(LOADER_ITINERARY, args, this);

			args = new Bundle();
			args.putParcelable(LOADER_ARG_DATA, mCastsUri);
			lm.initLoader(LOADER_CASTS, args, this);

			initCastList();

		} else {
			finish();
		}
	}

	@Override
	protected void onResume() {
		mFirstLoadSync = true;
		super.onResume();
		mSyncHandle = ContentResolver.addStatusChangeListener(0xff, new LocastSyncStatusObserver(
				this, mHandler));
		LocastSyncStatusObserver.notifySyncStatusToHandler(this, mHandler);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSyncHandle != null) {
			ContentResolver.removeStatusChangeListener(mSyncHandle);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		// only enable this menu if we can actually edit things.
		if (Constants.CAN_CREATE_CASTS) {
			AdapterView.AdapterContextMenuInfo info;
			try {
				info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			} catch (final ClassCastException e) {
				Log.e(TAG, "bad menuInfo", e);
				return;
			}

			// XXX the "- 1" below is due to having a header. I'm not sure where this is supposed to
			// be handled.
			final Cursor c = (Cursor) mCastAdapter.getItem(info.position - 1);
			if (c == null) {
				return;
			}

			// load the base menus.
			final MenuInflater menuInflater = getMenuInflater();
			menuInflater.inflate(R.menu.cast_context, menu);
			menuInflater.inflate(R.menu.cast_options, menu);

			menu.setHeaderTitle(c.getString(c.getColumnIndex(Cast._TITLE)));

			final boolean canEdit = Cast.canEdit(this, c);
			menu.findItem(R.id.cast_edit).setVisible(canEdit);
			menu.findItem(R.id.cast_delete).setVisible(canEdit);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (final ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		final Uri cast = Cast.getCanonicalUri(this, ContentUris.withAppendedId(mCastsUri, info.id));

		switch (item.getItemId()) {
			case R.id.cast_view:
				startActivity(new Intent(Intent.ACTION_VIEW, cast));
				return true;

			case R.id.cast_edit:
				startActivity(new Intent(Intent.ACTION_EDIT, cast));
				return true;

			case R.id.cast_delete:
				startActivity(new Intent(Intent.ACTION_DELETE, cast));
				return true;

				// case R.id.cast_play:
				// startActivity(new Intent(CastDetailsActivity.ACTION_PLAY_CAST, cast));
				// return true;

			default:
				return super.onContextItemSelected(item);
		}
	}

	private void initCastList() {

		mCastAdapter = new CastCursorAdapter(ItineraryDetail.this, null);
		mCastView.setAdapter(new ImageLoaderAdapter(this, mCastAdapter, mImageCache,
				new int[] { R.id.media_thumbnail }, 48, 48, ImageLoaderAdapter.UNIT_DIP));

		if (USE_MAP) {

			mCastsOverlay = new CastsIconOverlay(ItineraryDetail.this);
			mPathOverlay = new PathOverlay(this);

			final List<Overlay> overlays = mMapView.getOverlays();
			overlays.add(mPathOverlay);
			overlays.add(mCastsOverlay);
		}
	}

	private void refresh(boolean explicitSync) {
		final Bundle extras = new Bundle();
		if (mItineraryCastCount == UNKNOWN_COUNT
				|| (mItineraryCastCount != mCastAdapter.getCount())) {
			extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		}

		final Bundle extras2 = new Bundle();
		// we always deprioritize this so that the casts will take priority.
		extras2.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
		LocastSyncService.startSync(this, mUri, explicitSync, extras2);
		LocastSyncService.startSync(this, mCastsUri, explicitSync, extras);

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

		final Cursor cast = (Cursor) adapter.getItemAtPosition(position);
		final int dratCol = cast.getColumnIndex(Cast._DRAFT);
		final boolean isDraft = !cast.isNull(dratCol) && cast.getInt(dratCol) == 1;

		if (isDraft) {
			startActivity(new Intent(Intent.ACTION_EDIT, ContentUris.withAppendedId(mCastsUri, id)));
		} else {
			startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mCastsUri, id)));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.refresh:
				refresh(true);
				break;

			case R.id.home:
				startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName()));
				break;

			case R.id.add_cast:
				startActivity(new Intent(Intent.ACTION_INSERT, Itinerary.getCastsUri(getIntent()
						.getData())));
				break;
		}
	}

	private static final int LOADER_ITINERARY = 0, LOADER_CASTS = 1;
	private static final String LOADER_ARG_DATA = "edu.mit.mobile.android.locast.LOADER_ARG_DATA";

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri uri = args.getParcelable(LOADER_ARG_DATA);

		CursorLoader cl = null;

		switch (id) {
			case LOADER_ITINERARY:
				cl = new CursorLoader(this, uri, ITINERARY_PROJECTION, null, null, null);
				break;

			case LOADER_CASTS:
				cl = new CursorLoader(this, uri, CastCursorAdapter.DEFAULT_PROJECTION, null, null,
						Cast.SORT_ORDER_DEFAULT);
				break;

		}

		cl.setUpdateThrottle(Constants.UPDATE_THROTTLE);

		return cl;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
			case LOADER_ITINERARY: {
				if (c.moveToFirst()) {
					mItineraryCastCount = c.getInt(c.getColumnIndex(Itinerary._CASTS_COUNT));
					final String description = c
							.getString(c.getColumnIndex(Itinerary._DESCRIPTION));
					((TextView) findViewById(R.id.description)).setText(description);
					((TextView) findViewById(R.id.description_empty)).setText(description);

					((TextView) findViewById(android.R.id.title)).setText(c.getString(c
							.getColumnIndex(Itinerary._TITLE)));

					if (USE_MAP) {
						final List<GeoPoint> path = Itinerary.getPath(c);
						mPathOverlay.setPath(path);

						if (!path.isEmpty()) {
							mMapController.setCenter(mPathOverlay.getCenter());
						}

						if (Constants.USES_OSMDROID) {
							// this needs to be run after the MapView has been first sized due to a
							// bug in zoomToSpan()
							mMapView.post(new Runnable() {

								@Override
								public void run() {
									if (mMapView.getHeight() > 0) {
										mMapController.zoomToSpan(mPathOverlay.getLatSpanE6(),
												mPathOverlay.getLonSpanE6());
									} else {
										mMapView.post(this);
									}
								}
							});
						} else {
							mMapController.zoomToSpan(mPathOverlay.getLatSpanE6(),
									mPathOverlay.getLonSpanE6());
						}

						mMapView.setVisibility(View.VISIBLE);
					}
				} else {
					Log.e(TAG, "error loading itinerary");
				}

			}
				break;

			case LOADER_CASTS: {
				mCastAdapter.swapCursor(c);
				if (USE_MAP) {
					mCastsOverlay.swapCursor(c);
				}
				// this is done after the casts are loaded so that an expedited sync can be
				// requested if the list is empty.
				if (mFirstLoadSync) {
					refresh(false);
					mFirstLoadSync = false;
				}
			}
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
			case LOADER_CASTS:
				mCastAdapter.swapCursor(null);
				if (USE_MAP) {
					mCastsOverlay.swapCursor(null);
				}
				break;

			case LOADER_ITINERARY:

				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.itinerary_detail, menu);
		if (Constants.CAN_CREATE_CASTS) {
			menu.findItem(R.id.add_cast).setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_cast:
				startActivity(new Intent(Intent.ACTION_INSERT, Itinerary.getCastsUri(getIntent()
						.getData())));
				return true;

			case R.id.refresh:
				refresh(true);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
