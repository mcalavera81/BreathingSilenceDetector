package com.acurable.wav.domain;

import com.acurable.wav.exception.WavFileException;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;

import static com.acurable.wav.parser.WavFileParser.parseHeaders;


@Getter
@Setter
public class WavFileIterator {

    private final static int BUFFER_SIZE = 4096;
    String filePath;
    long length;

    private enum IOState {READING, CLOSED}

    int bytesPerSample;			// Number of bytes required to store a single sample
    long numFrames;					// Number of frames within the data section


    // Input stream used for reading data
    InputStream iStream;

    // Specifies the IO State of the Wav File (used for sanity checking)
    IOState ioState;


    SubChunkFmtData fmtData;
    ScalingData scalingData;

    // Buffering

    //Local buffer used for IO
    byte[] buffer;

    //Buffer iteration status
    IteratorStatus iterStatus;


    // Bit rate in kb/s
    public long getBitRate(){ return  fmtData.sampleRate* fmtData.numChannels*fmtData.validBits/1000; }

    // File size in Kbytes
    public long getSize(){
        return fmtData.validBits*numFrames*fmtData.numChannels/(8*1024);
    }

    //File duration
    public Duration getDuration(){
        return Duration.ofMillis(numFrames*1000/fmtData.sampleRate);
    }


    public static WavFileIterator newIterator(WavFile wavFile) throws IOException, WavFileException {

        WavFileIterator wavIter = initialize(wavFile);
        parseHeaders(wavIter);
        return wavIter;
    }

    public int readFrames(double[] sampleBuffer, int numFramesToRead) throws IOException, WavFileException
    {

        int offset=0;

        if (ioState != IOState.READING) throw new IOException("Cannot read from WavFile instance");

        for (int f=0 ; f<numFramesToRead ; f++)
        {
            if (iterStatus.frameCounter == numFrames) return f;
            offset = readOneFrame(sampleBuffer, offset);
        }

        return numFramesToRead;
    }


    private int readOneFrame(double[] sampleBuffer,int offset) throws IOException, WavFileException {

        for (int c=0 ; c< fmtData.numChannels ; c++)
        {
            sampleBuffer[offset] = scalingData.scaleSample(readSample());
            offset ++;
        }

        iterStatus.frameCounter ++;
        return offset;
    }


    private static WavFileIterator initialize(WavFile wavFile) throws IOException {
        WavFileIterator wavIter = new WavFileIterator();
        //wavIter.file = wavFile.file;
        // Create a new file input stream for reading file data

        wavIter.iStream = Files.newInputStream(wavFile.filepath);
        wavIter.buffer = new byte[BUFFER_SIZE];
        wavIter.iterStatus = new IteratorStatus();
        wavIter.ioState = WavFileIterator.IOState.READING;
        wavIter.filePath = wavFile.getFilepath().toString();
        wavIter.length = Files.size(wavFile.getFilepath());

        return wavIter;
    }



    /**
     * Close Input Stream
     */
    public void close() throws IOException
    {
        // Close the input stream and set to null
        if (iStream != null)
        {
            iStream.close();
            iStream = null;
        }

        // Flag that the stream is closed
        ioState = WavFileIterator.IOState.CLOSED;
    }

    private long readSample() throws IOException, WavFileException
    {
        long val = 0;

        for (int b=0 ; b<bytesPerSample ; b++)
        {
            if (iterStatus.bufferPointer == iterStatus.bytesRead)
            {
                int read = iStream.read(buffer, 0, BUFFER_SIZE);
                if (read == -1) throw new WavFileException("Not enough data available");
                iterStatus.bytesRead = read;
                iterStatus.bufferPointer = 0;
            }

            int v = buffer[iterStatus.bufferPointer];
            if (b < bytesPerSample-1 || bytesPerSample == 1) v &= 0xFF;
            val += v << (b * 8);

            iterStatus.bufferPointer ++;
        }

        return val;
    }

    static class IteratorStatus {

        IteratorStatus() {
            this.bufferPointer = 0;
            this.bytesRead = 0;
            this.frameCounter = 0;
        }

        //Points to the current position in local buffer
        int bufferPointer;				//

        // Bytes read after last read into local buffer
        int bytesRead;

        // Current number of frames read or written
        long frameCounter;

    }


}
