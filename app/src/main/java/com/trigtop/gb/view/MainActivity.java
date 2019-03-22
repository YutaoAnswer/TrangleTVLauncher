package com.trigtop.gb.view;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trigtop.gb.R;
import com.trigtop.gb.util.ContantUtil;
import com.trigtop.gb.util.DateUtil;
import com.trigtop.gb.widget.DrawingOrderRelativeLayout;
import com.trigtop.gb.widget.MainUpView;
import com.trigtop.gb.widget.MaulCarouselAdapter;
import com.trigtop.gb.widget.MetroViewBorderHandler;
import com.trigtop.gb.widget.MetroViewBorderImpl;
import com.trigtop.gb.widget.TvRecyclerView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

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
    @BindView(R.id.tv_recycler_view)
    TvRecyclerView tvRecyclerView;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MSG_ONE = 1;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ONE:
                    setDateTimeWeek();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_main);
        ButterKnife.bind(this);
        initView();
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

        GridLayoutManager manager = new GridLayoutManager(this, 1);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        manager.supportsPredictiveItemAnimations();
        tvRecyclerView.setLayoutManager(manager);
        int itemSpace = getResources().
                getDimensionPixelSize(R.dimen.recyclerView_item_space1);
        tvRecyclerView.addItemDecoration(new SpaceItemDecoration(itemSpace));
        DefaultItemAnimator animator = new DefaultItemAnimator();
        tvRecyclerView.setItemAnimator(animator);
        final MaulCarouselAdapter mAdapter = new MaulCarouselAdapter(this);
        tvRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemStateListener(new MaulCarouselAdapter.OnItemStateListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, ContantUtil.TEST_DATAS[position],
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * set system time to the TextView
     */
    @SuppressLint("SetTextI18n")
    private void setDateTimeWeek() {
        String systemDate = DateUtil.getSystemDate();
        Log.d(TAG, "setDateTimeWeek: " + systemDate);
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
}
