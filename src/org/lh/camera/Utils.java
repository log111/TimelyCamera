package org.lh.camera;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by lh on 14-1-26.
 */
public final class Utils {

    public static RectF toRectF(Rect rect){
        return new RectF(rect);
    }

    public static Rect toRect(RectF rectf){
        Rect ret = new Rect();
        ret.left = Float.valueOf(rectf.left).intValue();
        ret.top = Float.valueOf(rectf.top).intValue();
        ret.right = Float.valueOf(rectf.right).intValue();
        ret.bottom = Float.valueOf(rectf.bottom).intValue();
        return ret;
    }
}
