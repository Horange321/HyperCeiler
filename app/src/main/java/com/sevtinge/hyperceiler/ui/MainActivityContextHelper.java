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
package com.sevtinge.hyperceiler.ui;

import static com.sevtinge.hyperceiler.utils.devicesdk.DeviceSDKKt.getLanguage;
import static com.sevtinge.hyperceiler.utils.log.XposedLogUtils.logE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.provider.Settings;

import com.sevtinge.hyperceiler.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivityContextHelper {

    private Context context;

    public MainActivityContextHelper(Context context) {
        this.context = context;
    }

    @SuppressLint("HardwareIds")
    public String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getSHA256Signature() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);

            byte[] cert = info.signingInfo.getApkContentsSigners()[0].toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] publicKey = md.digest(cert);
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                    .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                if (i < publicKey.length - 1) hexString.append(":");
            }
            return hexString.toString();
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isSignCheckPass() {
        return false;
    }

    public String getRandomTip() {
        AssetManager assetManager = context.getAssets();
        String fileName = "tips/tips-" + getLanguage();
        List<String> tipsList = new ArrayList<>();

        try {
            InputStream inputStream;
            try {
                inputStream = assetManager.open(fileName);
            } catch (IOException ex) {
                inputStream = assetManager.open("tips/tips");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("//")) {
                    tipsList.add(line);
                }
            }

            reader.close();
            inputStream.close();

            Random random = new Random();
            String randomTip = "";
            while (randomTip.isEmpty() && !tipsList.isEmpty()) {
                int randomIndex = random.nextInt(tipsList.size());
                randomTip = tipsList.get(randomIndex);
                tipsList.remove(randomIndex);
            }

            if (!randomTip.isEmpty()) {
                return randomTip;
            } else {
                return "Get random tip is empty.";
            }
        } catch (IOException e) {
            logE("MainActivityContextHelper", "getRandomTip() error: " + e.getMessage());
            return "error";
        }
    }
}
