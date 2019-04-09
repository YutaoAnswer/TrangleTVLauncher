package com.trigtop.gb.view;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trigtop.gb.R;
import com.trigtop.gb.adapt.ShortcutsAdapter;
import com.trigtop.gb.bean.Shortcut;
import com.trigtop.gb.newyahooweather.NewYahooWeather;
import com.trigtop.gb.util.AnimUtil;
import com.trigtop.gb.util.DBHelper;
import com.trigtop.gb.util.Data;
import com.trigtop.gb.util.DateUtil;
import com.trigtop.gb.util.LogUtil;
import com.trigtop.gb.util.Util;
import com.trigtop.gb.util.WeatherUtils;
import com.trigtop.gb.widget.DrawingOrderRelativeLayout;
import com.trigtop.gb.widget.GridViewTV;
import com.trigtop.gb.widget.MainUpView;
import com.trigtop.gb.widget.MetroItemFrameLayout;
import com.trigtop.gb.widget.MetroViewBorderHandler;
import com.trigtop.gb.widget.MetroViewBorderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zh.wang.android.apis.yweathergetter4a.WeatherInfo;

public class MainActivity extends Activity
        implements AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {

    @BindView(R.id.mainUpView)
    MainUpView mainUpView;
    @BindView(R.id.list)
    DrawingOrderRelativeLayout drawingOrderRelativeLayout;
    @BindView(R.id.tv_week)
    TextView tvWeek;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.view10)
    MetroItemFrameLayout view10;
    @BindView(R.id.view11)
    MetroItemFrameLayout view11;
    @BindView(R.id.view)
    MetroItemFrameLayout view;
    @BindView(R.id.view4)
    MetroItemFrameLayout view4;
    @BindView(R.id.view2)
    MetroItemFrameLayout view2;
    @BindView(R.id.view3)
    MetroItemFrameLayout view3;
    @BindView(R.id.view5)
    MetroItemFrameLayout view5;
    @BindView(R.id.view6)
    MetroItemFrameLayout view6;
    @BindView(R.id.view12)
    MetroItemFrameLayout view12;
    @BindView(R.id.view13)
    MetroItemFrameLayout view13;
    @BindView(R.id.view7)
    MetroItemFrameLayout view7;
    @BindView(R.id.view8)
    MetroItemFrameLayout view8;
    @BindView(R.id.fragment_home_gridview)
    GridViewTV gridViewTV;
    @BindView(R.id.title_weather_image)
    ImageView titleWeatherImage;
    @BindView(R.id.title_weather_city)
    TextView titleWeatherCity;
    @BindView(R.id.title_weather_temperature)
    TextView titleWeatherTemperature;
    @BindView(R.id.title_weather_info)
    TextView titleWeatherInfo;

    private View mOldView;
    private PackageManager pm;
    private float scale = 1.1f;
    private boolean isSelect = false;
    private ShortcutsAdapter mAdapter;
    private static final int MSG_ONE = 1;
    private static final int MSG_ZERO = 0;
    public static final int columns = 9;
    private String mCurrentCategory = Data.HOME;
    private WeatherReceiver mWeatherReceiver;
    private final String ADDITIONAL = "additional";
    private List<Shortcut> mShortcut = new ArrayList<>();
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_main);
        ButterKnife.bind(this);
        initView();
        initDatebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
        registerBroadcast();
        getWeatherAtonResume();
    }

    private void registerBroadcast() {
        mWeatherReceiver = new WeatherReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWeatherReceiver, intentFilter);
    }

    public void setCurrentCategory(String category) {
        mCurrentCategory = category;
    }

    public String getmCurrentCategory() {
        return mCurrentCategory;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ONE:
                    setDateTimeWeek();
                    break;
                case MSG_ZERO:
                    if (isSelect) {
                        isSelect = false;
                    } else {
                        // 如果是第一次进入该gridView，则进入第一个item，如果不是第一次进去，则选择上次出来的item
                        if (mOldView == null) {
                            mOldView = gridViewTV.getChildAt(0);
                            if (mOldView != null) {
                                AnimUtil.setViewScale(mOldView, scale);
                            }
                        } else {
                            AnimUtil.setViewScale(mOldView, scale);
                        }
                    }
                    break;
            }
        }
    };

    private void initDatebase() {
        //db
        int lastDatabaseVersion = Util.getInt(this, Data.PRE_DB_VERSION);//获取上一版本数据库如果第一次装是0
        final int newDatabaseVersion = Util.getNewDatabaseVersion(this, Data.PRE_DB_VERSION);//获取xml里设置的数据库版本
        if (lastDatabaseVersion < newDatabaseVersion) {
            new Thread() {
                public void run() {
                    Util.copyDatabaseFromAssert(MainActivity.this, newDatabaseVersion);//顺便把数据库版本set进去
                    Util.copyWallpaperFromAssert(MainActivity.this);
                }
            }.start();
        }
    }

    private void initView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1000);
                        Message message = new Message();
                        message.what = MSG_ONE;
                        handler.sendMessage(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);
            }
        }).start();

        city_name = Util.getString(MainActivity.this, WeatherUtils.WEATHER_CITY);

        setDateTimeWeek();

        FrameLayout roundedFrameLayout = new FrameLayout(this);
        roundedFrameLayout.setClipChildren(false);

        final MetroViewBorderImpl<FrameLayout> metroViewBorderImpl = new MetroViewBorderImpl<>(roundedFrameLayout);
        metroViewBorderImpl.setBackgroundResource(R.drawable.border_color);

        ViewGroup list = findViewById(R.id.list);
        metroViewBorderImpl.attachTo(list);

        metroViewBorderImpl.getViewBorder().addOnFocusChanged(new MetroViewBorderHandler.FocusListener() {
            @Override
            public void onFocusChanged(View oldFocus, final View newFocus) {
                metroViewBorderImpl.getView().setTag(newFocus);
            }
        });
        metroViewBorderImpl.getViewBorder().addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                View t = metroViewBorderImpl.getView().findViewWithTag("top");
                if (t != null) {
                    ((ViewGroup) t.getParent()).removeView(t);
                    View of = (View) metroViewBorderImpl.getView().getTag(metroViewBorderImpl.getView().getId());
                    ((ViewGroup) of).addView(t);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                View nf = (View) metroViewBorderImpl.getView().getTag();
                if (nf != null) {
                    View top = nf.findViewWithTag("top");
                    if (top != null) {
                        ((ViewGroup) top.getParent()).removeView(top);
                        (metroViewBorderImpl.getView()).addView(top);
                        metroViewBorderImpl.getView().setTag(metroViewBorderImpl.getView().getId(), nf);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

//        GridLayoutManager manager = new GridLayoutManager(this, 1);
//        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        manager.supportsPredictiveItemAnimations();
//        tvRecyclerView.setHasFixedSize(true);
//        tvRecyclerView.setNestedScrollingEnabled(false);
//        tvRecyclerView.setLayoutManager(manager);
//        int itemSpace = getResources().
//                getDimensionPixelSize(R.dimen.recyclerView_item_space1);
//        tvRecyclerView.addItemDecoration(new SpaceItemDecoration(itemSpace));
//        DefaultItemAnimator animator = new DefaultItemAnimator();
//        tvRecyclerView.setItemAnimator(animator);
//        final MaulCarouselAdapter mAdapter = new MaulCarouselAdapter(this);
//        tvRecyclerView.setAdapter(mAdapter);
//        mAdapter.setOnItemStateListener(new MaulCarouselAdapter.OnItemStateListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                ShortcutsAdapter.ShortcutHolder holder = (ShortcutsAdapter.ShortcutHolder) view.getTag();
//                ComponentName componentName = holder.componentName;
//                if (ADDITIONAL.equals(componentName.getPackageName())) {
//                    Intent editIntent = new Intent(MainActivity.this, EditorActivity.class);
//                    editIntent.putExtra(Util.CATEGORY, mCurrentCategory)
//                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(editIntent);
//                } else {
//                    try {
//                        Intent mainintent = new Intent(Intent.ACTION_MAIN, null);
//                        mainintent.addCategory(Intent.CATEGORY_LAUNCHER);
//                        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                        mainintent.setComponent(componentName);
//                        startActivity(mainintent);
//                    } catch (Exception e) {
//                        LogUtil.e("error", "FolderActivitty.ItemClickListener.onItemClick() startActivity failed: " + componentName);
//                    }
//                }
//            }
//        });

        pm = MainActivity.this.getPackageManager();
        mAdapter = new ShortcutsAdapter(MainActivity.this, mShortcut, pm, false);
        gridViewTV.setAdapter(mAdapter);
        gridViewTV.setNumColumns(columns);
        gridViewTV.setOnItemClickListener(new ItemClickListener());
        gridViewTV.setOnItemSelectedListener(this);
        gridViewTV.setOnFocusChangeListener(this);
    }

    private void getData() {
        mShortcut.clear();
        mShortcut.addAll(DBHelper.getInstance(this).queryByCategory(mCurrentCategory));
        Shortcut forAddItem = new Shortcut();
        forAddItem.setComponentName(ADDITIONAL);
        mShortcut.add(forAddItem);
        mAdapter.notifyDataSetChanged(mShortcut);
    }

    /**
     * set system time to the TextView
     */
    @SuppressLint("SetTextI18n")
    private void setDateTimeWeek() {
        String systemDate = DateUtil.getSystemDate();
        String[] strings1 = Objects.requireNonNull(systemDate).split("年");
        String[] strings2 = Objects.requireNonNull(strings1[1]).split("月");
        String[] strings3 = Objects.requireNonNull(strings2[1]).split("日");
        String[] strings4 = Objects.requireNonNull(strings3[1]).split(":");
        String[] strings5 = Objects.requireNonNull(strings4[2]).split(" ");
        tvTime.setText(strings4[0] + ":" + strings4[1]);
        tvDate.setText(strings2[0] + "-" + strings3[0]);
        switch (strings5[1]) {
            case "Sunday":
                tvWeek.setText("星期日");
                break;
            case "Monday":
                tvWeek.setText("星期一");
                break;
            case "Tuesday":
                tvWeek.setText("星期二");
                break;
            case "Wednesday":
                tvWeek.setText("星期三");
                break;
            case "Thursday":
                tvWeek.setText("星期四");
                break;
            case "Friday":
                tvWeek.setText("星期五");
                break;
            case "Saturday":
                tvWeek.setText("星期六");
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.view10, R.id.view11, R.id.view, R.id.view4, R.id.view2,
            R.id.view3, R.id.view5, R.id.view6, R.id.view12, R.id.view13, R.id.view7, R.id.view8})
    public void onViewClicked(View view) {
        ContentActivity contentActivity = new ContentActivity();
        switch (view.getId()) {
            case R.id.view10://weather
                showEditCityForWeatherDialog();
                break;
            case R.id.view11://clean
                LaunchActivity(new QuickenActivity(), null);
                break;
            case R.id.view://Apps
                LaunchActivity(new AppActicity(), null);
                break;
            case R.id.view4://online_video
                LaunchActivity(contentActivity, "online_video");
                break;
            case R.id.view2://browser
                startThirdAppforPM(getResources().getString(R.string.browser_internet));
                break;
            case R.id.view3://music
                LaunchActivity(contentActivity, "music");
                break;
            case R.id.view5://local_video
                LaunchActivity(contentActivity, "local_video");
                break;
            case R.id.view6://images
                LaunchActivity(contentActivity, "images");
                break;
            case R.id.view12://gone
                LaunchActivity(contentActivity, "");
                break;
            case R.id.view13://gone
                LaunchActivity(contentActivity, "");
                break;
            case R.id.view7://待定
                LaunchActivity(contentActivity,"");
                break;
            case R.id.view8://setting
                Intent settingIntent = new Intent();
                settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName mComp = new ComponentName(getString(R.string.setting_pkg), getString(R.string.setting_main_activity));
                settingIntent.setComponent(mComp);
                try {
                    startActivity(settingIntent);
                } catch (Exception e) {
                    settingIntent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(settingIntent);
                }
                break;
            default:
                break;
        }
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    private void LaunchActivity(Activity activity, String category) {
        Intent intent = new Intent(this, activity.getClass());
        intent.putExtra("category", category);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_down_in, R.anim.activity_down_out);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view != null && gridViewTV.hasFocus()) {
            isSelect = true;
            AnimUtil.setViewScale(view, scale);
            if (mOldView != null)
                AnimUtil.setViewScaleDefault(mOldView);
        }
        mOldView = view;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            new Thread(run).start();
        } else {
            if (mOldView != null) {
                AnimUtil.setViewScaleDefault(mOldView);
            }
            isSelect = false;
        }
    }


    /**
     * handle the icon click event
     */
    private class ItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            ShortcutsAdapter.ShortcutHolder holder = (ShortcutsAdapter.ShortcutHolder) arg1.getTag();
            ComponentName componentName = holder.componentName;
            if (ADDITIONAL.equals(componentName.getPackageName())) {
                Intent editIntent = new Intent(MainActivity.this, EditorActivity.class);
                editIntent.putExtra(Util.CATEGORY, mCurrentCategory)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(editIntent);
            } else {
                try {
                    Intent mainintent = new Intent(Intent.ACTION_MAIN, null);
                    mainintent.addCategory(Intent.CATEGORY_LAUNCHER);
                    mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mainintent.setComponent(componentName);
                    startActivity(mainintent);
                } catch (Exception e) {
                    LogUtil.e("error", "FolderActivitty.ItemClickListener.onItemClick() startActivity failed: " + componentName);
                }
            }
        }
    }

    private class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.left = space;
        }
    }

    String city_name = "";

    public void initWeather() {
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            if (!connManager.getActiveNetworkInfo().isConnected()) return;
            Timer mTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    WeatherUtils.updateWeather(MainActivity.this, mUpdateWeatherHandler, city_name);
                }
            };
            try {
                mTimer.schedule(task, 2000, 1000 * 60 * 60 * 3);//do it after 10 seconds , per 3 hours do it again
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    private void getWeatherAtonResume() {
        if (!GET_WEATHER_OK) {
            ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager.getActiveNetworkInfo() != null) {
                if (!connManager.getActiveNetworkInfo().isConnected()) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        WeatherUtils.updateWeather(MainActivity.this, mUpdateWeatherHandler, city_name);
                    }
                }).start();
            }
        }
    }

    private static int mWeatherCode = 3200;
    private static boolean GET_WEATHER_OK = false;

    @SuppressLint("HandlerLeak")
    private Handler mUpdateWeatherHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WeatherUtils.MSG_WEATHER_OK_NEW: {
                    NewYahooWeather weatherInfo = (NewYahooWeather) msg.obj;
                    if (titleWeatherCity != null && titleWeatherImage != null && weatherInfo != null) {
                        mWeatherCode = (int) weatherInfo.getCode();
                        int temp = (int) ((weatherInfo.getTemp() - 32) / 1.8);
                        titleWeatherTemperature.setText(temp + "ºC");
                        titleWeatherCity.setText(weatherInfo.getCity());
                        titleWeatherInfo.setText(weatherInfo.getDesc());
                        titleWeatherTemperature.setVisibility(View.VISIBLE);
                        if (mWeatherCode >= 0 && mWeatherCode <= 47) {
                            titleWeatherImage.setImageResource(Data.getWeatherIcon(mWeatherCode));//设置通过weathercode设置已经在本地的天气图片
                        } else {
                            titleWeatherImage.setImageResource(R.mipmap.weather3200);
                        }
                        GET_WEATHER_OK = true;
                    } else {
                        if (titleWeatherCity != null && titleWeatherImage != null) {
                            // weather_city1.setText("Sunny to cloudy");
                            //   weathrer_temperature1.setText("28");
                            //  weather_info1.setText("走到这里");
                            initWeather();
                        }
                    }
                }
                break;
                case WeatherUtils.MSG_WEATHER_NO_CITY: {
                    if (titleWeatherCity != null)
                        titleWeatherCity.setText(R.string.weather_no_city);
                    //  Util.setString(getActivity(), WeatherUtils.WEATHER_CITY, "empty");
                    break;
                }
                case WeatherUtils.MSG_WEATHER_OK: {
                    WeatherInfo weatherInfo = (WeatherInfo) msg.obj;
                    if (titleWeatherCity != null && titleWeatherImage != null && weatherInfo != null) {
                        mWeatherCode = weatherInfo.getCurrentCode();
                        int temp = (int) ((weatherInfo.getCurrentTemp() - 32) / 1.8);
                        titleWeatherTemperature.setText(temp + "ºC");
                        titleWeatherCity.setText(weatherInfo.getLocationCity());
                        titleWeatherInfo.setText(weatherInfo.getCurrentText());
                        titleWeatherTemperature.setVisibility(View.VISIBLE);
                        if (mWeatherCode >= 0 && mWeatherCode <= 47) {
                            titleWeatherImage.setImageResource(Data.getWeatherIcon(mWeatherCode));//设置通过weathercode设置已经在本地的天气图片
                        } else {
                            titleWeatherImage.setImageResource(R.mipmap.weather3200);
                        }
                        GET_WEATHER_OK = true;
                    } else {
                        if (titleWeatherCity != null && titleWeatherImage != null) {
                            // weather_city1.setText("Sunny to cloudy");
                            //   weathrer_temperature1.setText("28");
                            //  weather_info1.setText("走到这里");
                            initWeather();
                        }
                    }
                    break;
                }
            }
        }
    };

    public void showEditCityForWeatherDialog() {
        final EditText editor = new EditText(this);
        editor.setSingleLine();
        new AlertDialog.Builder(this).setTitle(R.string.weather_edit_city_dialog_tips).setIcon(android.R.drawable.ic_dialog_info).setView(editor).setPositiveButton(R.string.weather_edit_city_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String _location = editor.getText().toString();
                if (!TextUtils.isEmpty(_location)) {
                    InputMethodManager imm = (InputMethodManager) editor.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);
                    WeatherUtils.updateWeather(MainActivity.this, mUpdateWeatherHandler, _location);
                    Util.setString(MainActivity.this, WeatherUtils.WEATHER_CITY, _location);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.weather_edit_city_dialog_not_empty, Toast.LENGTH_LONG).show();
                }
            }
        }).show();
    }

    private class WeatherReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                GET_WEATHER_OK = false;
                initWeather();
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();//注释父类方法，拦截回退键
    }

    /**
     * 通过包名跳转
     *
     * @param pkg
     */
    private void startThirdAppforPM(String pkg) {
        try {
            startActivity(pm.getLaunchIntentForPackage(pkg));
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.noapp), Toast.LENGTH_SHORT).show();
        }
    }
}
