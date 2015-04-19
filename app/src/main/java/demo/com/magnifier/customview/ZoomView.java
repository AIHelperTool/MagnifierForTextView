package demo.com.magnifier.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import demo.com.magnifier.R;

/**
 * Created by eric on 15/4/18.
 */
public class ZoomView extends TextView {

    private static final int RADIUS = 58;

    private static final long DELAY_TIME = 200;

    private Rect srcRect;
    private Point dstPoint;

    private Bitmap resBitmap;
    private Bitmap bg;

    private PopupWindow popup;
    private Magnifier magnifier;

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        magnifier = new Magnifier(context);

        int width = getResources().getDimensionPixelSize(R.dimen.popupwindow_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popupwindow_height);
        popup = new PopupWindow(magnifier, width, height);
        popup.setAnimationStyle(android.R.style.Animation_Toast);

        srcRect = new Rect(0, 0, 4 * RADIUS, 2 * RADIUS);
        dstPoint = new Point(0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setDrawingCacheEnabled(true);
        bg = getDrawingCache();
        if (bg != null) {
            resBitmap = toBeBig(bg);
        }
        setDrawingCacheEnabled(false);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            if (resBitmap != null) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                srcRect.offsetTo(2 * x - RADIUS, 2 * y - RADIUS);
                dstPoint.set(x, y);

                if (srcRect.left < 0) {
                    srcRect.offset(-srcRect.left, 0);
                } else if (srcRect.right > resBitmap.getWidth()) {
                    srcRect.offset(resBitmap.getWidth() - srcRect.right, 0);
                }

                if (srcRect.top < 0) {
                    srcRect.offset(0, -srcRect.top);
                } else if (srcRect.bottom > resBitmap.getHeight()) {
                    srcRect.offset(0, resBitmap.getHeight() - srcRect.bottom);
                }

                if ((x < 0) || (y < 0)) {
                    // hide popup if out of bounds
                    popup.dismiss();
                    invalidate();
                    return true;
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    removeCallbacks(showZoom);
                    postDelayed(showZoom, DELAY_TIME);
                } else if (!popup.isShowing()) {
                    showZoom.run();
                }
                popup.update(getLeft() + dstPoint.x, getTop() + dstPoint.y, -1, -1);
                magnifier.invalidate();
            }

        } else if (action == MotionEvent.ACTION_UP) {
            removeCallbacks(showZoom);
            popup.dismiss();
        }
        invalidate();
        return true;
    }

    Runnable showZoom = new Runnable() {
        public void run() {
            popup.showAtLocation(ZoomView.this,
                    Gravity.NO_GRAVITY,
                    getLeft() + dstPoint.x,
                    getTop() + dstPoint.y);
        }
    };

    class Magnifier extends ImageView {
        private Paint mPaint;
        private Rect rect;

        public Magnifier(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xff008000);
            mPaint.setStyle(Style.STROKE);
            rect = new Rect(0, 0, RADIUS * 4, RADIUS * 2);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (resBitmap != null) {
                canvas.save();

                // draw popup
                mPaint.setAlpha(255);
                canvas.drawBitmap(resBitmap, srcRect, rect, mPaint);
                canvas.restore();
            }
        }
    }

    private static Bitmap toBeBig(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(2.0f,2.0f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }
}
