package demo.com.magnifier.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.BreakIterator;
import java.util.Locale;

import demo.com.magnifier.R;

/**
 * Created by eric on 15/4/18.
 */
public class ZoomView extends TextView {

    private static final int PADDING_SIZE = 20;

    private static final long DELAY_TIME = 200;

    private Rect srcRect;
    private Point dstPoint;

    private Bitmap magnifierBitmap;
    private Bitmap resBitmap;
    private Bitmap bg;

    private PopupWindow popup;
    private Magnifier magnifier;

    private int width;
    private int height;

    private int rectWidth;
    private int rectHeight;

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        width = getResources().getDimensionPixelSize(R.dimen.popupwindow_width);
        height = getResources().getDimensionPixelSize(R.dimen.popupwindow_height);
        rectWidth = width - PADDING_SIZE;
        rectHeight = height - 2 * PADDING_SIZE;
        BitmapDrawable magnifierDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.magnifying_glass_bg);
        magnifierBitmap = scaleBitmpa(magnifierDrawable.getBitmap(), width, height);

        magnifier = new Magnifier(context);

        popup = new PopupWindow(magnifier, width, height);
        popup.setAnimationStyle(android.R.style.Animation_Toast);

        srcRect = new Rect(0, 0, rectWidth, rectHeight);
        dstPoint = new Point(0, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        String testString = getResources().getString(R.string.test_string).trim();
        setMovementMethod(LinkTouchMovementMethod.getInstance());
        setText(testString, BufferType.SPANNABLE);
        Spannable spans = (Spannable) getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(testString);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = testString.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                TouchableSpan touchSpan = getTouchableSpan(possibleWord);
                spans.setSpan(touchSpan, start, end, 0);
            }
        }
    }

    private TouchableSpan getTouchableSpan(final String word) {
        return new TouchableSpan() {
            final String mWord;

            {
                mWord = word;
            }

            @Override
            public boolean onTouch(View widget, MotionEvent m) {
                Log.d("TouchableSpan", "touch word:" + mWord);
                Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT)
                        .show();
                return true;
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setAntiAlias(true);
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        Log.d("zhangzhenhui", "onTouchEvent");
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

                srcRect.set((int)(x*760/480-width/2), (int)(y*1030/840-height/2),
                        (int)(x*760/480+width/2), (int)(y*1030/840+height/2));

                dstPoint.set(x - width/2, y - height/2);

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

                if ((x < 0) || (y < 0) || (y > getMeasuredHeight())
                        || (x > getMeasuredWidth())) {
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
            rect = new Rect(20, 20, rectWidth, rectHeight);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            if (resBitmap != null) {
                canvas.save();
                // draw popup frame
                mPaint.setAlpha(255);
                canvas.drawBitmap(magnifierBitmap, 0, 0, mPaint);

                // draw popup
                mPaint.setAlpha(255);
                canvas.drawBitmap(resBitmap, srcRect, rect, mPaint);
                canvas.restore();
            }
        }
    }

    private Bitmap toBeBig(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1.5f, 1.5f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    private Bitmap scaleBitmpa(Bitmap bm, int reqWidth, int reqHeight) {
        int bmWidth = bm.getWidth();
        int bmHeight = bm.getHeight();

        float scaleWidth = ((float) reqWidth) / bmWidth;
        float scaleHeight = ((float) reqHeight) / bmHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, bmWidth, bmHeight, matrix,
                true);
        return newbm;
    }
}
