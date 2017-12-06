package com.acurable.wav.domain;

import com.acurable.wav.io.IOUtils;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class WavFile
{

	// File that will be read from
	Path filepath;

	private WavFile(){}

	public static WavFile newWavFile(String filename)
	{
		// Instantiate new Wavfile and store the file reference

		WavFile wavFile = new WavFile();
		wavFile.filepath = IOUtils.resolvePath(filename);
		return wavFile;
	}






}
