package yberg.intnet.com.app.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Viktor on 2016-03-31.
 */

public class BitmapHandler extends AsyncTask<Void, Void, Void> {

    public static int THUMB_HEIGHT      = 128;
    public static int IMAGE_QUALITY     = 70;  // 0 - 100
    public static int MAX_WIDTH         = 960;
    public static int MAX_HEIGHT        = 540;

    private OnPostExecuteListener mListener;

    private Bitmap bitmap;
    private String encodedImage;

    public BitmapHandler(OnPostExecuteListener listener) {
        mListener = listener;
    }

    public void process(Bitmap bitmap) {
        this.bitmap = getCompressedBitmap(bitmap);
        execute();
    }

    public Bitmap getThumbnail(Bitmap bitmap) {
        return ThumbnailUtils.extractThumbnail(bitmap,
                (int) ((bitmap.getWidth() / (double) bitmap.getHeight()) * THUMB_HEIGHT), THUMB_HEIGHT);
    }

    public Bitmap getCompressedBitmap(Bitmap bitmap) {
        if (bitmap.getWidth() > MAX_WIDTH || bitmap.getHeight() > MAX_HEIGHT) {
            float scale = Math.min(((float) MAX_WIDTH / bitmap.getWidth()),
                    ((float) MAX_HEIGHT / bitmap.getHeight()));
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap resizedImage = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return resizedImage;
        }
        return bitmap;
    }

    @Override
    protected Void doInBackground(Void... params) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream);

        byte[] array = stream.toByteArray();
        encodedImage = Base64.encodeToString(array, 0);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onPostExecute(encodedImage);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(String encodedImage);
    }

}
