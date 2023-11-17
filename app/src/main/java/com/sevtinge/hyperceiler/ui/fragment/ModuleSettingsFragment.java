package com.sevtinge.hyperceiler.ui.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.sevtinge.hyperceiler.R;
import com.sevtinge.hyperceiler.ui.LauncherActivity;
import com.sevtinge.hyperceiler.ui.fragment.base.SettingsPreferenceFragment;
import com.sevtinge.hyperceiler.utils.BackupUtils;
import com.sevtinge.hyperceiler.utils.DialogHelper;
import com.sevtinge.hyperceiler.utils.PrefsUtils;
import com.sevtinge.hyperceiler.view.RestartAlertDialog;

import moralnorm.appcompat.app.AppCompatActivity;
import moralnorm.preference.DropDownPreference;
import moralnorm.preference.MultiSelectListPreference;
import moralnorm.preference.Preference;
import moralnorm.preference.SwitchPreference;

public class ModuleSettingsFragment extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener {

    DropDownPreference mIconModePreference;
    DropDownPreference mIconModeValue;
    SwitchPreference mHideAppIcon;

    @Override
    public int getContentResId() {
        return R.xml.prefs_settings;
    }

    @Override
    public void initPrefs() {
        int mIconMode = Integer.parseInt(PrefsUtils.getSharedStringPrefs(getContext(), "prefs_key_settings_icon", "0"));
        mIconModePreference = findPreference("prefs_key_settings_icon");
        mIconModeValue = findPreference("prefs_key_settings_icon_mode");
        mHideAppIcon = findPreference("prefs_key_settings_hide_app_icon");

        setIconMode(mIconMode);
        mIconModePreference.setOnPreferenceChangeListener(this);

        if (mHideAppIcon != null) {
            mHideAppIcon.setOnPreferenceChangeListener((preference, o) -> {

                PackageManager pm = requireActivity().getPackageManager();
                int mComponentEnabledState;

                if ((Boolean) o) {
                    mComponentEnabledState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                } else {
                    mComponentEnabledState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                }

                pm.setComponentEnabledSetting(new ComponentName(requireActivity(), LauncherActivity.class), mComponentEnabledState, PackageManager.DONT_KILL_APP);
                return true;
            });
        }

        findPreference("prefs_key_back").setOnPreferenceClickListener(preference -> {
            final AppCompatActivity activity = (AppCompatActivity) getActivity();
            backupSettings(activity);
            return true;
        });

        findPreference("prefs_key_rest").setOnPreferenceClickListener(preference -> {
            restoreSettings(getActivity());
            return true;
        });

        findPreference("prefs_key_reset").setOnPreferenceClickListener(preference -> {
            DialogHelper.showDialog(getActivity(), R.string.reset_title, R.string.reset_desc, (dialog, which) -> {
                PrefsUtils.mSharedPreferences.edit().clear().apply();
                Toast.makeText(getActivity(), R.string.reset_okay, Toast.LENGTH_LONG).show();
            });
            return true;
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == mIconModePreference) {
            setIconMode(Integer.parseInt((String) o));
        }
        return true;
    }

    private void setIconMode(int mode) {
        mIconModeValue.setVisible(mode != 0);
    }

    public void backupSettings(Activity activity) {
        BackupUtils.backup(activity);
    }

    public void restoreSettings(Activity activity) {
        BackupUtils.restore(activity);
    }
}
