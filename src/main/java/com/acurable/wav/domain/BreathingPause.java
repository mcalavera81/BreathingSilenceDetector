package com.acurable.wav.domain;


import com.acurable.wav.config.AppConfig;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BreathingPause {



    public BreathingPause() {}
    private BreathingPause(String filePath, int pauseIndex, double start, double end, double duration, Type pause_type) {
        this.filePath = filePath;
        this.pauseIndex = pauseIndex;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.type = pause_type;
    }

    //file path to the actual audio file
    String filePath;

    // Sequence asigned to each pause detected in the order they appear in the audio file.
    int pauseIndex;

    // The index, masured in seconds from the start of the audio recording, to the beginning of the pause.
    double start;

    //it's the index, masured in seconds from the start of the audio recording, to the end of the pause.
    double end;

    double duration;

    //the type of pause, normal or apnoea.
    Type type;

    public enum Type {NORMAL, APNOEA}


    public static BreathingPause newBreathingPause(String filePath,int pauseIndex, double startTime, double endTime){
        double duration=endTime-startTime;
        /*
         * To be able to classify correctly a machine learning algorithm would be needed
         * plus a decent sample of pauses correctly classified for the training stage
         *
         * To keep it simple will use a fixed time timeThreshold above which will determine the pause as apnoea.
         *
         */

        Type pause_type= (duration > AppConfig.timeThreshold) ? Type.APNOEA : Type.NORMAL;
        return new BreathingPause(filePath, pauseIndex, startTime, endTime, duration, pause_type);
    }

}
