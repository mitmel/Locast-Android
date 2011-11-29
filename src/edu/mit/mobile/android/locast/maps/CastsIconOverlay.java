package edu.mit.mobile.android.locast.maps;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.stackoverflow.ArrayUtils;

import edu.mit.mobile.android.locast.data.Cast;
import edu.mit.mobile.android.locast.ver2.R;
import edu.mit.mobile.android.locast.ver2.itineraries.LocatableItemIconOverlay;

public class CastsIconOverlay extends LocatableItemIconOverlay {
	
	private int mOfficialCol, mTitleCol, mDescriptionCol, mIdCol;
	private final Drawable mOfficialCastDrawable;
	private final Drawable mCommunityCastDrawable;

	public static final String[] CASTS_OVERLAY_PROJECTION = ArrayUtils.concat(
			LOCATABLE_ITEM_PROJECTION, new String[] { Cast._TITLE,
					Cast._DESCRIPTION, Cast._OFFICIAL });

	public CastsIconOverlay(Context context, ItemizedIconOverlay.OnItemGestureListener<OverlayItem> onGeastureListener) {
		super(context.getResources().getDrawable(R.drawable.ic_map_community), onGeastureListener, new DefaultResourceProxyImpl(context));
		final Resources res = context.getResources();
		mOfficialCastDrawable = boundCenterBottom(res.getDrawable(R.drawable.ic_map_official));
		mCommunityCastDrawable = boundCenterBottom(res.getDrawable(R.drawable.ic_map_community));
	}

	@Override
	protected void updateCursorCols() {
		super.updateCursorCols();
		if (mLocatableItems != null) {
			mIdCol = mLocatableItems.getColumnIndex(Cast._ID);
			mTitleCol = mLocatableItems.getColumnIndex(Cast._TITLE);
			mDescriptionCol = mLocatableItems.getColumnIndex(Cast._DESCRIPTION);
			mOfficialCol = mLocatableItems.getColumnIndex(Cast._OFFICIAL);
		}
	}

	@Override
	protected OverlayItem createItem(int i) {
		mLocatableItems.moveToPosition(i);

		final OverlayItem item = new OverlayItem(
				mLocatableItems.getString(mIdCol),
				mLocatableItems.getString(mTitleCol),
				mLocatableItems.getString(mDescriptionCol),
				getItemLocation(mLocatableItems));

		if (mLocatableItems.getInt(mOfficialCol) != 0) {
			item.setMarker(mOfficialCastDrawable);
		} else {
			item.setMarker(mCommunityCastDrawable);
		}
		
		mItemList.add(item);
		return item;
	}	

	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, MapView mapView) {
		// TODO Auto-generated method stub
		return false;
	}	
}
