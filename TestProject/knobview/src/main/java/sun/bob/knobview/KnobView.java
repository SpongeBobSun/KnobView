package sun.bob.knobview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by sunkuan on 15/5/6.
 */
public class KnobView extends View {
    private Point center;
    private int radiusOut;
    private Paint paintOut;
    private OnTickListener onTickListener;
    private float startDeg = Float.NaN;
    private float rotateDeg = 0;

    private Bitmap bmp;
    private Paint bmpPaint;

    public KnobView(Context context, AttributeSet attrs) {
        super(context, attrs);
        center = new Point();
        paintOut = new Paint();
        paintOut.setColor(Color.GRAY);
        paintOut.setAntiAlias(true);
        bmpPaint = new Paint();
        bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher);
    }

    @Override
    protected void onMeasure(int measureWidthSpec,int measureHeightSpec){
        super.onMeasure(measureWidthSpec, measureHeightSpec);
        int measuredWidth = measureWidth(measureWidthSpec);
        int measuredHeight = measureHeight(measureHeightSpec);
        this.setMeasuredDimension(measuredWidth, measuredHeight);
        radiusOut = (measuredHeight - 20)/ 2;
        center.x = measuredWidth/2;
        center.y = measuredHeight/2;
    }

    /**
     * Set the knob's background.
     * If background is not specified, it will use the launcher icon as background.
     * @param bmp
     */
    public void setBackground(Bitmap bmp){
        if (bmp != null)
            this.bmp = bmp;
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawCircle(center.x,center.y,radiusOut,paintOut);
        canvas.rotate(rotateDeg,center.x,center.y);

        canvas.drawBitmap(bmp,center.x - bmp.getWidth()/2,center.y - bmp.getHeight()/2,bmpPaint);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 0;
        if (specMode == MeasureSpec.AT_MOST) {
            result = getWidth();
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 0;
        if (specMode == MeasureSpec.AT_MOST) {
            result = getWidth();
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x = event.getX() / (center.x * 2);
                float y = event.getY() / (center.y * 2);

                startDeg = xyToDegrees(x, y);
                Log.d("deg = ", "" + startDeg);
                if (Float.isNaN(startDeg)) {
                    return false;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!Float.isNaN(startDeg)) {
                    float currentDeg = xyToDegrees((event.getX() - (getWidth() - getHeight())/2) / ((float) getHeight()),
                            event.getY() / getHeight());

                    if (!Float.isNaN(currentDeg)) {
                        float degPerTick = 72f;
                        float deltaDeg = startDeg - currentDeg;
                        if(Math.abs(deltaDeg) < 72f){
                            return true;
                        }
                        int ticks = (int) (Math.signum(deltaDeg)
                                * Math.floor(Math.abs(deltaDeg) / degPerTick));
                        if(ticks == 1){
                            Log.e("Ticks","Next");
                            startDeg = currentDeg;
                            if(onTickListener !=null)
                                onTickListener.onNextTick();
                            rotateDeg += 72f;
                            this.invalidate();
                        }
                        if(ticks == -1){
                            Log.e("Ticks","Previous");
                            startDeg = currentDeg;
                            if(onTickListener !=null)
                                onTickListener.onPreviousTick();
                            rotateDeg -= 72f;
                            this.invalidate();
                        }
                    }
                    startDeg = currentDeg;
                    return true;
                } else {
                    return false;
                }

//            case MotionEvent.ACTION_UP:
//                if ((Math.pow(event.getX() - getWidth() / 2f,2) + Math.pow(event.getY() - getHeight() / 2f,2) <= radiusIn*radiusIn )){
//                    if(onButtonListener !=null)
//                        onButtonListener.onSelect();
//                    return true;
//                }
//                //TODO
//                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void setOnTickListener(OnTickListener listener){
        this.onTickListener = listener;
    }

    private float xyToDegrees(float x, float y) {
        float distanceFromCenter = PointF.length((x - 0.5f), (y - 0.5f));
        if (distanceFromCenter < 0.15f
                || distanceFromCenter > 0.5f) { // ignore center and out of bounds events
            return Float.NaN;
        } else {
            return (float) Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
        }
    }
}
