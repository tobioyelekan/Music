package com.tobioyelekan.music;

/**
 * Created by TOBI OYELEKAN on 02/01/2018.
 */

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "com.tobioyelekan.music.action.main";
        String PREV_ACTION = "com.tobioyelekan.music.action.prev";
        String PLAY_ACTION = "com.tobioyelekan.music.action.play";
        String NEXT_ACTION = "com.tobioyelekan.music.action.next";
        String PLAY_INFO = "com.tobioyelekan.music.action.play_info";
        String STARTFOREGROUND_ACTION = "com.tobioyelekan.music.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.tobioyelekan.music.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 1;
    }
}
