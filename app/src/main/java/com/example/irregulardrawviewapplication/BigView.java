package com.example.irregulardrawviewapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.io.IOException;
import java.io.InputStream;

public class BigView extends View implements GestureDetector.OnGestureListener,View.OnTouchListener {
    private static final String TAG = "BigView";
    private Scroller mScroller;
    private GestureDetector mGestureDetector;
    private BitmapFactory.Options mOptions;
    private Rect rect;//指定要加载的矩形区域
    private int mImageWidth;//图片长宽
    private int mImageHeight;
    private int mViewWidth; //view长宽
    private int mViewHeight;
    private BitmapRegionDecoder bitmapRegionDecoder;//区域解码器
    private float mScale; //缩放因子
    private Bitmap bitmap;

    public BigView(Context context) {
        this(context,null,0);
    }

    public BigView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BigView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //指定要加载的矩形区域
        rect = new Rect();
        //解码图片的配置
        mOptions = new BitmapFactory.Options();
        //手势
        mGestureDetector = new GestureDetector(context,this);
        setOnTouchListener(this);
        //滑动帮助
        mScroller = new Scroller(context);
    }

    /**
     * 由使用者输入一张图片
     * @param inputStream 输入流
     */
    public void setImage(InputStream inputStream){
        //先读取原图片的宽 高
        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream,null,mOptions);
        mImageHeight = mOptions.outHeight;
        mImageWidth = mOptions.outWidth;
        //复用 内存复用
        mOptions.inMutable = true;
        //设置像素格式为 rgb565
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mOptions.inJustDecodeBounds = false;
        //创建区域解码器 用于区域解码图片
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取测量的view的大小
        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();
        //如果解码器是null 表示没有设置过要显示的图片
        if (null == bitmapRegionDecoder){
            return;
        }
        //确定要加载的图片的区域
        rect.left = 0;
        rect.top = 0;
        rect.right = mImageWidth;
        //获得缩放因子
        mScale = mViewWidth / (float)mImageWidth;

        //需要加载的高 * 缩放因子 = 视图view的高
        rect.bottom = (int)(mViewHeight / mScale);
        Log.e(TAG,"l="+rect.left);
        Log.e(TAG,"t="+rect.top);
        Log.e(TAG,"r="+rect.right);
        Log.e(TAG,"b="+rect.bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //如果解码器是null  表示没有设置过要实现的图片
        if (null == bitmapRegionDecoder){
            return;
        }
        //复用上一张bitmap
        Log.e(TAG,"复用上一张bitmap="+bitmap);
        mOptions.inBitmap = bitmap;
        //解码指定区域
        bitmap = bitmapRegionDecoder.decodeRegion(rect,mOptions);
        //使用矩阵 对图片进行 缩放
        Matrix matrix = new Matrix();
        matrix.setScale(mScale,mScale);
        //画出来
        canvas.drawBitmap(bitmap,matrix,null);
    }

    /**
     * 手指按下屏幕的回调
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        //如果滑动还没有停止 强制停止
        if (!mScroller.isFinished()){
            mScroller.forceFinished(true);
        }
        //继续接收后续事件
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    /**
     *
     * @param motionEvent
     * @param motionEvent1
     * @param v
     * @param v1
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
       //手指从下往上 图片也要往上 v是负数 ，top和bottom在减
       //手指从上往下 图片也要往上 v是正数 ，top和bottom在加
        //改变加载图片的区域
        rect.offset(0,(int)v1);
        if (rect.bottom > mImageHeight){
            rect.bottom = mImageHeight;
            rect.top = mImageHeight - (int)(mViewHeight / mScale);
        }
        if (rect.top < 0){
            rect.top = 0 ;
            rect.bottom = (int)(mViewHeight / mScale);
        }
        //重绘
        invalidate();
        return false;

    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    /**
     * 手指离开屏幕 滑动 惯性
     * @param motionEvent
     * @param motionEvent1
     * @param v 速度 每秒x方向 移动的像素
     * @param v1
     * @return
     */
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        /**
         * startx:滑动开始的x坐标
         * starty：滑动开始的y坐标
         * minx：x方向最小值
         * max：最大
         * y
         */
        //计算器
        mScroller.fling(0,rect.top,0,(int)-v1,0,0,0,
                mImageHeight - (int)(mViewHeight / mScale));
        return false;
    }
    //获取计算结果并且重绘
    @Override
    public void computeScroll() {
        //已经计算结束 return
        if(mScroller.isFinished()){
            return;
        }
        //true 表示当前动画并未结束
        if (mScroller.computeScrollOffset()){
            rect.top = mScroller.getCurrY();
            rect.bottom = rect.top + (int)(mViewHeight / mScale);
            invalidate();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //交给手势处理
        return mGestureDetector.onTouchEvent(motionEvent);
    }
}
