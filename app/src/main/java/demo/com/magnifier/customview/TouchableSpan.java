package demo.com.magnifier.customview;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by eric on 15/4/21.
 */
public abstract class TouchableSpan extends CharacterStyle implements UpdateAppearance {

    /**
     * Performs the touch action associated with this span.
     * @return
     */
    public abstract boolean onTouch(View widget, MotionEvent m);

    /**
     * Could make the text underlined or change link color.
     */
    @Override
    public abstract void updateDrawState(TextPaint ds);

}
