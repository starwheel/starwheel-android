package net.omplanet.starwheel.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class CircleTextView extends View {
    //Given params
    private final int mPosition;
    private final boolean mTextOffset;
    private String mText;
    private final int mTextColor;
    private final int mCircleWidth;
    private final int mCircleColor;
    private final float mCircleAlpha;

    //Init params
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);;
    private final Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);;
    private float mCircleRadius;
    private Path mTextArc;
    private RectF mTextRect;

    public CircleTextView(int position, boolean offset, String text, int textColor, int circleWidth, int circleColor, float circleAlpha, Context context) {
        super(context);

        mPosition = position;
        mTextOffset = offset;
        mText = text;
        mTextColor = textColor;
        mCircleWidth = circleWidth;
        mCircleColor = circleColor;
        mCircleAlpha = circleAlpha;

        setFocusable(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setup();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int x = canvas.getWidth()/2; //Horizontal center of canvas view
        int y = canvas.getHeight()/2; //Vertical center of canvas view

        if(mCircleWidth > 0) {
            canvas.drawCircle(x, y, mCircleRadius, mCirclePaint);
        }
        if(mText != null && mText.length() > 0) {
//            if(mPosition == 0 || mPosition == 3 || mPosition == 4) {
//                //Rotate the text upside down
//                mTextArc.quadTo(0,0,100,100);
//            }
            //canvas.drawArc(mTextRect, -180 + (mPosition * 60) % 360, 120, false, mTextPaint);
            canvas.drawTextOnPath(mText, mTextArc, 0, 0, mTextPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    private void setup() {
        //Prepare the circle
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStrokeWidth(mCircleWidth);
        mCirclePaint.setAlpha((int) (mCircleAlpha*255));
        mCirclePaint.setPathEffect(new DashPathEffect(new float[]{2, 2}, 0));

        RectF mCircleRect = new RectF();
        mCircleRect.set(0, 0, getWidth(), getHeight());
        mCircleRadius = Math.min((mCircleRect.height() - mCircleWidth) / 2, (mCircleRect.width() - mCircleWidth) / 2);

        //Prepare the text
        if(mText != null && mText.length() > 0) {
            mText = mText.length() > 20 ? mText.substring(0, 20)+"..." : mText;
            int fontSize = mText.length() < 16 ? (int) mCircleRect.width()/10 : (int) mCircleRect.width()/13;

            mTextRect = new RectF();
            if(mTextOffset) {
                mTextRect.set(mCircleWidth + mCircleRadius/2, mCircleWidth + mCircleRadius/2, mCircleRect.width() - mCircleWidth - mCircleRadius/2, mCircleRect.height() - mCircleWidth - mCircleRadius/2);
            } else {
                mTextRect.set(mCircleWidth + fontSize, mCircleWidth + fontSize, mCircleRect.width() - mCircleWidth - fontSize, mCircleRect.height() - mCircleWidth - fontSize);
            }
            //float mTextRadius = Math.min(mTextRect.height() / 2, mTextRect.width() / 2);

            mTextArc = new Path();
            if(mPosition == 0) {
                mTextArc.addArc(mTextRect, -180, 180);
            } else {
                mTextArc.addArc(mTextRect, -140 + (mPosition*60)%360, 100);
            }
//            mTextPaint.setStyle(Paint.Style.STROKE);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setColor(mTextColor);
            mTextPaint.setTextSize(fontSize);
        }
    }
}