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
package com.sevtinge.hyperceiler.module.hook.systemui;

import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isAndroidVersion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import com.sevtinge.hyperceiler.module.base.BaseHook;

import java.util.Locale;

import de.robv.android.xposed.XposedHelpers;

public class HideNavigationBar extends BaseHook {
    boolean run = false;

    @Override
    public void init() {
        /*启用隐藏*/
        if (isAndroidVersion(34)) {
            hookAllConstructors("com.android.systemui.statusbar.phone.NavigationModeControllerExt", new MethodHook() {
                @SuppressLint("PrivateApi")
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    super.after(param);
                    XposedHelpers.setStaticBooleanField(param.getClass().getClassLoader().loadClass("com.android.systemui.statusbar.phone.NavigationModeControllerExt"), "mHideGestureLine", true);
                }
            });
        } else {
            findAndHookMethod("com.android.systemui.statusbar.phone.NavigationModeControllerExt",
                "hideNavigationBar",
                new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) {
                        param.setResult(true);
                    }
                }
            );
        }

        /*不隐藏时创建手势条*/
        hookAllMethods("com.android.systemui.navigationbar.NavigationBarController",
            "createNavigationBar",
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    if (param.args.length >= 3) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        ContentObserver(mContext);
                        try {
                            int End = Settings.Global.getInt(mContext.getContentResolver(), "hide_gesture_line");
                            if (End == 1) {
                                Settings.Global.putInt(mContext.getContentResolver(), "hide_gesture_line", 0);
                                logI(TAG, HideNavigationBar.this.lpparam.packageName, "Settings The hide_gesture_line To 0");
                            }
                        } catch (Settings.SettingNotFoundException e) {
                            logW(TAG, HideNavigationBar.this.lpparam.packageName, "Don‘t Have hide_gesture_line");
                        }
                        param.setResult(null);
                    }
                }
            }
        );

        /*状态更改设置*/
        findAndHookMethod("com.android.systemui.statusbar.phone.MiuiDockIndicatorService",
            "onNavigationModeChanged", int.class,
            new MethodHook() {
                @Override
                protected void before(MethodHookParam param) {
                    XposedHelpers.setObjectField(param.thisObject, "mNavMode", param.args[0]);
                    if (XposedHelpers.getObjectField(param.thisObject, "mNavigationBarView") != null) {
                        XposedHelpers.callMethod(param.thisObject, "setNavigationBarView", (Object) null);
                    } else {
                        XposedHelpers.callMethod(param.thisObject, "checkAndApplyNavigationMode");
                    }
                    param.setResult(null);
                }
            }
        );
    }

    /*防呆专用*/
    public void ContentObserver(Context context) {
        if (!run) {
            run = true;
            ContentObserver contentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                @Override
                public void onChange(boolean z) {
                    boolean language = false;
                    Locale locale = Locale.getDefault();
                    String languageCode = locale.getLanguage();
                    if (languageCode.equals("zh")) language = true;
                    Settings.Global.putInt(context.getContentResolver(), "force_fsg_nav_bar", 1);
                    Toast.makeText(context, language ? "请勿切换经典导航键" : "Don't switch navigation keys", Toast.LENGTH_SHORT).show();
                    logI(TAG, HideNavigationBar.this.lpparam.packageName, "Please don't switch classic navigation keys");
                }
            };
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, contentObserver);
        }
    }
}
