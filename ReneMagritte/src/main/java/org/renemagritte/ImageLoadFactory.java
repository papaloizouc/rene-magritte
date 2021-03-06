package org.renemagritte;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageLoadFactory {

    private static ImageLoadFactory factory = null;
    private static LruCache<String, Bitmap> cache;
    private static Map<Class, Map<Integer, ImageView>> viewMap;

    private ImageLoadFactory(int cacheSize) {
        cache = new LruCache<String, Bitmap>(cacheSize);
        viewMap = new ConcurrentHashMap<Class, Map<Integer, ImageView>>();
    }

    private static void initFactory(int cacheSize){
        if (factory == null) {
            factory = new ImageLoadFactory(cacheSize);
        }
    }

//    private static ImageLoadFactory getFactory() {
//        initFactory();
//        return factory;
//    }

    public static ImageLoadFactory getFactory(Map<Integer, ImageView> views, Class key, int cacheSize) {
        initFactory(cacheSize);
        viewMap.put(key, views);
        return factory;
    }

    public static void load(Class clazz, Bundle bundle){
        new LoadImageTask(viewMap.get(clazz)).execute(bundle);
    }


    public static class LoadImageTask extends AsyncTask<Bundle, Void, Bundle> {

        private final String BUNDLE_URI = "uri";
        private final String BUNDLE_POS = "pos";
        private final String BUNDLE_BM = "bm";
        private Map<Integer, ImageView> views;

        public LoadImageTask(Map<Integer, ImageView> views) {
            this.views = views;
        }

        @Override
        protected Bundle doInBackground(Bundle... bundles) {
            Bitmap bm;
            Bundle inputBundle = bundles[0];
            String path = inputBundle.getString(BUNDLE_URI);
            bm = cache.get(path);
            if (bm == null) { // not cached
                bm = decodeSampledBitmapFromUri(path, 500, 500);
                cache.put(path, bm);
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable(BUNDLE_BM, bm);
            bundle.putInt(BUNDLE_POS, inputBundle.getInt(BUNDLE_POS));
            bundle.putString(BUNDLE_URI, path);
            return bundle;
        }

        @Override
        protected void onPostExecute(Bundle result) {
            super.onPostExecute(result);
            ImageView view = views.get(result.getInt(BUNDLE_POS));
            Bitmap bm = (Bitmap) result.getParcelable(BUNDLE_BM);
            String path = result.getString(BUNDLE_URI);
            if (bm != null) {
                view.setImageBitmap(bm);
                view.setOnClickListener(new CustomOnClickListener(path));
            }

        }

        public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
            Bitmap bm;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, options);
            return bm;
        }

        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
            if (height > reqHeight || width > reqWidth) {
                if (width > height) {
                    inSampleSize = Math.round((float) height / (float) reqHeight);
                } else {
                    inSampleSize = Math.round((float) width / (float) reqWidth);
                }
            }
            return inSampleSize;
        }

        private class CustomOnClickListener implements View.OnClickListener {

            private String path;

            public CustomOnClickListener(String path) {
                this.path = path;
            }

            @Override
            public void onClick(View v) {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            Log.d("We are not sorry, nothing went wrong", " --- ");
                        } catch (Exception e) {
                            Log.e("We are sorry, Something went wrong", Log.getStackTraceString(e));
                        }
                        return null;
                    }
                }.execute(null, null, null);
            }
        }


    }

}
