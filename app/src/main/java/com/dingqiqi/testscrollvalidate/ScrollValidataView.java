package com.dingqiqi.testscrollvalidate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dingqiqi on 2016/12/7.
 */
public class ScrollValidataView extends View {
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 滑块画笔
     */
    private Paint mScrollPaint;
    /**
     * 验证块画笔
     */
    private Paint mValidatePaint;
    /**
     * 背景图片
     */
    private Bitmap mBitmap;
    /**
     * 背景图片放大缩小
     */
    private Matrix mMatrix;
    /**
     * 验证块宽高
     */
    private int mValidateWidth;
    /**
     * 验证快圆半径
     */
    private int mValidateRadius;
    /**
     * 滑块高度
     */
    private float mScrollHeight;
    /**
     * 图片滑块间距
     */
    private float mScrollMargin;
    /**
     * 滑块区域
     */
    private Path mScrollPath;
    /**
     * 验证块区域
     */
    private Path mValidatePath;
    /**
     * 验证快圆方向   0 外  1 内  其他不画
     */
    private int mLeftFlag;
    private int mTopFlag;
    private int mRightFlag;
    private int mBottomFlag;
    /**
     * 图片初始位置
     */
    private int x;
    private int y;
    /**
     * 滑块左边距离
     */
    private int mLeftMargin;
    /**
     * 滑块盖区域
     */
    private RectF mScrollRect;
    /**
     * 是否画验证块
     */
    private boolean mIsDraw = false;
    /**
     * 是否获取验证块数据
     */
    private boolean mIsInvalidate = true;
    /**
     * 按下的点
     */
    private int mDownX;
    /**
     * 验证块bitmap
     */
    private Bitmap mValidateBitmap;
    /**
     * 验证结果回调
     */
    private ValidateCallBack mCallBack;
    /**
     * 验证成功闪光
     */
    private LinearGradient mLinearGradient;
    /**
     * 是否验证成功
     */
    private boolean mIsSuccess = false;
    /**
     * 闪光画笔
     */
    private Paint mPaint;
    /**
     * 是否在验证
     */
    private boolean mIsTouch = false;

    public ScrollValidataView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public ScrollValidataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    /**
     * 初始化变量
     */
    private void initView() {
        mValidatePaint = new Paint();
        mValidatePaint.setStyle(Paint.Style.FILL);
        mValidatePaint.setAntiAlias(true);

        mScrollPaint = new Paint();
        mScrollPaint.setStyle(Paint.Style.FILL);
        mScrollPaint.setAntiAlias(true);
        mScrollPaint.setColor(Color.GRAY);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(dp2px(10));

        mValidatePath = new Path();

        mScrollRect = new RectF();

        //滑块与图片间距
        mScrollMargin = dp2px(10);
        //滑动高度
        mScrollHeight = dp2px(40);
        //验证块宽度
        mValidateWidth = dp2px(60);
    }

    /**
     * 滑块回调
     *
     * @param callBack
     */
    public void setCallBack(ValidateCallBack callBack) {
        this.mCallBack = callBack;
    }

