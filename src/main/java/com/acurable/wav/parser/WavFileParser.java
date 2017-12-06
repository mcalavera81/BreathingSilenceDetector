package com.acurable.wav.parser;

import com.acurable.wav.domain.ScalingData;
import com.acurable.wav.domain.SubChunkFmtData;
import com.acurable.wav.exception.WavFileException;
import com.acurable.wav.domain.WavFileIterator;

import java.io.IOException;

import static com.acurable.wav.domain.WavConstants.*;

public class WavFileParser {

    static class SubChunkHeader {

        SubChunkHeader(int id, long size) {
            this.id = id;
            this.size = size;
        }

        final int id;
        final long size;
    }


    public static void parseHeaders(WavFileIterator wavIter) throws IOException, WavFileException {
        validate_RIFF_Header(wavIter);


        boolean foundFormat = false;
        boolean foundDataAndFormatChunks = false;

        // Search for the Format and Data Chunks
        while (!foundDataAndFormatChunks)
        {

            SubChunkHeader subChunkHeader = parse_subchunk_header(wavIter);

            // Extract the chunk ID and Size
            int chunkID = subChunkHeader.id;
            long chunkSize = subChunkHeader.size;

            long numChunkBytes = getChunkBytesWordAligned(chunkSize);

            switch (chunkID){
                case FMT_CHUNK_ID:
                    handleFmtChunk(wavIter, numChunkBytes);
                    foundFormat = true;
                    break;
                case DATA_CHUNK_ID:
                    handleDataChunk(wavIter, foundFormat, chunkSize);
                    foundDataAndFormatChunks = true;
                    break;
                default:
                    skipUnknownChunk(wavIter, numChunkBytes);
            }
        }


        wavIter.setScalingData(ScalingData.calculateScaling(wavIter.getFmtData().getValidBits()));

    }

    private static void validate_RIFF_Header(WavFileIterator wavIter) throws IOException, WavFileException {
        // Read the first 12 bytes of the file
        int bytesRead = wavIter.getIStream().read(wavIter.getBuffer(), 0, 12);
        if (bytesRead != 12) throw new WavFileException("Not enough wav file bytes for header");

        // Extract parts from the header
        long riffChunkID = get_little_endian(wavIter.getBuffer(), 0, 4);
        long chunkSize = get_little_endian(wavIter.getBuffer(), 4, 4);
        long riffTypeID = get_little_endian(wavIter.getBuffer(), 8, 4);

        // Check the header bytes contains the correct signature
        if (riffChunkID != RIFF_CHUNK_ID) throw new WavFileException("Invalid Wav Header data, incorrect riff chunk ID");
        if (riffTypeID != RIFF_TYPE_ID) throw new WavFileException("Invalid Wav Header data, incorrect riff type ID");

        // Check that the file size matches the number of bytes listed in header
        if (wavIter.getLength() != chunkSize+8) {
            throw new WavFileException("Header chunk size (" + chunkSize + ") does not match file size (" + wavIter.getLength() + ")");
        }
    }

    private static SubChunkHeader parse_subchunk_header(WavFileIterator wavIter) throws IOException, WavFileException {
        // Read the first 8 bytes of the chunk (ID and chunk size)
        int bytesRead= wavIter.getIStream().read(wavIter.getBuffer(), 0, 8);
        if (bytesRead == -1) throw new WavFileException("Reached end of file without finding format/data chunk");
        if (bytesRead != 8) throw new WavFileException("Could not read chunk header");

        // Extract the chunk ID and Size
        int chunkID = (int) get_little_endian(wavIter.getBuffer(), 0, 4);
        long chunkSize = get_little_endian(wavIter.getBuffer(), 4, 4);

        return new SubChunkHeader(chunkID, chunkSize);

    }

    private static SubChunkFmtData parse_fmt_data(WavFileIterator wavIter) throws IOException, WavFileException {
        // Read in the header info
        wavIter.getIStream().read(wavIter.getBuffer(), 0, 16);
        // Check this is uncompressed data
        //PCM = 1 (i.e. Linear quantization). Values other than 1 indicate some form of compression.
        int compressionCode = (int) get_little_endian(wavIter.getBuffer(), 0, 2);
        if (compressionCode != 1) throw new WavFileException("Compression Code " + compressionCode + " not supported");

        // Extract the format information
        int numChannels = (int) get_little_endian(wavIter.getBuffer(), 2, 2);
        int sampleRate = (int) get_little_endian(wavIter.getBuffer(), 4, 4);
        int blockAlign = (int) get_little_endian(wavIter.getBuffer(), 12, 2);
        int validBits = (int) get_little_endian(wavIter.getBuffer(), 14, 2);


        return SubChunkFmtData.build(numChannels, sampleRate, blockAlign, validBits);
    }

    // Get little endian data from local buffer
    // ------------------------------------------------
    private static long get_little_endian(byte[] buffer, int pos, int numBytes)
    {
        numBytes--;
        pos += numBytes;

        long val = buffer[pos] & 0xFF;
        for (int b=0 ; b<numBytes ; b++) val = (val << 8) + (buffer[--pos] & 0xFF);

        return val;
    }



    /*
    *Word align the chunk size
    * chunkSize specifies the number of bytes holding data. However,
    * the data should be word aligned (2 bytes) so we need to calculate
    * the actual number of bytes in the chunk
    */
    private static long getChunkBytesWordAligned(long chunkSize) {
        return (chunkSize%2 == 1) ? chunkSize+1 : chunkSize;
    }

    private static void handleFmtChunk(WavFileIterator wavIter, long numChunkBytes) throws IOException, WavFileException {
        wavIter.setFmtData(parse_fmt_data(wavIter));
        wavIter.setBytesPerSample(wavIter.getFmtData().getBytesPerSample());

        // Account for number of format bytes and then skip over
        // any extra format bytes
        numChunkBytes -= 16;
        if (numChunkBytes > 0) wavIter.getIStream().skip(numChunkBytes);
    }


    private static void handleDataChunk(WavFileIterator wavIter, boolean foundFormat, long chunkSize) throws WavFileException {
        validateDataChunk(wavIter.getFmtData(), chunkSize, foundFormat);
        wavIter.setNumFrames(wavIter.getFmtData().getNumberOfFrames(chunkSize));
    }

    private static void skipUnknownChunk(WavFileIterator wavIter, long numChunkBytes) throws IOException, WavFileException {

        // If an unknown chunk ID is found, just skip over the chunk data
        if(wavIter.getIStream().skip(numChunkBytes) < 0){
            throw new WavFileException("Did not find a data chunk");
        }

    }


    private static void validateDataChunk(SubChunkFmtData fmtData, long chunkSize, boolean foundFormat) throws WavFileException {
        // Check if we've found the format chunk,
        // If not, throw an exception as we need the format information
        // before we can read the data chunk
        if (!foundFormat) throw new WavFileException("Data chunk found before Format chunk");

        // Check that the chunkSize (wav data length) is a multiple of the
        // block align (bytes per frame)
        if (chunkSize % fmtData.getBlockAlign()!= 0) throw new WavFileException("Data Chunk size is not multiple of Block Align");
    }

}
