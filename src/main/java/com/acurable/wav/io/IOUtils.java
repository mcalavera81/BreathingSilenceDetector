package com.acurable.wav.io;

import com.acurable.wav.config.AppConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {

    public static Path resolvePath(String filename){
        return Paths.get(AppConfig.basepath, filename);
    }
}
