package com.acurable.wav.features;

import com.acurable.wav.domain.BreathingPause;
import com.acurable.wav.domain.WavFile;
import com.acurable.wav.domain.WavFileIterator;
import com.acurable.wav.exception.WavFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;



interface WavFileVolume {

    Logger logger = LoggerFactory.getLogger(WavFileVolume.class);

    List<BreathingPause> detectBreathingPauses(WavFile... wavFiles) throws IOException, WavFileException;

    default List<BreathingPause> _detect_breathing_pauses(double sampling , double threshold, WavFile wavFile) throws IOException, WavFileException {
        logger.info("Starting processing file: {}", wavFile.getFilepath());


        WavFileIterator wavIter = WavFileIterator.newIterator(wavFile);
        List<BreathingPause> pauses = new ArrayList<>();

        int framesPerSample= (int) (sampling*wavIter.getFmtData().getSampleRate());

        BiFunction<Double, Double, Boolean> isCrossingDown = (currentValue,currentTime)-> currentValue < threshold && currentTime==-1;
        BiFunction<Double, Double, Boolean> isCrossingUp = (currentValue,currentTime)-> currentValue > threshold && currentTime!=-1;

        double[] buffer = new double[framesPerSample * wavIter.getFmtData().getNumChannels()];
        double startTime=-1;
        for(int sampleIndex=0, pauseIndex=0; wavIter.readFrames(buffer, framesPerSample)!=0; sampleIndex++){
            double v = volumeRMS(buffer);
            double currentTime = wavIter.getFmtData().getFrameTime(framesPerSample * sampleIndex);
            if(isCrossingDown.apply(v,startTime)){
                startTime=currentTime;
            }else if(isCrossingUp.apply(v,startTime)){
                pauses.add(BreathingPause.newBreathingPause(wavIter.getFilePath(),pauseIndex, startTime, currentTime));
                pauseIndex++;
                startTime=-1;
            }

        }

        wavIter.close();

        logger.info("Finished processing file: {}", wavFile.getFilepath());
        return pauses;
    }


    /** Computes the RMS volume of a group of signal sizes ranging from -1 to 1. */
    static double volumeRMS(double[] raw) {


        double sum = 0d;
        if (raw.length==0) {
            return sum;
        } else {
            for (double aRaw : raw) {
                sum += aRaw;
            }
        }
        double average = sum/raw.length;

        double sumMeanSquare = 0d;
        for (double aRaw : raw) {
            sumMeanSquare += Math.pow(aRaw-average,2d);
        }
        double averageMeanSquare = sumMeanSquare/raw.length;

        return Math.sqrt(averageMeanSquare);
    }

}
