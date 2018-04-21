package com.example.heartx.assistant.util.adaptive;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.heartx.assistant.R;
import com.orhanobut.logger.Logger;

/**
 * 用于图片和布局自适应多屏幕的工具类
 * Created by HeartX on 2017/12/13.
 */

public class AdaptiveScreen {

    private final static String SCALED = "SCALED";

    private final static int DEFAULT_SCREEN_WIDTH = 1080;
    private final static int DEFAULT_SCREEN_HEIGHT = 1920;

    private static float widthScale;
    private static float heightScale;

    private static float currentWidth;
    private static float currentHeight;

    //private static ViewSizeChangeFactory factory = new ViewSizeChangeFactory();

    private static boolean isLandscape = false;

    public static boolean adaptive(View view) {

        if (isUntrueResolution(view) /*&& factory != null*/) {
            if (/*factory.*/isPad(view) && isLandscape && isUncreatedFiller(view)) {
                Logger.d("create ");

                FillerCreator.create(((ViewGroup) view).getChildAt(0).getContext());
            }
            return new ViewSizeChangeFactory().change(view);
        }

        Logger.d("need not scale");
        return false;
    }

    public static boolean adaptive(View view, ViewGroup.LayoutParams params) {
        view.setLayoutParams(params);
        return adaptive(view);
    }

    private static boolean isUncreatedFiller(View view) {

        ViewGroup contentView;

        if (isContentView(view)) {
            contentView = (ViewGroup) view;
        } else {
            return false;//不是contentView不创建Filler
        }

        int childCount = contentView.getChildCount();
        if (childCount == 1) {
            return true;//等于1代表未创建Filler填充空白屏幕
        }

        //代码执行到这里代表contentView至少有一个子控件
        for (int i = 0; i < childCount; i++) {
            if (contentView.getChildAt(i).getTag() instanceof Filler) {
                return false;//contentView已经创建过Filler控件
            }
        }

        return true;
    }

    private static boolean isUntrueResolution(View view) {

        DisplayMetrics displayMetrics = view.getResources().getDisplayMetrics();

        currentWidth = displayMetrics.widthPixels;
        currentHeight = displayMetrics.heightPixels;

        widthScale = currentWidth / DEFAULT_SCREEN_WIDTH;
        heightScale = currentHeight / DEFAULT_SCREEN_HEIGHT;

        boolean untrue = currentWidth != DEFAULT_SCREEN_WIDTH || currentHeight != DEFAULT_SCREEN_HEIGHT;

        isLandscape = view.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscape/*currentWidth > currentHeight*/) {
            //isLandscape = true;
            widthScale = currentHeight / DEFAULT_SCREEN_WIDTH;
            heightScale = currentHeight / DEFAULT_SCREEN_HEIGHT;
        }

