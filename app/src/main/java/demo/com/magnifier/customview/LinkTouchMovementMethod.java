package demo.com.magnifier.customview;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by eric on 15/4/21.
 */
public class LinkTouchMovementMethod extends LinkMovementMethod {

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new LinkTouchMovementMethod();

        return sInstance;
    }

    private static LinkTouchMovementMethod sInstance;


    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();

        x += widget.getScrollX();
        y += widget.getScrollY();

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        TouchableSpan[] link = buffer.getSpans(off, off, TouchableSpan.class);

        if (link.length != 0) {
            if (action == MotionEvent.ACTION_UP) {
                //link[0].onTouch(widget,event); //////// CHANGED HERE
                Selection.removeSelection(buffer);
            } else {
                Log.d("zhangzhenhui", "test");
                link[0].onTouch(widget, event); //////// ADDED THIS
                Selection.setSelection(buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0]));
            }

            return true;
        } else {
            Selection.removeSelection(buffer);
        }

        return super.onTouchEvent(widget, buffer, event);
    }

}
