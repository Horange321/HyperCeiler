package com.sevtinge.hyperceiler.module.app;

import com.sevtinge.hyperceiler.module.base.BaseModule;
import com.sevtinge.hyperceiler.module.hook.demo.ToastTest;

public class Demo extends BaseModule {
    @Override
    public void handleLoadPackage() {
        initHook(new ToastTest(), true);
    }
}