    /**
     * 设置滑块图片
     *
     * @param bitmap
     */
    public void setBackground(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    /**
     * 初始化滑块区域
     */
    public void initScrollRect() {
        mScrollPath = new Path();
        //左半圈
        RectF rectFLeft = new RectF(0, mBitmap.getHeight() + mScrollMargin, mScrollHeight, mBitmap.getHeight() + mScrollMargin + mScrollHeight);
        mScrollPath.addArc(rectFLeft, 90, 180);
        //右半圈
        RectF rectFRight = new RectF(getMeasuredWidth() - mScrollHeight, mBitmap.getHeight() + mScrollMargin, getMeasuredWidth(), mBitmap.getHeight() + mScrollMargin + mScrollHeight);
        mScrollPath.arcTo(rectFRight, 270, 180);
    }

    /**
     * 设置验证快圆圈方向
     *
     * @param left   0  外圈 1内圈 其他 不画圈
     * @param top    0  外圈 1内圈 其他 不画圈
     * @param right  0  外圈 1内圈 其他 不画圈
     * @param bottom 0  外圈 1内圈 其他 不画圈
     */
    public void setCircleOrientation(int left, int top, int right, int bottom) {
        mLeftFlag = left;
        mTopFlag = top;
        mRightFlag = right;
        mBottomFlag = bottom;
        invalidate();
    }

    /**
     * 随机验证块位置（形状有，但是扣不出图片）
     */
    private void randomValidateXY() {
        mValidatePath.reset();
        //滑块范围 3 - 9
        x = (int) (Math.random() * 7 + 3);
        y = (int) (Math.random() * 7 + 3);
        //设置范围 0.1-0.9
        x = (int) (mBitmap.getWidth() * x * 0.1f);
        y = (int) (mBitmap.getHeight() * y * 0.1f);

        mValidateRadius = mValidateWidth / 5;

        //不能超出0.9
        if (x + mValidateWidth + mValidateRadius > mBitmap.getWidth() * 0.9f) {
            x = x - mValidateWidth - mValidateRadius;
        }

        //不能超出0.9
        if (y + mValidateWidth + mValidateRadius > mBitmap.getHeight() * 0.9f) {
            y = y - mValidateWidth - mValidateRadius;
        }

        mValidatePath.moveTo(x + mValidateRadius, y + mValidateRadius * 3 + mValidateWidth / 2);

        //画左边半圆
        RectF leftRectF = new RectF(x, y + mValidateWidth / 2, x + mValidateRadius * 2, y + mValidateWidth / 2 + mValidateRadius * 2);
        if (mLeftFlag == 0) {
            mValidatePath.addArc(leftRectF, 90, 180);
        } else if (mLeftFlag == 1) {
            mValidatePath.addArc(leftRectF, 90, -180);
        }
        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius, y + mValidateRadius);
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth / 2 - mValidateRadius, y + mValidateRadius);

