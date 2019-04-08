package com.trigtop.gb.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.trigtop.gb.R;
import com.trigtop.gb.adapt.ShortcutsAdapter;
import com.trigtop.gb.bean.Shortcut;
import com.trigtop.gb.receiver.BroadcastReceiverListener;
import com.trigtop.gb.util.AnimUtil;
import com.trigtop.gb.util.DBHelper;
import com.trigtop.gb.util.Data;
import com.trigtop.gb.util.Util;
import com.trigtop.gb.widget.GridViewTV;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContentActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {

    @BindView(R.id.folder_id_gridview)
    GridViewTV gridViewTV;

    private View mOldView;
    public static int columns = 9;
    private PackageManager pm;
    private boolean isSelect = false;
//    private boolean isPaused = false;
    private ShortcutsAdapter mAdapter;
    private IntentFilter mIntentFilter;
    private String mCurrentCategory = "photo";
    private float scale = 1.1f;
    private final String ADDITIONAL = "additional";
    private List<Shortcut> mShortcut = new ArrayList<>();
    private PackageChangedReceiver mUpdateShortcutsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        init();
    }

    @Override
    public void onResume() {
        getData();
        mAdapter.refreshApps();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateShortcutsReceiver);

        super.onDestroy();
    }

    private void init() {
        gridViewTV.setOnItemClickListener(new ItemClickListener());
        gridViewTV.setOnItemSelectedListener(this);
        gridViewTV.setOnFocusChangeListener(this);

        mUpdateShortcutsReceiver = new PackageChangedReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Data.ACTION_UPDATE_SHORTCUTS);
        pm = getPackageManager();
        gridViewTV.setNumColumns(columns);
        mAdapter = new ShortcutsAdapter(this, mShortcut, pm, true);
        gridViewTV.setAdapter(mAdapter);
        registerReceiver(mUpdateShortcutsReceiver, mIntentFilter);
        setScreenListener();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view != null) {
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
            switch (v.getId()) {
                case R.id.folder_id_gridview:
//                    Log.i("bo", "gridview has focus");
                    new Thread(run).start();
                    break;

            }
        } else {
            switch (v.getId()) {
                case R.id.folder_id_gridview:
                    if (mOldView != null) {
                        AnimUtil.setViewScaleDefault(mOldView);
                    }
                    isSelect = false;
                    break;
            }
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
                Intent editIntent = new Intent(ContentActivity.this, EditorActivity.class);
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
                    e.printStackTrace();
                }
            }
        }
    }

    private class PackageChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getData();
        }

    }

    private void getData() {
        if (mCurrentCategory == null) {
            return;
        }
        if (mShortcut == null) {
            mShortcut = new ArrayList<>();
        }
        mShortcut.clear();
        mShortcut.addAll(DBHelper.getInstance(ContentActivity.this).queryByCategory(mCurrentCategory));
        Shortcut forAddItem = new Shortcut();
        forAddItem.setComponentName(ADDITIONAL);
        mShortcut.add(forAddItem);
        mAdapter.notifyDataSetChanged(mShortcut);
        gridViewTV.requestFocus();
    }

    Runnable run = new Runnable() {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    };

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 0:
                    if (isSelect) {
                        isSelect = false;

                    } else {
                        // 如果是第一次进入该gridView，则进入第一个item，如果不是第一次进去，则选择上次出来的item
                        if (mOldView == null) {
                            mOldView = gridViewTV.getChildAt(0);
                            if (mOldView != null){
                                AnimUtil.setViewScale(mOldView, scale);
                            }
                        }else {
                            AnimUtil.setViewScale(mOldView, scale);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_up_in, R.anim.activity_up_out);
    }

    private void setScreenListener() {
        BroadcastReceiverListener screenListener = new BroadcastReceiverListener(ContentActivity.this);
        screenListener.start(new BroadcastReceiverListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {// 开屏
            }
            @Override
            public void onScreenOff() {// 锁屏
            }
            @Override
            public void onUserPresent() {// 解锁
            }

            @Override
            public void onHome() {//home主页键
                finish();
                overridePendingTransition(R.anim.activity_up_in, R.anim.activity_up_out);
            }
        });
    }

}
