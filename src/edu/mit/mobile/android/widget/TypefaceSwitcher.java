package edu.mit.mobile.android.widget;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;

public class TypefaceSwitcher {

	private static HashMap<String, WeakReference<Typeface>> mTypeFaces = new HashMap<String, WeakReference<Typeface>>();

	private static Typeface getTypeface(Context context, String typefaceAsset) {
		final WeakReference<Typeface> typefaceRef = mTypeFaces.get(typefaceAsset);
		Typeface typeface = null;
		if (typefaceRef != null) {
			typeface = typefaceRef.get();
		}
		if (typeface == null) {
			typeface = Typeface.createFromAsset(context.getAssets(), typefaceAsset);
		}

		if (typeface == null) {
			throw new IllegalArgumentException("could not get typeface for " + typefaceAsset);
		}

		return typeface;
	}

	/**
	 * Sets the typeface for a list of TextView resourceIds. The loaded typeface is cached
	 * statically using a WeakReference.
	 *
	 * @param context
	 * @param typefaceAsset
	 *            the path under your assets/ directory where the typeface lives. Typically, this is
	 *            the filename.
	 * @param parent
	 *            the parent with whom all the resourceIds are under
	 * @param resourceIds
	 *            a list of TextView resource IDs.
	 */
	public static void setTypeface(Context context, String typefaceAsset, ViewGroup parent,
			int... resourceIds) {
		final Typeface typeface = getTypeface(context, typefaceAsset);
		for (final int tvId : resourceIds) {
			((TextView) parent.findViewById(tvId)).setTypeface(typeface);
		}
	}

	public static void setTypeface(Activity context, String typefaceAsset, int... resourceIds) {
		final Typeface typeface = getTypeface(context, typefaceAsset);
		for (final int tvId : resourceIds) {
			((TextView) context.findViewById(tvId)).setTypeface(typeface);
		}
	}
}