        //画上边半圆
        RectF topRectF = new RectF(x + mValidateWidth / 2, y, x + mValidateWidth / 2 + mValidateRadius * 2, y + mValidateRadius * 2);
        if (mTopFlag == 0) {
            mValidatePath.addArc(topRectF, 180, 180);
        } else if (mTopFlag == 1) {
            mValidatePath.addArc(topRectF, 180, -180);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth, y + mValidateRadius);
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth, y + mValidateRadius + mValidateWidth / 2 - mValidateRadius);

        //画右边半圆
        RectF rightRectF = new RectF(x + mValidateWidth, y + mValidateWidth / 2, x + mValidateWidth + mValidateRadius * 2, y + mValidateWidth / 2 + mValidateRadius * 2);
        if (mRightFlag == 0) {
            mValidatePath.addArc(rightRectF, 270, 180);
        } else if (mRightFlag == 1) {
            mValidatePath.addArc(rightRectF, 270, -180);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth, y + mValidateRadius + mValidateWidth);
        mValidatePath.lineTo(x + 2 * mValidateRadius + mValidateWidth / 2, y + mValidateRadius + mValidateWidth);

        //画下边半圆
        RectF bottomRectF = new RectF(x + mValidateWidth / 2, y + mValidateWidth, x + mValidateWidth / 2 + mValidateRadius * 2, y + mValidateWidth + mValidateRadius * 2);
        if (mBottomFlag == 0) {
            mValidatePath.addArc(bottomRectF, 0, 180);
        } else if (mBottomFlag == 1) {
            mValidatePath.addArc(bottomRectF, 0, -180);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius, y + mValidateRadius + mValidateWidth);
        mValidatePath.lineTo(x + mValidateRadius, y + 2 * mValidateRadius + mValidateWidth / 2);
    }

    /**
     * 随机验证块位置（贝塞尔曲线）
     */
    private void randomValidateXY1() {
        mValidatePath.reset();
        //滑块范围 3 - 9
        x = (int) (Math.random() * 7 + 3);
        y = (int) (Math.random() * 7 + 3);
        //设置范围 0.1-0.9
        x = (int) (mBitmap.getWidth() * x * 0.1f);
        y = (int) (mBitmap.getHeight() * y * 0.1f);

        mValidateRadius = mValidateWidth / 5;

        //不能超出0.9
        if (x + mValidateWidth + mValidateRadius > mBitmap.getWidth() * 0.9f) {
            x = x - mValidateWidth - mValidateRadius;
        }

        //不能超出0.9
        if (y + mValidateWidth + mValidateRadius > mBitmap.getHeight() * 0.9f) {
            y = y - mValidateWidth - mValidateRadius;
        }
        //移动到初始位置
        mValidatePath.moveTo(x + mValidateRadius, y + mValidateRadius * 2 + mValidateWidth / 2);

        //画左边半圆
        if (mLeftFlag == 0) {
            mValidatePath.quadTo(x - mValidateRadius, y + mValidateRadius + mValidateWidth / 2, x + mValidateRadius, y + mValidateWidth / 2);
        } else if (mLeftFlag == 1) {
            mValidatePath.quadTo(x + mValidateRadius * 3, y + mValidateRadius + mValidateWidth / 2, x + mValidateRadius, y + mValidateWidth / 2);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius, y + mValidateRadius);
        mValidatePath.lineTo(x + mValidateWidth / 2, y + mValidateRadius);

        //画上边半圆
        if (mTopFlag == 0) {
            mValidatePath.quadTo(x + mValidateWidth / 2 + mValidateRadius, y - mValidateRadius, x + mValidateWidth / 2 + mValidateRadius * 2, y + mValidateRadius);
        } else if (mTopFlag == 1) {
            mValidatePath.quadTo(x + mValidateWidth / 2 + mValidateRadius, y + 3 * mValidateRadius, x + mValidateWidth / 2 + mValidateRadius * 2, y + mValidateRadius);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth, y + mValidateRadius);
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth, y + mValidateWidth / 2);

        //画右边半圆
        if (mRightFlag == 0) {
            mValidatePath.quadTo(x + mValidateWidth + mValidateRadius * 3, y + mValidateWidth / 2 + mValidateRadius, x + mValidateRadius + mValidateWidth, y + mValidateWidth / 2 + mValidateRadius * 2);
        } else if (mRightFlag == 1) {
            mValidatePath.quadTo(x + mValidateWidth - mValidateRadius, y + mValidateWidth / 2 + mValidateRadius, x + mValidateRadius + mValidateWidth, y + mValidateWidth / 2 + mValidateRadius * 2);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius + mValidateWidth, y + mValidateRadius + mValidateWidth);
        mValidatePath.lineTo(x + 2 * mValidateRadius + mValidateWidth / 2, y + mValidateRadius + mValidateWidth);

        //画下边半圆
        if (mBottomFlag == 0) {
            mValidatePath.quadTo(x + mValidateRadius + mValidateWidth / 2, y + mValidateRadius * 3 + mValidateWidth, x + mValidateWidth / 2, y + mValidateRadius + mValidateWidth);
        } else if (mBottomFlag == 1) {
            mValidatePath.quadTo(x + mValidateRadius + mValidateWidth / 2, y - mValidateRadius + mValidateWidth, x + mValidateWidth / 2, y + mValidateRadius + mValidateWidth);
        }

        //连接半圆
        mValidatePath.lineTo(x + mValidateRadius, y + mValidateRadius + mValidateWidth);
        mValidatePath.lineTo(x + mValidateRadius, y + 2 * mValidateRadius + mValidateWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画白光，暂时没画效果
        if (mIsSuccess) {
//            if (mLinearGradient == null) {
//                mLinearGradient = new LinearGradient(0, 0, 30, mBitmap.getHeight(), new int[]{Color.parseColor("#f0f0f0"),
//                        Color.parseColor("#fcfcfc"),
//                        Color.parseColor("#ffffff"),
//                        Color.parseColor("#fcfcfc"),
//                        Color.parseColor("#f0f0f0")
//                }, null, Shader.TileMode.CLAMP);
//            }
//
//            mPaint.setShader(mLinearGradient);
//
//            //canvas.save();
//
//            canvas.drawRect(0, 0, 30, mBitmap.getHeight(), mPaint);
            //canvas.restore();

            mIsSuccess = false;
        }

        if (mBitmap != null) {
            float width = mBitmap.getWidth() * 1.0f;
            float scale = getMeasuredWidth() / width;
            mMatrix = new Matrix();
            mMatrix.postScale(scale, scale);
            //背景缩放
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mMatrix, false);
            //画背景
            canvas.drawBitmap(mBitmap, (getMeasuredWidth() - mBitmap.getWidth()) / 2, 0, null);

            //初始化滑块
            if (mScrollPath == null) {
                initScrollRect();
            }
            //画滑块
            mScrollPaint.setColor(Color.parseColor("#FFE1FF"));
            canvas.drawPath(mScrollPath, mScrollPaint);

            //滑块盖子
            mScrollPaint.setColor(Color.GRAY);
            mScrollRect.set(mLeftMargin, mBitmap.getHeight(), mLeftMargin + 90, (int) (mBitmap.getHeight() + 2 * mScrollMargin + mScrollHeight));
            canvas.drawRoundRect(mScrollRect, 20, 20, mScrollPaint);

            //手指按下才画验证图形
            if (mIsDraw) {
                //初始化验证块数据
                if (mIsInvalidate) {
                    randomValidateXY1();
                    mIsInvalidate = false;

                    mValidateBitmap = createValidateBitmap();
                }

                //画验证块
                mValidatePaint.setColor(Color.parseColor("#88000000"));
                canvas.drawPath(mValidatePath, mValidatePaint);

                //画抠图
                canvas.drawBitmap(mValidateBitmap, -x + mLeftMargin, 0, null);
            }

        }

    }

    /**
     * 刷新验证(正在验证的时候不能刷新)
     */
    public void refreshView() {
        if (mIsTouch) {
            return;
        }
        mIsDraw = false;
        mIsInvalidate = true;
        mLeftMargin = 0;
        invalidate();
    }

    /**
     * 创建滑块图片
     *
     * @return
     */
    private Bitmap createValidateBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        //抗锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        //画验证块
        mValidatePaint.setColor(Color.WHITE);
        canvas.drawPath(mValidatePath, mValidatePaint);
        //抠图
        mValidatePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mBitmap, (getMeasuredWidth() - mBitmap.getWidth()) / 2, 0, mValidatePaint);
        mValidatePaint.setXfermode(null);

        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int curX = (int) event.getX();
        int curY = (int) event.getY();
        //成功的话不响应
        if (mIsSuccess) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = curX;
                //滑块区域才响应
                if (mScrollRect.contains(curX, curY)) {
                    mIsTouch = true;
                    //画验证块
                    mIsDraw = true;
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //在滑块区域,才画
                if (curY >= mScrollRect.top && curY <= mScrollRect.bottom) {
                    mLeftMargin = curX - mDownX;
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                //范围在左右5像素都算成功
                if (Math.abs(mLeftMargin - x) < 5) {
                    mIsDraw = false;
                    mIsInvalidate = true;
                    mIsSuccess = true;
                    //滑块状态回调
                    if (mCallBack != null) {
                        mCallBack.doBack(true);
                    }
                } else {
                    mIsSuccess = false;
                    //滑块状态回调
                    if (mCallBack != null) {
                        mCallBack.doBack(false);
                    }
                }
                //滑块还原
                mLeftMargin = 0;
                mIsTouch = false;
                invalidate();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * dp转px
     *
     * @param dp
     * @return
     */
    private int dp2px(int dp) {
        return (int) (mContext.getResources().getDisplayMetrics().density * dp + 0.5);
    }

    /**
     * 回调
     */
    public interface ValidateCallBack {
        void doBack(boolean flag);
    }

}
