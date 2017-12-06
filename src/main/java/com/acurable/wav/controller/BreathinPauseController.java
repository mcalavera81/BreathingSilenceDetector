package com.acurable.wav.controller;

import com.acurable.wav.domain.BreathingPause;
import com.acurable.wav.domain.WavFile;
import com.acurable.wav.exception.WavFileException;
import com.acurable.wav.features.WavFileOps;
import com.acurable.wav.io.AudioFileIO;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BreathinPauseController {

    public static void extractBreathingPauses(String inputFilename,String outputFilename) throws IOException, WavFileException {

        WavFileOps wavFileOps = WavFileOps.newWavFileOps();

        List<String> fileNames = AudioFileIO.readAudioFileNames(inputFilename);
        List<WavFile> wavFiles = fileNames.stream().map(WavFile::newWavFile).collect(Collectors.toList());

        List<BreathingPause> breathingPauses = wavFileOps.detectBreathingPauses(wavFiles.toArray(new WavFile[wavFiles.size()]));
        AudioFileIO.persistPauses(outputFilename, breathingPauses);

    }

}
