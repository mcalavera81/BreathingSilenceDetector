package com.acurable.wav.format;


import java.io.PrintWriter;
import java.io.StringWriter;

public class Formatter {


    public static String formatFloatingPointNumber(double number) {
        return String.format("%.2f", number);
    }

    public static String getStackStrace(Exception ex){
        StringWriter w= new StringWriter();
        ex.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

}
