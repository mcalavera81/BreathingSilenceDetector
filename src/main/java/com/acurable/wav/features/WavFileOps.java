package com.acurable.wav.features;

import com.acurable.wav.config.AppConfig;
import com.acurable.wav.domain.BreathingPause;
import com.acurable.wav.domain.WavFile;
import com.acurable.wav.exception.WavFileException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WavFileOps implements WavFileVolume, WavFileDescriptor {

    private final double SAMPLING_SEC;
    private final double THRESHOLD;

    private WavFileOps() {
        this.SAMPLING_SEC = AppConfig.sampling;
        this.THRESHOLD=AppConfig.signalThreshold;
    }

    public static WavFileOps newWavFileOps(){
        return new WavFileOps();
    }

    @Override
    public void describe(WavFile wavFile) throws IOException, WavFileException {
        WavFileDescriptor.super._describe(wavFile);
    }

    @Override
    public List<BreathingPause> detectBreathingPauses(WavFile... wavFiles) throws IOException, WavFileException {
        List<BreathingPause> allPauses = new ArrayList<>();

        for (WavFile wavFile : wavFiles) {
            describe(wavFile);
            List<BreathingPause> breathingPauses = _detect_breathing_pauses(SAMPLING_SEC, THRESHOLD, wavFile);
            allPauses.addAll(breathingPauses);
        }

        return allPauses;
    }
}
