package com.acurable.wav.io;

import com.acurable.wav.config.AppConfig;
import com.acurable.wav.domain.BreathingPause;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.acurable.wav.format.Formatter.formatFloatingPointNumber;
import static com.opencsv.CSVWriter.*;

public class AudioFileIO {


    public static List<String> readAudioFileNames(String inputCsv) throws IOException {


        try(
                Reader reader = Files.newBufferedReader(IOUtils.resolvePath(inputCsv));
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()
        ){

            return csvReader.readAll().stream().map(row -> row[0])
                    .filter(filename->filename.trim().length()>0).collect(Collectors.toList());
        }

    }


    public static void persistPauses(String outputCsv, List<BreathingPause> breathingPauses) throws IOException {

        try(

                Writer writer = Files.newBufferedWriter(IOUtils.resolvePath(outputCsv), StandardCharsets.UTF_8);

                CSVWriter csvWriter = new CSVWriter(writer,
                        DEFAULT_SEPARATOR,
                        NO_QUOTE_CHARACTER,
                        DEFAULT_ESCAPE_CHARACTER,
                        DEFAULT_LINE_END)
        ){

            String[] headerRecord = {"File Path", "Pause #", "start [secs]", "end [secs]", "duration [secs]","type"};
            csvWriter.writeNext(headerRecord);


            for (BreathingPause p : breathingPauses) {
                csvWriter.writeNext(
                        new String[]{

                                p.getFilePath().replaceFirst("^"+ AppConfig.basepath+"/", ""),
                                String.valueOf(p.getPauseIndex()),
                                formatFloatingPointNumber(p.getStart()),
                                formatFloatingPointNumber(p.getEnd()),
                                formatFloatingPointNumber(p.getDuration()),
                                p.getType().name()
                        }
                );
            }


        }

    }

    public static List<String[]> readOutputFile(String outputCsv) throws IOException {



        try(
                Reader reader = Files.newBufferedReader(IOUtils.resolvePath(outputCsv));
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()
        ){

            return csvReader.readAll();
        }

    }

}
