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
package com.sevtinge.hyperceiler.module.hook.securitycenter

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.sevtinge.hyperceiler.module.base.BaseHook
import com.sevtinge.hyperceiler.utils.getObjectField


object GetBubbleAppString : BaseHook() {
    override fun init() {
        try {
            val classBubble = loadClass("com.miui.bubbles.Bubble")
            loadClass("com.miui.bubbles.settings.BubblesSettings").methodFinder().first {
                name == "getBubbleAppString"
            }.createHook {
                before {
                    val stringBuilder = StringBuilder()
                    val mActiveBubbles = it.thisObject.getObjectField("mActiveBubbles")
                    for (bubble in mActiveBubbles as HashSet<*>) {
                        stringBuilder.append(
                            classBubble.getMethod("getPackageName").invoke(bubble)
                        )
                        stringBuilder.append(":")
                        stringBuilder.append(bubble.getObjectField("userId"))
                        stringBuilder.append(",")
                    }
                    // XposedBridge.log("MaxFreeFormTest: getBubbleAppString called! Result:$stringBuilder")
                    it.result = stringBuilder.toString()
                }
            }
        } catch (e: Throwable) {
            logE(TAG, this.lpparam.packageName, e)
        }
    }

}
