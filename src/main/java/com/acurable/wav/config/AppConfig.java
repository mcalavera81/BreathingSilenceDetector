package com.acurable.wav.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;

@Slf4j
public class AppConfig {


    public static String basepath;
    public static double sampling;
    public static double signalThreshold;
    public static double timeThreshold;

    public static void loadConfig(String filename) {

        Properties props = new Properties();
        InputStream input = null;

        try {

            input = AppConfig.class.getClassLoader().getResourceAsStream(filename);
            if(input == null){
                log.info("Sorry, unable to find " + filename);
                return;
            }

            //load a properties file from class path, inside static method
            props.load(input);

            for(Enumeration<?> enumeration = props.propertyNames(); enumeration.hasMoreElements(); ) {
                String key = (String) enumeration.nextElement();
                try {

                    Field f = AppConfig.class.getDeclaredField(key);
                    if(f.getType()==double.class){
                        f.setDouble(null,Double.parseDouble(props.getProperty(key)));
                    }else{
                        f.set(null,props.getProperty(key));
                    }

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    log.warn(e.getMessage());
                }

            }


        } catch (IOException ex) {
            log.warn(ex.getMessage());
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }
            }
        }

    }


}


