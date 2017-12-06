package com.acurable.wav.features;

import com.acurable.wav.domain.WavFile;
import com.acurable.wav.domain.WavFileIterator;
import com.acurable.wav.exception.WavFileException;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.lang.System.out;

interface WavFileDescriptor {

    void describe(WavFile wavFile) throws IOException, WavFileException;

    default void _describe(WavFile wavFile) throws IOException, WavFileException {

        WavFileIterator iterator = WavFileIterator.newIterator(wavFile);
        out.printf("***********************************\n");
        out.printf("File: %s\n", iterator.getFilePath());
        out.printf("Size: %s Kbytes\n", iterator.getSize());

        out.printf("Channels: %d, Frames: %d\n", iterator.getFmtData().getNumChannels(), iterator.getNumFrames());
        out.printf("IO State: %s\n", iterator.getIoState());
        out.printf("Sample Rate: %d, Block Align: %d\n", iterator.getFmtData().getSampleRate(), iterator.getFmtData().getBlockAlign());
        out.printf("Bit Rate: %d kb/s\n", iterator.getBitRate());

        LocalTime t = LocalTime.MIDNIGHT.plus(iterator.getDuration());
        String s = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(t);
        out.printf("Duration: %s\n",s);
        out.printf("Valid Bits: %d, Bytes per sample: %d\n", iterator.getFmtData().getValidBits(), iterator.getBytesPerSample());
        out.printf("***********************************\n");
        iterator.close();

    }

}
