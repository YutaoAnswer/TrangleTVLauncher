package com.trigtop.gb.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.VideoView;

import com.trigtop.gb.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LauncherActivity extends Activity implements MediaPlayer.OnCompletionListener {

    @BindView(R.id.videoview)
    VideoView videoview;
    @BindView(R.id.iv_launcher)
    ImageView ivLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //todo ImageView设置渐变退出， VideoView设置渐变进入， MainActivity 设置进入动画方式
                ivLauncher.setVisibility(View.GONE);
                videoview.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_launcher);
                videoview.setVideoURI(uri);
                videoview.start();
                videoview.requestFocus();
                videoview.setOnCompletionListener(LauncherActivity.this);
            }
        }, 1000);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
