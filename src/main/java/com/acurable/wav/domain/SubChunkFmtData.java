package com.acurable.wav.domain;

import com.acurable.wav.exception.WavFileException;
import lombok.Getter;

@Getter
public class SubChunkFmtData {

    public static SubChunkFmtData build(int numChannels, int sampleRate, int blockAlign, int validBits) throws WavFileException {
        if (numChannels == 0) throw new WavFileException("Number of channels specified in header is equal to zero");
        if (blockAlign == 0) throw new WavFileException("Block Align specified in header is equal to zero");
        if (validBits < 2) throw new WavFileException("Valid Bits specified in header is less than 2");
        if (validBits > 64) throw new WavFileException("Valid Bits specified in header is greater than 64, this is greater than a long can hold");

        return new SubChunkFmtData(numChannels, sampleRate, blockAlign, validBits);
    }

    private SubChunkFmtData(int numChannels, int sampleRate, int blockAlign, int validBits) {

        this.numChannels = numChannels;
        this.sampleRate = sampleRate;
        this.blockAlign = blockAlign;
        this.validBits = validBits;
    }


    // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    final int numChannels;

    // 4 bytes unsigned, 0x00000001 (1) to 0xFFFFFFFF (4,294,967,295)
    // Although a java int is 4 bytes, it is signed, so need to use a long
    final long sampleRate;

    //2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    final int blockAlign;

    // 2 bytes unsigned, 0x0002 (2) to 0xFFFF (65,535)
    final int validBits;


    // Calculate the number of bytes required to hold 1 sample
    public int getBytesPerSample() throws WavFileException {
        int bytesPerSample = (this.validBits + 7) / 8;
        if (bytesPerSample * numChannels != blockAlign)
            throw new WavFileException("Block Align does not agree with bytes required for validBits and number of channels");
        return bytesPerSample;
    }

    public long getNumberOfFrames(long chunkSize){
        // Calculate the number of frames
        return chunkSize / blockAlign;
    }

    public double getFrameTime(int frameOffset){
        return (double)frameOffset/sampleRate;
    }

}