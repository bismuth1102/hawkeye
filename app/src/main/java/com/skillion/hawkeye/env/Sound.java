package com.skillion.hawkeye.env;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.R;
import com.skillion.hawkeye.enums.SoundOption;

public class Sound {
    private SoundPool.Builder builder = null;
    private SoundPool soundPool = null;
    private int voiceId;
    private int streamID;
    private static Sound instance;
    private SoundOption option = SoundOption.Beep;

    public static Sound getInstance(Context application){
        if (instance==null){
            instance = new Sound(application);
        }
        return instance;
    }

    private Sound(Context application){
        builder = new SoundPool.Builder();
        //传入最多播放音频数量,
        builder.setMaxStreams(1);
        //AudioAttributes是一个封装音频各种属性的方法
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        //设置音频流的合适的属性
        attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
        //加载一个AudioAttributes
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();
        voiceId = soundPool.load(application, R.raw.beep, 1);

        //异步需要等待加载完成，音频才能播放成功
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    //第一个参数soundID
                    //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
                    //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
                    //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
                    //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
                    //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
                    streamID = soundPool.play(voiceId, 1, 1, 1, -1, 1);
                }
            }
        });
        soundPool.pause(streamID);
    }


    public void resumeSound(){
        soundPool.resume(streamID);
    }

    public void pauseSound(){
        soundPool.pause(streamID);
    }

    public void stopSound(){
        soundPool.stop(streamID);
    }

    private void startSound(){
        streamID = soundPool.play(voiceId, 1, 1, 1, -1, 1);
        pauseSound();
    }


    public void setAndStartSoundbyOption(SoundOption option){
        this.option = option;
        startSoundbyOption();
    }


    public void startSoundbyOption(){
        switch (option){
            case Beep:
            case Beel:
            case Chime:
                startSound();
                break;
            case Off:
                stopSound();
                break;
        }
    }

    public SoundOption getSoundOption(){
        return option;
    }
}
