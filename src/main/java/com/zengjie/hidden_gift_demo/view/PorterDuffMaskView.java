package com.zengjie.hidden_gift_demo.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;


import com.zengjie.hidden_gift_demo.R;
import com.zengjie.opensource.util.CommonUtils;

/**
 * Created by ailun on 2020/12/02.
 * 自定义View，使用PorterDuff.Mode实现遮罩移动动画
 */
public class PorterDuffMaskView extends View {
    private Paint mPaint;
    private RectF dstRect, srcRect;
    private Path mClipPath;
    private Path mClipPath2;

    private final int mPadding = CommonUtils.dip2px(getContext(),82); //间距
    private int mMaskLeft = mPadding; //遮罩左边坐标
    private int mMaskTop = CommonUtils.dip2px(getContext(),332);  //遮罩顶部坐标
    private ValueAnimator mAnim;
    private int mAnimValue = 0;
    private int mRoundCorner = CommonUtils.dip2px(getContext(),25); //圆角矩形的角度
    private final int BEVEL_EDGE_MARGIN = CommonUtils.dip2px(getContext(), 30);
    private Xfermode mXfermode;

    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.SRC_IN;
    private int mWidth;
    private int mHeight;
    private int mMaskRight;
    private final int MASK_HEIGHT = CommonUtils.dip2px(getContext(), 42);
    private Bitmap mSrcBitmap;

    public PorterDuffMaskView(Context context) {
        super(context);
        init();
    }

    public PorterDuffMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PorterDuffMaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mWidth = CommonUtils.getScreenWidth(getContext());
        mHeight = CommonUtils.getScreenHeight(getContext());
        mClipPath = new Path();
        mClipPath2 = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        mXfermode = new PorterDuffXfermode(mPorterDuffMode);
        mSrcBitmap = createBitmap(R.drawable.kl_bg_gift_double);
        //开启View级别的离屏缓冲,并关闭硬件加速，使用软件绘制
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        startAnim();
    }

    private Bitmap createBitmap(int resId) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), resId);
            Log.d("zengjie",
                    "width = " + bitmap.getWidth() + ",height = " + bitmap.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //重置Path
        mClipPath.reset();
        mClipPath2.reset();
        //矩形遮罩区域
        mClipPath.addRect(mMaskLeft, mMaskTop, mMaskRight
                , mMaskTop + MASK_HEIGHT, /*mRoundCorner, 0,*/ Path.Direction.CCW);
        //设置三角形区域
        mClipPath2.moveTo(mAnimValue, mMaskTop);
        mClipPath2.lineTo(mAnimValue, mMaskTop + MASK_HEIGHT);
        mClipPath2.lineTo(mAnimValue - BEVEL_EDGE_MARGIN, mMaskTop + MASK_HEIGHT);
        mClipPath2.close();
        //裁切三角形
        mClipPath.op(mClipPath2,Path.Op.DIFFERENCE);
        //绘制遮罩
        canvas.drawPath(mClipPath,mPaint);

        //设置paint的 xfermode
        mPaint.setXfermode(mXfermode);
        //画遮罩的矩形(Source image)
        canvas.drawBitmap(mSrcBitmap,mPadding,mMaskTop,mPaint);
        //清空paint 的 xfermode
        mPaint.setXfermode(null);
        canvas.restore();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = w <= h ? w : h;
        int centerX = w/2;
        int centerY = h/2;
        int quarterWidth = width /4;
        //在中心区域截取矩形区域
        srcRect = new RectF(centerX-quarterWidth, centerY-quarterWidth, centerX+quarterWidth, centerY+quarterWidth);
        dstRect = new RectF(centerX-quarterWidth, centerY-quarterWidth, centerX+quarterWidth, centerY+quarterWidth);
        Log.d("maskview","onSizeChanged .");
    }



    private void startAnim() {
        stopAnim();
        mAnim = ValueAnimator.ofInt(mPadding, mWidth - mPadding + BEVEL_EDGE_MARGIN);
        if (mAnim != null) {
            mAnim.setDuration(1600);
            mAnim.setInterpolator(new LinearInterpolator());
            mAnim.setRepeatCount(0);
            mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mAnimValue = (int)animation.getAnimatedValue();
                    if (mAnimValue <= mWidth - mPadding){
                        mMaskRight = mAnimValue;
                    } else {
                        mMaskRight = mWidth - mPadding;
                    }
                   invalidate();
                }
            });

            mAnim.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mAnim.start();
        }
    }

    private void stopAnim() {
        //bugly #186107
        if (mAnim != null && mAnim.isRunning()) {
            mAnim.removeAllListeners();
            mAnim.removeAllUpdateListeners();
            mAnim.cancel();
            mAnim = null;
            mAnimValue = 0;
        }
    }

}
