package com.sevtinge.cemiuiler.ui.systemui.statusbar;

import androidx.fragment.app.Fragment;

import com.sevtinge.cemiuiler.R;
import com.sevtinge.cemiuiler.ui.base.SubFragment;
import com.sevtinge.cemiuiler.ui.systemui.base.BaseSystemUIActivity;

public class ClockIndicatorActivity extends BaseSystemUIActivity {

    @Override
    public Fragment initFragment() {
        return new ClockIndicatorActivity.ClockIndicatorFragment();
    }

    public static class ClockIndicatorFragment extends SubFragment {

        @Override
        public int getContentResId() {
            return R.xml.system_ui_status_bar_clock_indicator;
        }
    }
}