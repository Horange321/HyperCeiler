/*
 * This file is part of HyperCeiler.

 * HyperCeiler is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 HyperCeiler Contributions
 */
package com.sevtinge.hyperceiler.ui.fragment;

import static com.sevtinge.hyperceiler.utils.DisplayUtils.dip2px;
import static com.sevtinge.hyperceiler.utils.DisplayUtils.sp2px;
import static com.sevtinge.hyperceiler.utils.api.VoyagerApisKt.isPad;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.getBaseOs;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.getRomAuthor;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isAndroidVersion;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isMoreHyperOSVersion;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sevtinge.hyperceiler.R;
import com.sevtinge.hyperceiler.prefs.PreferenceHeader;
import com.sevtinge.hyperceiler.prefs.RecommendPreference;
import com.sevtinge.hyperceiler.prefs.TipsPreference;
import com.sevtinge.hyperceiler.ui.MainActivityContextHelper;
import com.sevtinge.hyperceiler.ui.fragment.base.SettingsPreferenceFragment;
import com.sevtinge.hyperceiler.ui.fragment.helper.CantSeeAppsFragment;
import com.sevtinge.hyperceiler.ui.fragment.helper.HomepageEntrance;
import com.sevtinge.hyperceiler.utils.PackagesUtils;
import com.sevtinge.hyperceiler.utils.ThreadPoolManager;
import com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt;
import com.sevtinge.hyperceiler.utils.log.AndroidLogUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Objects;

import moralnorm.preference.Preference;

public class MainFragment extends SettingsPreferenceFragment implements HomepageEntrance.EntranceState {

