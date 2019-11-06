package com.example.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class BigView extends View implements GestureDetector.OnGestureListener,
        View.OnTouchListener {

    private BitmapFactory.Options options;
    private Rect rect;
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int imageWidth, imageHeight, viewWidth, viewHeight;
    private BitmapRegionDecoder decoder;
    private float scale;
    private Bitmap bitmap;
    private Matrix matrix;

    public BigView(Context context) {
        this(context, null);
    }

    public BigView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //第一步 设置bigView所需要成员变量
        rect = new Rect();
        // 内存复用
        options = new BitmapFactory.Options();
        //手势识别
        gestureDetector = new GestureDetector(context, this);
        //滚动类
        scroller = new Scroller(context);

        setOnTouchListener(this);

    }

    //第二步 设置图片 等到图片信息
    public void setImage(InputStream is) {
        //获取图片的宽和高（没有将整个图片加载进内存）
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;

        //开启复用
        options.inMutable = true;
        //设置格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        //区域解码器
        try {
            decoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    //第三步 开始测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        //确定图片的加载区域
        rect.left = 0;
        rect.top = 0;
        rect.right = imageWidth;
        scale = viewWidth / (float) imageWidth;
        rect.bottom = (int) (viewHeight / scale);
    }

    //第四步  画出具体内容
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (decoder == null) {
            return;
        }
        //内存复用（复用的bitmap必须跟即将解码的bitmap尺寸一样）
        options.inBitmap = bitmap;
        //指定解码区域
        bitmap = decoder.decodeRegion(rect, options);
        //得到矩阵进行缩放，得到view大小
        matrix = new Matrix();
        matrix.setScale(scale, scale);
        canvas.drawBitmap(bitmap, matrix, null);
    }

    //第五步 处理点击事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //直接将事件交给手势事件
        return gestureDetector.onTouchEvent(event);
    }

    //第六步 手按下
    @Override
    public boolean onDown(MotionEvent e) {
        //如果移动没有停止 强行停止
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
        return true;
    }

    //第七步 处理滑动事件
    //e1 开始事件 手指按下 获取坐标
    //e2 获取当前坐标
    //xy 移动距离
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        rect.offset(0, (int) distanceY);
        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight;
            rect.top = imageHeight - (int) (viewHeight / scale);
        }
        if (rect.top < 0) {
            rect.top = 0;
            rect.bottom = (int) (viewHeight / scale);
        }
        invalidate();
        return false;
    }

    //第八步 处理惯性问题
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        scroller.fling(0, rect.top, 0, (int) -velocityY, 0, 0, 0,
                imageHeight - (int) (viewHeight / scale));
        return false;
    }

    //第九步 处理计算结果
    @Override
    public void computeScroll() {
        if (scroller.isFinished()) {
            return;
        }
        if (scroller.computeScrollOffset()) {
            rect.top = scroller.getCurrY();
            rect.bottom = rect.top + (int) (viewHeight / scale);
            invalidate();
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

}
