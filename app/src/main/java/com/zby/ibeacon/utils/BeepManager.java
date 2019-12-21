/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zby.ibeacon.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

/**
 * Manages beeps and vibrations for .
 */
public class BeepManager {

    private static final String TAG = BeepManager.class.getSimpleName();

    private static final float BEEP_VOLUME      = 0.10f;
    private static final long  VIBRATE_DURATION = 1000L;

    private final Activity activity;
    private       boolean  playBeep = true;
    private       boolean  vibrate  = true;
    private       Ringtone mRingtone;
    private       Vibrator mVibrator;

    public BeepManager(Activity activity) {
        this.activity = activity;
    }

    public synchronized void playBeepSoundAndVibrate() {
        if (playBeep) {
            if (mRingtone == null) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mRingtone = RingtoneManager.getRingtone(activity.getApplicationContext(), uri);
            }
            mRingtone.setLooping(true);
            mRingtone.play();
        }

        if (vibrate) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            //按照指定的模式去震动。这里的-1是指震动不连续，定义为0的话就代表一直震动下去
            vibrator.vibrate(new long[]{200,500,200,500}, 0);
        }
    }

    public void close() {
        stop();
        mVibrator = null;
        mRingtone = null;
    }

    public void stop() {
        if (mRingtone != null) {
            mRingtone.stop();
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }

    }

}
