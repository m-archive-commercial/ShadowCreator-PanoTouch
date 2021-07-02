package com.luci.pano_touch.screen_mirror;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableFloat;

import com.luci.pano_touch.R;

public class TrackBallBean {
    public ObservableFloat x;
    public ObservableFloat y;
    public ObservableBoolean vis;
    public int bgResourceId;

    public TrackBallBean(float x, float y) {
        this.x = new ObservableFloat(x);
        this.y = new ObservableFloat(y);
        this.vis = new ObservableBoolean(false);
        this.bgResourceId = R.drawable.track_ball;
    }
}
