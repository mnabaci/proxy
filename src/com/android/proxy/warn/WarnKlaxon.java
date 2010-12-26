package com.android.proxy.warn;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.proxy.R;

/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play
 * if another activity overrides the AlarmAlert dialog.
 */
public class WarnKlaxon extends Service {

    /** Play alarm up to 10 minutes before silencing */
    
    private static final String TAG = "WarnKlaxon";
    private static final boolean DEBUG = true;
    
    public static final String ACTION = "com.android.proxy.warn.ALARM_ALERT";
    
    private static final int ALARM_TIMEOUT_SECONDS = 10;
    private static final int NOTIFY_TIMEOUT_SECONDS = 3;

    private static final long[] sVibratePattern = new long[] { 500, 500 };

    private boolean mPlaying = false;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Warn mCurrentWarn;
    private long mStartTime;
    private TelephonyManager mTelephonyManager;
    private int mInitialCallState;

    // Internal messages
    private static final int KILLER = 1000;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                    stopSelf();
                    break;
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if (state != TelephonyManager.CALL_STATE_IDLE
                    && state != mInitialCallState) {
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        WarnAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
        stop();
        // Stop listening for incoming calls.
        mTelephonyManager.listen(mPhoneStateListener, 0);
        WarnAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean sound = intent.getBooleanExtra(WarnProvider.SOUND, false);
        boolean vibrate = intent.getBooleanExtra(WarnProvider.VIBRATE, false);
        int showType = intent.getIntExtra(WarnProvider.SHOW_TYPE, WarnManager.SHOW_TYPE_DIALOG);
        play(sound, vibrate, showType);
        // Record the initial call state here so that the new alarm has the
        // newest state.
        mInitialCallState = mTelephonyManager.getCallState();

        return START_STICKY;
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private void play(boolean sound, boolean vibrate, int showType) {
        // stop() checks to see if we are already playing.
        LOGD("play, sound=" + sound + ",vibrate=" + vibrate);
        stop();

        if (sound) {
            Uri alert = null;
            if (showType == WarnManager.SHOW_TYPE_DIALOG) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            } else if (showType == WarnManager.SHOW_TYPE_NOTIFY) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
            // RingtoneManager.
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "Error occurred while playing audio.");
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });

            try {
                // Check if we are in a call. If we are, use the in-call alarm
                // resource at a low volume to not disrupt the call.
                if (mTelephonyManager.getCallState()
                        != TelephonyManager.CALL_STATE_IDLE) {
                    Log.v(TAG, "Using the in-call alarm");
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            R.raw.in_call_alarm);
                } else {
                    mMediaPlayer.setDataSource(this, alert);
                }
                startAlarm(mMediaPlayer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /* Start the vibrator after everything is ok with the media player */
        if (vibrate) {
            mVibrator.vibrate(sVibratePattern, 0);
        } else {
            mVibrator.cancel();
        }

        enableKiller(showType);
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    // Do the common stuff when starting the alarm.
    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException,
                   IllegalStateException {
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }

    private void setDataSourceFromResource(Resources resources,
            MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not
     * repeating
     */
    public void stop() {
        Log.v(TAG, "WarnKlaxon.stop()");
        if (mPlaying) {
            mPlaying = false;

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        }
        disableKiller();
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     *
     * This just cancels the audio, but leaves the notification
     * popped, so the user will know that the alarm tripped.
     */
    private void enableKiller(int showType) {
        int timeout;
        if (showType == WarnManager.SHOW_TYPE_DIALOG) {
            timeout = ALARM_TIMEOUT_SECONDS;
        } else {
            timeout = NOTIFY_TIMEOUT_SECONDS;
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER),
                1000 * timeout);
    }

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }

    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}

