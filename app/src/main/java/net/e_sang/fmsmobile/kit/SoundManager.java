package net.e_sang.fmsmobile.kit;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import net.e_sang.fmsmobile.R;

public class SoundManager {

    private SoundPool soundPool;
    private int soundShutter;

    private float volume = 1.0f;

    public SoundManager(Context context) {

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attributes)
                .build();
        soundShutter = soundPool.load(context, R.raw.camera_shutter, 1);
    }

    public void setVolume(float volume){
        this.volume = volume;
    }

    public void playShutter(){
        soundPool.play(soundShutter, volume, volume, 1, 0, 1f);
    }

    public void release(){
        soundPool.release();
    }
}