        return untrue;
    }

    private static boolean isPad(View view) {

        return (view.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static boolean isUnscaledSize(View view) {

        return view.getTag(91295463) != SCALED;
    }

    private static boolean isRootLayout(View view) {

        return getRootLayout(view) == view;
    }

    private static View getRootLayout(View view) {

        if (isContentView(view)) {

            return ((ViewGroup) view).getChildAt(0);
        } else{

            Activity activity = (Activity) view.getContext();

            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);

            return contentView.getChildAt(0);
        }
    }

    private static boolean isContentView(View view) {

        return view.getId() == android.R.id.content;
    }

//    public static void release() {
//
//        if (factory != null) {
//            factory.release();
//        }
//    }

    private static class ViewSizeChangeFactory {

        private View contentView;

        private ViewSizeChangeFactory() {

        }

        private ViewSizeChangeFactory(View view) {
            this.contentView = view;
        }

        private boolean change(View view) {

            if (view instanceof ViewGroup) {

                changeViewGroup((ViewGroup) view);
            } else {

                changeView(view);
            }
            return true;
        }

        private void changeViewGroup(ViewGroup view) {

            int childCount = view.getChildCount();

            for (int i = 0; i < childCount; i++) {

                View child = view.getChildAt(i);

                if (child instanceof ViewGroup) {

                    changeViewGroup((ViewGroup) child);
                } else {

                    changeView(child);
                }
            }
            changeView(view);
        }

        private void changeView(View view) {
            if (isUnscaledSize(view) && !isContentView(view)) {
                //Logger.d(((TextView)child).getText());
                resetPadding(view);
                resetParams(view);
                //resetParams(child, null);
                view.setTag(91295463, SCALED);
            }
        }

//        private boolean isUnscaledSize(View view) {
//
//            return view.getTag(91295463) != SCALED;
//        }

        private void resetPadding(View child) {

            int paddingLeft = (int) (child.getPaddingLeft() * widthScale);
            int paddingTop = (int) (child.getPaddingTop() * heightScale);
            int paddingRight = (int) (child.getPaddingRight() * widthScale);
            int paddingBottom = (int) (child.getPaddingBottom() * heightScale);

            child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }

        private void resetParams(View child) {

            ViewGroup.LayoutParams lParams = child.getLayoutParams();

            if (lParams != null) {
                Logger.d("enter into : " + child.getClass().getSimpleName());

                if (isPad(child) && isLandscape && isRootLayout(child)) {

                    lParams.width = (int) currentHeight;

                } else if (lParams.width > 0) {
                    lParams.width = (int) (lParams.width * widthScale);
                }
                if (lParams.height > 0) {
                    lParams.height = (int) (lParams.height * heightScale);
                }

                if (lParams instanceof MarginLayoutParams) {
                    //Logger.d("is MarginLayoutParams");
                    MarginLayoutParams mParams = (MarginLayoutParams) lParams;

                    int leftMargin = (int) (mParams.leftMargin * widthScale);
                    int topMargin = (int) (mParams.topMargin * heightScale);
                    int rightMargin = (int) (mParams.rightMargin * widthScale);
                    int bottomMargin = (int) (mParams.bottomMargin * heightScale);

                    mParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                }
                if (lParams instanceof WindowManager.LayoutParams) {
                    Logger.d("lParams is WindowManager.LayoutParams instance");

                    WindowManager.LayoutParams wParams = (WindowManager.LayoutParams) lParams;

                    wParams.x = (int) (wParams.x * widthScale);
                    wParams.y = (int) (wParams.y * heightScale);
                }
                child.setLayoutParams(lParams);
            }
        }

//        private boolean isRootLayout(View view) {
//
//            return getRootLayout(view) == view;
//        }
//
//        private View getRootLayout(View view) {
//
//            if (isContentView(view)) {
//
//                return ((ViewGroup) view).getChildAt(0);
//            } else{
//
//                Activity activity = (Activity) view.getContext();
//
//                ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
//
//                return contentView.getChildAt(0);
//            }
//        }

        private void resetParams(View child, Object redundant) {

            ViewGroup.LayoutParams lParams = child.getLayoutParams();

            if (lParams != null) {

                if (isPad(child) && isLandscape && isRootLayout(child)) {

                    lParams.width = (int) currentHeight;

                } else if (lParams.width > 0) {
                    lParams.width = (int) (lParams.width * widthScale);
                }
                if (lParams.height > 0) {
                    lParams.height = (int) (lParams.height * heightScale);
                }

                if (lParams instanceof MarginLayoutParams) {
                    //Logger.d("is MarginLayoutParams");
                    MarginLayoutParams mParams = (MarginLayoutParams) lParams;

                    float l = (float) mParams.leftMargin / DEFAULT_SCREEN_WIDTH;
                    float t = (float) mParams.topMargin / DEFAULT_SCREEN_HEIGHT;
                    float r = (float) mParams.rightMargin / DEFAULT_SCREEN_WIDTH;
                    float b = (float) mParams.bottomMargin / DEFAULT_SCREEN_HEIGHT;

                    int leftMargin = (int) (currentWidth * l);
                    int topMargin = (int) (currentHeight * t);
                    int rightMargin = (int) (currentWidth * r);
                    int bottomMargin = (int) (currentHeight * b);

                    mParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                }
            }

            child.setLayoutParams(lParams);
        }

//        private boolean isPad(Context c) {
//
//            return (c.getResources().getConfiguration().screenLayout
//                    & Configuration.SCREENLAYOUT_SIZE_MASK)
//                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
//        }

        private float countRatio(int defaultSize) {
            return countRatio(contentView, defaultSize);
        }

        private float countRatio(View view, int defaultSize) {

            float pixels;

            switch (defaultSize) {
                case DEFAULT_SCREEN_WIDTH: {
                    pixels = view.getResources().getDisplayMetrics().widthPixels;
                    break;
                }

                case DEFAULT_SCREEN_HEIGHT: {
                    pixels = view.getResources().getDisplayMetrics().heightPixels;
                    break;
                }

                default: {
                    return 0;
                }
            }

            return pixels / defaultSize;
        }

//        private void release() {
//
//            if (contentView != null) {
//                contentView = null;
//            }
//        }

        private int getTitleBarHeight(Context c) {
            ActionBar supportActionBar = null;
            if (c instanceof AppCompatActivity) {
                supportActionBar = ((AppCompatActivity) c).getSupportActionBar();
            }
            return supportActionBar == null ? -1 : supportActionBar.getHeight();
        }

        private int getStatusBarHeight(Context c) {
            int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
            return resourceId > 0 ? c.getResources().getDimensionPixelSize(resourceId) : 0;
        }
    }

    private static class FillerCreator {

        private Context context;

        private FillerCreator(Context context) {
            this.context = context;
        }

        private static void create(Context context) {// TODO: 2017/12/20 只是create所以不应该执行添加
            FillerCreator viewCreateFactory = new FillerCreator(context);

            ViewGroup contentView = (ViewGroup) ((Activity) context).findViewById(android.R.id.content);
            LinearLayout linearLayout = viewCreateFactory.newLinearLayout();
            RelativeLayout relativeLayout = viewCreateFactory.newRelativeLayout();

            relativeLayout.addView(viewCreateFactory.newTextView());
            linearLayout.addView(relativeLayout);
            linearLayout.setTag(new Filler(){});
            contentView.addView(linearLayout);
        }

        private FrameLayout newFrameLayout() {

            FrameLayout frameLayout = new FrameLayout(context);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -1);

            frameLayout.setBackgroundColor(0x99336699);

            frameLayout.setLayoutParams(layoutParams);

            return frameLayout;
        }

        private RelativeLayout newRelativeLayout() {

            RelativeLayout relativeLayout = new RelativeLayout(context);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -2);

            relativeLayout.setBackgroundColor(0xff336699);

            relativeLayout.setLayoutParams(layoutParams);

            return relativeLayout;
        }

        private LinearLayout newLinearLayout() {

            LinearLayout linearLayout = new LinearLayout(context);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -1);

            linearLayout.setBackgroundColor(0x99336699);

            linearLayout.setLayoutParams(layoutParams);

            linearLayout.setX(currentHeight);

            return linearLayout;
        }

        private TextView newTextView() {

            TextView textView = new TextView(context);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -2);
            //ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(layoutParams);
            //margin.leftMargin = 800;
            //margin.topMargin = 800;

            textView.setLayoutParams(layoutParams);

            textView.setText(R.string.title);

            textView.setBackgroundColor(0x20f5f904);
            //button.setGravity(Gravity.CENTER);
            //button.setX(200);

            return textView;
        }
    }

}
