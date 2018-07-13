package com.onedriver;

import com.onedrive.sdk.extensions.Item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A item representing a piece of content from OneDrive.
 */
class DisplayItem {

    /**
     * The item factory that created this item
     */
    private final LruCache<String, Bitmap> mImageCache;

    /**
     * The actual backing item instance
     */
    private final Item mItem;

    /**
     * The id for this display item
     */
    private final String mId;

    /**
     * The task to retrieve the thumbnail for this item
     */
    private final AsyncTask<Void, Void, Bitmap> mGetThumbnailTask;

    /**
     * Default Constructor
     *
     * @param item The Item
     * @param id   The internal id for the item
     * @param imageCache The thumbnail image cache
     */
    public DisplayItem(final Item item,
                       final String id,
                       final LruCache<String, Bitmap> imageCache) {
        mImageCache = imageCache;
        mItem = item;
        mId = id;

        if (hasThumbnail()) {
            mGetThumbnailTask = new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(final Void... params) {
                    return null;
                }

                @Override
                protected void onPostExecute(final Bitmap image) {
                    if (image != null) {
                    }
                }
            };
//            mGetThumbnailTask.execute();
        } else {
            mGetThumbnailTask = null;
        }
    }

    /**
     * The item id
     * @return the item id
     */
    public String getId() {
        return mId;
    }

    /**
     * The backing item instance
     * @return The item instance
     */
    public Item getItem() {
        return mItem;
    }

    /**
     * Stops attempting to download this thumbnail
     */
    public void cancelThumbnailDownload() {
        if (mGetThumbnailTask != null && mGetThumbnailTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetThumbnailTask.cancel(false);
        }
    }

    /**
     * Resume downloading this thumbnail if it hasn't been retrieved already
     */
    public void resumeThumbnailDownload() {
        if (mGetThumbnailTask != null && mGetThumbnailTask.getStatus() == AsyncTask.Status.PENDING) {
            mGetThumbnailTask.execute((Void)null);
        }
    }

    /**
     * Determine if an item has a thumbnail used for visualization
     * @return If the item has a thumbnail
     */
    private boolean hasThumbnail() {
        return mItem.thumbnails != null
               && mItem.thumbnails.getCurrentPage() != null
               && !mItem.thumbnails.getCurrentPage().isEmpty()
               && mItem.thumbnails.getCurrentPage().get(0).small != null
               && mItem.thumbnails.getCurrentPage().get(0).small.url != null;
    }

    /**
     * Gets a list of the facets on this item
     * @return The list of facets
     */
    public String  getTypeFacets() {
        final List<String> typeFacets = new LinkedList<>();
        if (mItem.folder != null) {
            typeFacets.add(mItem.folder.getClass().getSimpleName());
        }
        if (mItem.file != null) {
            typeFacets.add(mItem.file.getClass().getSimpleName());
        }
        if (mItem.audio != null) {
            typeFacets.add(mItem.audio.getClass().getSimpleName());
        }
        if (mItem.image != null) {
            typeFacets.add(mItem.image.getClass().getSimpleName());
        }
        if (mItem.photo != null) {
            typeFacets.add(mItem.photo.getClass().getSimpleName());
        }
        if (mItem.specialFolder != null) {
            typeFacets.add(mItem.specialFolder.getClass().getSimpleName());
        }
        if (mItem.video != null) {
            typeFacets.add(mItem.video.getClass().getSimpleName());
        }
        final String joiner = ", ";
        final StringBuilder sb = new StringBuilder();
        for (final String facet : typeFacets) {
            sb.append(facet);
            sb.append(joiner);
        }

        final int lastIndexOfJoiner = sb.lastIndexOf(joiner);
        if (lastIndexOfJoiner != -1) {
            sb.delete(lastIndexOfJoiner, sb.length());
        }

        return sb.toString();
    }

    /**
     * ToString() implementation
     *
     * @return The name of the item
     */
    @Override
    public String toString() {
        return mItem.name;
    }

    /**
     * The image for this display item
     * @return The image, or null if non was found
     */
    public Bitmap getImage() {
        if (hasThumbnail()) {
            return mImageCache.get(mId);
        }
        return null;
    }

}
