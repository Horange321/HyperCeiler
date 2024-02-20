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
package com.sevtinge.hyperceiler.view;

import static com.sevtinge.hyperceiler.utils.api.VoyagerApisKt.isPad;
import static com.sevtinge.hyperceiler.utils.devicesdk.SystemSDKKt.isMoreHyperOSVersion;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.sevtinge.hyperceiler.R;
import com.sevtinge.hyperceiler.module.app.GlobalActions;
import com.sevtinge.hyperceiler.utils.shell.ShellInit;

import java.util.Arrays;
import java.util.List;

import moralnorm.appcompat.app.AlertDialog;

public class RestartAlertDialog extends AlertDialog {

    List<String> mAppNameList;
    List<String> mAppPackageNameList;

    public RestartAlertDialog(Context context) {
        super(context);
        setTitle(R.string.hyperceiler_restart_quick);
        setView(createMultipleChoiceView(context));
    }

    private MultipleChoiceView createMultipleChoiceView(Context context) {
        Resources mRes = context.getResources();
        MultipleChoiceView view = new MultipleChoiceView(context);
        mAppNameList = Arrays.asList(mRes.getStringArray(!isMoreHyperOSVersion(1f) ? (!isPad() ? R.array.restart_apps_name : R.array.restart_apps_name_pad) : R.array.restart_apps_name_hyperos));
        mAppPackageNameList = Arrays.asList(mRes.getStringArray(R.array.restart_apps_packagename));
        view.setData(mAppNameList, null);
        view.deselectAll();
        view.setOnCheckedListener(sparseBooleanArray -> {
            dismiss();
            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                if (sparseBooleanArray.get(i)) {
                    // ShellUtils.execCommand("pkill -l 9 -f " + mAppPackageNameList.get(i), true, false);
                    // String test = "XX";
                    String packageGet = mAppPackageNameList.get(i);
                    ShellInit.getShell().add("pid=$(pgrep -f \"" + packageGet + "\" | grep -v $$)")
                        .add("if [[ $pid == \"\" ]]; then")
                        .add(" pids=\"\"")
                        .add(" pid=$(ps -A -o PID,ARGS=CMD | grep \"" + packageGet + "\" | grep -v \"grep\")")
                        .add("  for i in $pid; do")
                        .add("   if [[ $(echo $i | grep '[0-9]' 2>/dev/null) != \"\" ]]; then")
                        .add("    if [[ $pids == \"\" ]]; then")
                        .add("      pids=$i")
                        .add("    else")
                        .add("      pids=\"$pids $i\"")
                        .add("    fi")
                        .add("   fi")
                        .add("  done")
                        .add("fi")
                        .add("if [[ $pids != \"\" ]]; then")
                        .add(" pid=$pids")
                        .add("fi")
                        .add("if [[ $pid != \"\" ]]; then")
                        .add(" for i in $pid; do")
                        .add("  kill -s 15 $i &>/dev/null")
                        .add(" done")
                        .add("else")
                        .add(" echo \"No Find Pid!\"")
                        .add("fi").over().sync();
                }
            }
        });
        return view;
    }

    public void restartApp(Context context, String packageName) {
        Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "RestartApps");
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
    }

    public void restartSystemUI(Context context) {
        Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "RestartSystemUI");
        intent.setPackage("com.android.systemui");
        context.sendBroadcast(intent);
    }
}
