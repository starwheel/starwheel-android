package net.omplanet.starwheel.ui.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
 
/**
 * A Drawable that draws an oval with given {@link Bitmap}
 */
public class RoundedAvatarDrawable extends Drawable {
  private final Bitmap mBitmap;
  private final Paint mPaint;
  private final RectF mRectF;
  private final int mBitmapWidth;
  private final int mBitmapHeight;
 
  public RoundedAvatarDrawable(Bitmap bitmap) {
    mBitmap = bitmap;
    mRectF = new RectF();
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    mPaint.setShader(shader);
 
    // NOTE: we assume bitmap is properly scaled to current density
    mBitmapWidth = mBitmap.getWidth();
    mBitmapHeight = mBitmap.getHeight();
  }
  
  public static Bitmap getCroppedBitmap(Bitmap bitmap) {
	    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	            bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	    canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
	            bitmap.getWidth() / 2, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	    //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
	    //return _bmp;
	    return output;
	}
 
  @Override
  public void draw(Canvas canvas) {
    canvas.drawOval(mRectF, mPaint);
  }
 
  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
 
    mRectF.set(bounds);
  }
 
  @Override
  public void setAlpha(int alpha) {
    if (mPaint.getAlpha() != alpha) {
      mPaint.setAlpha(alpha);
      invalidateSelf();
    }
  }
 
  @Override
  public void setColorFilter(ColorFilter cf) {
    mPaint.setColorFilter(cf);
  }
 
  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
 
  @Override
  public int getIntrinsicWidth() {
    return mBitmapWidth;
  }
 
  @Override
  public int getIntrinsicHeight() {
    return mBitmapHeight;
  }
 
  public void setAntiAlias(boolean aa) {
    mPaint.setAntiAlias(aa);
    invalidateSelf();
  }
 
  @Override
  public void setFilterBitmap(boolean filter) {
    mPaint.setFilterBitmap(filter);
    invalidateSelf();
  }
 
  @Override
  public void setDither(boolean dither) {
    mPaint.setDither(dither);
    invalidateSelf();
  }
 
  public Bitmap getBitmap() {
    return mBitmap;
  }
 
  // TODO allow set and use target density, mutate, constant state, changing configurations, etc.
}