package com.acurable.wav.controller;


import com.acurable.wav.config.AppConfig;
import com.acurable.wav.exception.WavFileException;
import com.acurable.wav.format.Formatter;
import com.acurable.wav.io.AudioFileIO;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public class BreathinPauseControllerTest {


    private List<Path> paths;

    @Before
    public void init(){
        AppConfig.loadConfig("test-config.properties");
        paths = new ArrayList<>();
    }


    @Test
    public void testExtractBreathingPauses() throws IOException, WavFileException {
        String inputPath = createInputFile();
        String outputPath = createOutputFile();


        BreathinPauseController.extractBreathingPauses(inputPath, outputPath);

        List<String[]> rows = AudioFileIO.readOutputFile(outputPath);

        assertThat(rows).as("check number of rows").isNotEmpty().hasSize(4);
        assertThat(rows.get(2)[0]).as("check audio file name").isEqualTo("audio-test.wav");
        assertThat(rows.get(2)[4]).as("check audio duration").isEqualTo("4.99");
        assertThat(rows.get(2)[5]).as("check pause type").isEqualTo("APNOEA");
    }


    private String createOutputFile() throws IOException {
        Path outputFile = Files.createTempFile(Paths.get(AppConfig.basepath), "output", ".csv");

        paths.add(outputFile);
        return outputFile.toString().replaceFirst("^"+AppConfig.basepath+"/","");
    }

    private String createInputFile() throws IOException {

        InputStream audioStream = BreathinPauseController.class.getClassLoader().getResourceAsStream("audio-test.wav");
        Path audioFile = Paths.get(AppConfig.basepath,"audio-test.wav");
        Files.copy(audioStream, audioFile, REPLACE_EXISTING);

        // REPLACE_EXISTING
        Path inputCVSFile = Files.createTempFile(Paths.get(AppConfig.basepath), "input", ".csv");
        List<String> strings = new ArrayList<>();
        strings.add("File Path");
        strings.add(audioFile.toString().replaceFirst("^"+AppConfig.basepath+"/",""));
        Files.write(inputCVSFile, strings);

        paths.add(audioFile);
        paths.add(inputCVSFile);

        return inputCVSFile.toString().replaceFirst("^"+AppConfig.basepath+"/","");
    }


    @After
    public void destroy(){
        paths.forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException ex) {
                log.error(Formatter.getStackStrace(ex));
            }
        });
    }
}
