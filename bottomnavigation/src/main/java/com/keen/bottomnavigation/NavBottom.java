package com.keen.bottomnavigation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keen on 08/01/18.
 */

public class NavBottom extends LinearLayout implements View.OnClickListener{

    private static final String KEY_CURRENT_TAG = "NavigateTabBar";

    private List<ViewHolder>      mViewHolderList;
    private OnTabSelectedListener mTabSelectListener;
    private FragmentActivity      mFragmentActivity;
    private String                mCurrentTag;
    private String                mRestoreTag;

    private int mMainContentLayoutId;
    private ColorStateList mSelectedTextColor;
    private ColorStateList mNormalTextColor;
    private float mTabTextSize;
    private int mDefaultSelectedTab = 0;
    private int mCurrentSelectedTab;

    public NavBottom(Context context) {
        this(context, null);
    }

    public NavBottom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavBottom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NavBottom, 0, 0);
        ColorStateList tabTextColor = typedArray.getColorStateList(R.styleable.NavBottom_navigateTabTextColor);
        ColorStateList selectedTabTextColor = typedArray.getColorStateList(R.styleable.NavBottom_navigateTabSelectedTextColor);

        this.mTabTextSize = typedArray.getDimensionPixelSize(R.styleable.NavBottom_navigateTabTextSize, 0);
        this.mMainContentLayoutId = typedArray.getResourceId(R.styleable.NavBottom_containerId, 0);
        this.mNormalTextColor = (tabTextColor != null ? tabTextColor : context.getResources().getColorStateList(R.color.navigate_tabbar_text_normal));

        if (selectedTabTextColor != null) {
            this.mSelectedTextColor = selectedTabTextColor;
        }
        else {
            ThemeUtils.checkAppCompatTheme(context);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            this.mSelectedTextColor = context.getResources().getColorStateList(typedValue.resourceId);
        }

        this.mViewHolderList = new ArrayList<>();
    }

    public void addTab(Class frameLayoutClass, TabParam tabParam) {
        int defaultLayout = R.layout.tab_view;

        View view = LayoutInflater.from(getContext()).inflate(defaultLayout, null);
        view.setFocusable(true);

        ViewHolder holder = new ViewHolder();
        holder.tabIndex = this.mViewHolderList.size();
        holder.fragmentClass = frameLayoutClass;
        holder.tag = tabParam.title;
        holder.pageParam = tabParam;
        holder.tabIcon = view.findViewById(R.id.tab_icon);
        holder.tabTitle = view.findViewById(R.id.tab_title);

        if (TextUtils.isEmpty(tabParam.title)) {
            holder.tabTitle.setVisibility(View.INVISIBLE);
        }
        else {
            holder.tabTitle.setText(tabParam.title);
        }

        if (this.mTabTextSize != 0) {
            holder.tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.mTabTextSize);
        }
        if (this.mNormalTextColor != null) {
            holder.tabTitle.setTextColor(this.mNormalTextColor);
        }

        if (tabParam.backgroundColor > 0) {
            view.setBackgroundResource(tabParam.backgroundColor);
        }

        if (tabParam.iconResId > 0) {
            holder.tabIcon.setImageResource(tabParam.iconResId);
        }
        else {
            holder.tabIcon.setVisibility(View.INVISIBLE);
        }

        if (tabParam.iconResId > 0 && tabParam.iconSelectedResId > 0) {
            view.setTag(holder);
            view.setOnClickListener(this);
            this.mViewHolderList.add(holder);
        }

        super.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mMainContentLayoutId == 0) {
            throw new RuntimeException("mFrameLayoutId Cannot be 0");
        }
        if (this.mViewHolderList.size() == 0) {
            throw new RuntimeException("mViewHolderList.size Cannot be 0, Please call addTab()");
        }
        if (!(getContext() instanceof FragmentActivity)) {
            throw new RuntimeException("parent activity must is extends FragmentActivity");
        }
        this.mFragmentActivity = (FragmentActivity) getContext();

        ViewHolder defaultHolder = null;

        hideAllFragment();
        if (!TextUtils.isEmpty(this.mRestoreTag)) {
            for (ViewHolder holder : this.mViewHolderList) {
                if (TextUtils.equals(this.mRestoreTag, holder.tag)) {
                    defaultHolder = holder;
                    this.mRestoreTag = null;
                    break;
                }
            }
        }
        else {
            defaultHolder = this.mViewHolderList.get(this.mDefaultSelectedTab);
        }

        this.showFragment(defaultHolder);
    }

    @Override
    public void onClick(View v) {
        Object object = v.getTag();
        if (object != null && object instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) v.getTag();
            showFragment(holder);
            if (this.mTabSelectListener != null) {
                this.mTabSelectListener.onTabSelected(holder);
            }
        }
    }

    private void showFragment(ViewHolder holder) {
        android.support.v4.app.FragmentTransaction transaction = this.mFragmentActivity.getSupportFragmentManager().beginTransaction();
        if (isFragmentShown(transaction, holder.tag)) {
            return;
        }
        setCurrSelectedTabByTag(holder.tag);

        android.support.v4.app.Fragment fragment = this.mFragmentActivity.getSupportFragmentManager().findFragmentByTag(holder.tag);
        if (fragment == null) {
            fragment = getFragmentInstance(holder.tag);
            transaction.add(this.mMainContentLayoutId, fragment, holder.tag);
        }
        else {
            transaction.show(fragment);
        }
        transaction.commit();
        this.mCurrentSelectedTab = holder.tabIndex;
    }

    private boolean isFragmentShown(android.support.v4.app.FragmentTransaction transaction, String newTag) {
        if (TextUtils.equals(newTag, this.mCurrentTag)) {
            return true;
        }

        if (TextUtils.isEmpty(this.mCurrentTag)) {
            return false;
        }

        android.support.v4.app.Fragment fragment = this.mFragmentActivity.getSupportFragmentManager().findFragmentByTag(this.mCurrentTag);
        if (fragment != null && !fragment.isHidden()) {
            transaction.hide(fragment);
        }

        return false;
    }

    private void setCurrSelectedTabByTag(String tag) {
        if (TextUtils.equals(this.mCurrentTag, tag)) {
            return;
        }
        for (ViewHolder holder : this.mViewHolderList) {
            if (TextUtils.equals(this.mCurrentTag, holder.tag)) {
                holder.tabIcon.setImageResource(holder.pageParam.iconResId);
                holder.tabTitle.setTextColor(this.mNormalTextColor);
            }
            else if (TextUtils.equals(tag, holder.tag)) {
                holder.tabIcon.setImageResource(holder.pageParam.iconSelectedResId);
                holder.tabTitle.setTextColor(this.mSelectedTextColor);
            }
        }
        this.mCurrentTag = tag;
    }

    private android.support.v4.app.Fragment getFragmentInstance(String tag) {
        android.support.v4.app.Fragment fragment = null;
        for (ViewHolder holder : this.mViewHolderList) {
            if (TextUtils.equals(tag, holder.tag)) {
                try {
                    fragment = (android.support.v4.app.Fragment) Class.forName(holder.fragmentClass.getName()).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return fragment;
    }

    private void hideAllFragment() {
        if (this.mViewHolderList == null || this.mViewHolderList.size() == 0) {
            return;
        }
        android.support.v4.app.FragmentTransaction transaction = this.mFragmentActivity.getSupportFragmentManager().beginTransaction();
        for (ViewHolder holder : this.mViewHolderList) {
            android.support.v4.app.Fragment fragment = this.mFragmentActivity.getSupportFragmentManager().findFragmentByTag(holder.tag);
            if (fragment != null && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    public void setSelectedTabTextColor(ColorStateList selectedTextColor) {
        this.mSelectedTextColor = selectedTextColor;
    }

    public void setSelectedTabTextColor(int color) {
        this.mSelectedTextColor = ColorStateList.valueOf(color);
    }

    public void setTabTextColor(ColorStateList color) {
        this.mNormalTextColor = color;
    }

    public void setTabTextColor(int color) {
        this.mNormalTextColor = ColorStateList.valueOf(color);
    }

    public void setFrameLayoutId(int frameLayoutId) {
        this.mMainContentLayoutId = frameLayoutId;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mRestoreTag = savedInstanceState.getString(KEY_CURRENT_TAG);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENT_TAG, this.mCurrentTag);
    }

    private static class ViewHolder {
        public String    tag;
        public TabParam  pageParam;
        public ImageView tabIcon;
        public TextView  tabTitle;
        public Class     fragmentClass;
        public int       tabIndex;
    }

    public static class TabParam {
        public int backgroundColor = android.R.color.white;
        public int    iconResId;
        public int    iconSelectedResId;
        public int    titleStringRes;
        //        public int tabViewResId;
        public String title;

        public TabParam(int iconResId, int iconSelectedResId, String title) {
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.title = title;
        }

        public TabParam(int iconResId, int iconSelectedResId, int titleStringRes) {
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.titleStringRes = titleStringRes;
        }

        public TabParam(int backgroundColor, int iconResId, int iconSelectedResId, int titleStringRes) {
            this.backgroundColor = backgroundColor;
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.titleStringRes = titleStringRes;
        }

        public TabParam(int backgroundColor, int iconResId, int iconSelectedResId, String title) {
            this.backgroundColor = backgroundColor;
            this.iconResId = iconResId;
            this.iconSelectedResId = iconSelectedResId;
            this.title = title;
        }
    }

    public interface OnTabSelectedListener {
        void onTabSelected(ViewHolder holder);
    }

    public void setTabSelectListener(OnTabSelectedListener tabSelectListener) {
        this.mTabSelectListener = tabSelectListener;
    }

    public void setDefaultSelectedTab(int index) {
        if (index >= 0 && index < this.mViewHolderList.size()) {
            this.mDefaultSelectedTab = index;
        }
    }

    public void setCurrentSelectedTab(int index) {
        if (index >= 0 && index < this.mViewHolderList.size()) {
            ViewHolder holder = this.mViewHolderList.get(index);
            this.showFragment(holder);
        }
    }

    public int getCurrentSelectedTab() {
        return this.mCurrentSelectedTab;
    }

    public static class ThemeUtils {

        private static final int[] APPCOMPAT_CHECK_ATTRS = {R.attr.colorPrimary};

        public static void checkAppCompatTheme(Context context) {
            TypedArray a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS);
            final boolean failed = !a.hasValue(0);
            if (a != null) {
                a.recycle();
            }
            if (failed) {
                throw new IllegalArgumentException("You need to use a Theme.AppCompat theme "
                        + "(or descendant) with the design library.");
            }
        }
    }
}