    Preference mCamera;
    Preference mCameraNew;
    Preference mPowerSetting;
    Preference mMTB;
    Preference mSecurityCenter;
    Preference mMiLink;
    Preference mAod;
    Preference mGuardProvider;
    Preference mMirror;
    Preference mHeadtipWarn;
    Preference mHelpCantSeeApps;
    TipsPreference mTips;
    MainActivityContextHelper mainActivityContextHelper;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x11) {
                mTips.updateTips();
                removeMessages(0x11);
                sendEmptyMessageDelayed(0x11, 6000);
            }
        }
    };
    private final String TAG = "MainFragment";
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Message message = mHandler.obtainMessage(0x11);
        mHandler.sendMessageDelayed(message, 6000);
    }

    @Override
    public int getContentResId() {
        return R.xml.prefs_main;
    }

    @Override
    public void initPrefs() {
        HomepageEntrance.setEntranceStateListen(this);
        Resources resources = getResources();
        ThreadPoolManager.getInstance().submit(() -> {
            try (XmlResourceParser xml = resources.getXml(R.xml.prefs_set_homepage_entrance)) {
                try {
                    int event = xml.getEventType();
                    while (event != XmlPullParser.END_DOCUMENT) {
                        if (event == XmlPullParser.START_TAG) {
                            if (xml.getName().equals("SwitchPreference")) {
                                String key = xml.getAttributeValue(ANDROID_NS, "key");
                                if (key != null) {
                                    String checkKey = key.replace("_state", "");
                                    boolean state = getSharedPreferences().getBoolean(key, true);
                                    if (!state) {
                                        PreferenceHeader preferenceHeader = findPreference(checkKey);
                                        if (preferenceHeader != null) {
                                            boolean visible = preferenceHeader.isVisible();
                                            if (visible) {
                                                preferenceHeader.setVisible(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        event = xml.next();
                    }
                } catch (XmlPullParserException | IOException e) {
                    AndroidLogUtils.logE(TAG, "An error occurred when reading the XML:", e);
                }
            }
        });
        mCamera = findPreference("prefs_key_camera");
        mCameraNew = findPreference("prefs_key_camera_new");
        mPowerSetting = findPreference("prefs_key_powerkeeper");
        mMTB = findPreference("prefs_key_mtb");
        mSecurityCenter = findPreference("prefs_key_security_center");
        mMiLink = findPreference("prefs_key_milink");
        mAod = findPreference("prefs_key_aod");
        mGuardProvider = findPreference("prefs_key_guardprovider");
        mMirror = findPreference("prefs_key_mirror");
        mTips = findPreference("prefs_key_tips");
        mHeadtipWarn = findPreference("prefs_key_headtip_warn");
        mHelpCantSeeApps = findPreference("prefs_key_help_cant_see_app");

        mHelpCantSeeApps.setVisible(!getSharedPreferences().getBoolean("prefs_key_help_cant_see_apps_switch", false));

        mCamera.setVisible(!isMoreHyperOSVersion(1f) && !PackagesUtils.checkAppStatus(getContext(), "com.android.camera"));
        mCameraNew.setVisible(isMoreHyperOSVersion(1f) && !PackagesUtils.checkAppStatus(getContext(), "com.android.camera"));
        mPowerSetting.setVisible(!isAndroidVersion(30) && !PackagesUtils.checkAppStatus(getContext(), "com.miui.powerkeeper"));
        mMTB.setVisible(!isAndroidVersion(30) && !PackagesUtils.checkAppStatus(getContext(), "com.xiaomi.mtb"));

        if (isMoreHyperOSVersion(1f)) {
            mAod.setTitle(R.string.aod_hyperos);
            mMiLink.setTitle(R.string.milink_hyperos);
            mGuardProvider.setTitle(R.string.guard_provider_hyperos);
            mMirror.setTitle(R.string.mirror_hyperos);
            mSecurityCenter.setTitle(R.string.security_center_hyperos);
        } else {
            mAod.setTitle(R.string.aod);
            mMiLink.setTitle(R.string.milink);
            mGuardProvider.setTitle(R.string.guard_provider);
            mMirror.setTitle(R.string.mirror);
            if (isPad()) {
                mSecurityCenter.setTitle(R.string.security_center_pad);
            } else {
                mSecurityCenter.setTitle(R.string.security_center);
            }
        }

        mainActivityContextHelper = new MainActivityContextHelper(requireContext());

        isOfficialRom();
        if (!getIsOfficialRom()) isSignPass();

        mTips = findPreference("prefs_key_tips");
    }

    public void isOfficialRom() {
        mHeadtipWarn.setTitle(R.string.headtip_warn_not_offical_rom);
        mHeadtipWarn.setVisible(getIsOfficialRom());
    }

    public boolean getIsOfficialRom() {
        return (
                !getBaseOs().startsWith("V") &&
                        !getBaseOs().startsWith("Xiaomi") &&
                        !getBaseOs().startsWith("Redmi") &&
                        !getBaseOs().startsWith("POCO") &&
                        !getBaseOs().isEmpty()
        ) ||
                !getRomAuthor().isEmpty() ||
                Objects.equals(SystemSDKKt.getHost(), "xiaomi.eu") ||
                (
                        !SystemSDKKt.getHost().startsWith("pangu-build-component-system") &&
                                !SystemSDKKt.getHost().startsWith("non-pangu-pod-g0sww")
                );
    }


    public void isSignPass() {
        mHeadtipWarn.setTitle(R.string.headtip_warn_sign_verification_failed);
        mHeadtipWarn.setVisible(!mainActivityContextHelper.isSignCheckPass());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(moralnorm.preference.R.id.recycler_view);
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets inset = Insets.max(insets.getInsets(WindowInsetsCompat.Type.systemBars()),
                        insets.getInsets(WindowInsetsCompat.Type.displayCutout()));
                // 22dp + 2dp + 12sp + 10dp + 18dp + 0.5dp + inset.bottom + 4dp(?)
                v.setPadding(inset.left, 0, inset.right, inset.bottom + dip2px(requireContext(), 56.5F) + sp2px(requireContext(), 12));
                return insets;
            }
        });
    }

    @Override
    public void onEntranceStateChange(String key, boolean state) {
        String mainKey = key.replace("_state", "");
        PreferenceHeader preferenceHeader = findPreference(mainKey);
        if (preferenceHeader != null) {
            boolean last = preferenceHeader.isVisible();
            if (!last || state) return;
            preferenceHeader.setVisible(false);
        }
    }
}
