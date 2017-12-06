package com.acurable.wav;

import com.acurable.wav.config.AppConfig;
import com.acurable.wav.controller.BreathinPauseController;
import com.acurable.wav.format.Formatter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
class Main
{


    public static void main(String[] args)
    {
        try {

            log.info("Start processing...");
            AppConfig.loadConfig("app-config.properties");

            if(args==null || args.length != 2)
            {
                System.out.println("Proper Usage is: java program <input-file> <output-file>");
                System.exit(0);
            }

            String inputFilename= args[0];
            String outputFilename= args[1];

            //wavFileOps.describe(wavFile);
            BreathinPauseController.extractBreathingPauses(inputFilename,outputFilename);

            log.info("End processing...");
        } catch (Exception ex) {
            log.error(Formatter.getStackStrace(ex));
        }
    }
}