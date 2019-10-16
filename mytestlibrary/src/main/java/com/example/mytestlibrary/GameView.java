package com.example.mytestlibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class GameView extends ViewGroup {
    private Context context = null;
    private int width = 0, height = 0;
    private int time = 6;
    private TextView view = null;
    private int count = 0;
    private int co = 2;
    private int ro = 10;
    private LinearLayout[] linearLayouts = null;
    private View[][] circles = new View[co][ro];
    private int[] durs = new int[10];
    private int[] delays = new int[10];
    private Random random = new Random();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            get();
            handler.postDelayed(this, 50);
        }
    };

    private void get() {
        for (int j = 0; j < co; j++) {
            for (int i = 0; i < ro; i++) {
                if (circles[j][i].getVisibility() == View.VISIBLE) {
                    if (circles[j][i].getLeft() >= (view.getLeft() - width / 10) &&
                            circles[j][i].getLeft() <= view.getRight() &&
                            ((circles[j][i].getTop() + circles[j][i].getTranslationY()) > (view.getTop() - width / 10))
                            && ((circles[j][i].getTop() + circles[j][i].getTranslationY()) < (view.getTop() + width / 20))) {
                        circles[j][i].setVisibility(View.INVISIBLE);
                        count++;
                        Toast.makeText(context, count + "", Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }

    public GameView(Context context) {
        super(context);
        initView(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @SuppressLint("NewApi")
    public GameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }


    /**
     * 要求所有的孩子测量自己的大小，然后根据这些孩子的大小完成自己的尺寸测量
     */
    @SuppressLint("NewApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //测量并保存layout的宽高(使用getDefaultSize时，wrap_content和match_perent都是填充屏幕)
        //稍后会重新写这个方法，能达到wrap_content的效果
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        if (width == 0) {
            height = getMeasuredHeight();
            width = getMeasuredWidth();
            int vw = view.getMeasuredWidth();
            int left = (width - vw) / 2;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = left;
            layoutParams.rightMargin = left;
            view.setLayoutParams(layoutParams);
            view.postInvalidate();
            initAnims();
            handler.post(runnable);
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();
        int childMeasureWidth = 0;
        int childMeasureHeight = 0;
        int layoutWidth = 0;    // 容器已经占据的宽度
        int layoutHeight = 0;   // 容器已经占据的宽度
        int maxChildHeight = 0; //一行中子控件最高的高度，用于决定下一行高度应该在目前基础上累加多少
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            //注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
            childMeasureWidth = child.getMeasuredWidth();
            childMeasureHeight = child.getMeasuredHeight();
            if (layoutWidth < getWidth()) {
                //如果一行没有排满，继续往右排列
                left = layoutWidth;
                right = left + childMeasureWidth;
                top = layoutHeight;
                bottom = top + childMeasureHeight;
            } else {
                //排满后换行
                layoutWidth = 0;
                layoutHeight += maxChildHeight;
                maxChildHeight = 0;

                left = layoutWidth;
                right = left + childMeasureWidth;
                top = layoutHeight;
                bottom = top + childMeasureHeight;
            }

            layoutWidth += childMeasureWidth;  //宽度累加
            if (childMeasureHeight > maxChildHeight) {
                maxChildHeight = childMeasureHeight;
            }

            //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
            child.layout(left, top, right, bottom);
        }

    }

    private void initView(Context context) {
        this.context = context;
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.games_layout, this, false);
        view = root.findViewById(R.id.view);

        view.setOnTouchListener(onTouchListener);


        linearLayouts = new LinearLayout[co];
        for (int i = 0; i < co; i++) {
            LinearLayout linearLayout = new LinearLayout(context);
            LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            linearLayout.setLayoutParams(layoutParams);
            root.addView(linearLayout);
            linearLayouts[i] = linearLayout;
        }
        addView(root);
    }

    private void initAnims() {
        for (int j = 0; j < co; j++) {
            for (int i = 0; i < ro; i++) {
                View view = new View(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((width - 100) / 10, (width - 100) / 10);
                layoutParams.setMargins(5, -width / 10, 5, 0);
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(Color.GRAY);
                circles[j][i] = view;
                linearLayouts[j].addView(view);
            }
        }
        for (int j = 0; j < co; j++) {
            for (int i = 0; i < ro; i++) {
                if (circles[j][i] != null) {
                    initAnim(i, circles[j][i]);
                }
            }
        }
    }


    private void initAnim(final int i, final View mview) {
        int dur = (random.nextInt(time / 2) + time / 2) * 1000;
        while (i != 0 && durs[i - 1] == dur) {
            dur = (random.nextInt(time / 2) + time / 2) * 1000;
        }
        durs[i] = dur;
        int delay = random.nextInt(5) * 1000;
        while (i != 0 && delays[i - 1] == delay) {
            delay = random.nextInt(5) * 1000;
        }
        delays[i] = delay;
        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mview, View.TRANSLATION_Y, 0, height + width / 5 * 2);
        objectAnimator.setDuration(durs[i]);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                initAnim(i, mview);
            }
        });
        mview.postDelayed(new Runnable() {
            @Override
            public void run() {
                objectAnimator.start();
            }
        }, delays[i]);
    }


    private int lastX;
    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;

                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                    int l = layoutParams.leftMargin + dx;

                    if (l < 0) {
                        l = 0;
                    } else if (l > (width - view.getMeasuredWidth())) {
                        l = width - view.getMeasuredWidth();
                    }
                    int r = width - l - v.getWidth();

                    layoutParams.leftMargin = l;
                    layoutParams.rightMargin = r;
                    v.setLayoutParams(layoutParams);

                    lastX = (int) event.getRawX();
                    v.postInvalidate();

                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }
    };
}
