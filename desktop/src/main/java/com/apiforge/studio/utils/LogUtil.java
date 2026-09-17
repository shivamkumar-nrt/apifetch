package com.apiforge.studio.utils;

import java.io.FileWriter;
import java.io.PrintWriter;

public class LogUtil {
    public static void log(String msg, Exception e) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(System.getProperty("user.home") + "/.apiforge/error.log", true))) {
            pw.println(msg);
            if (e != null) {
                e.printStackTrace(pw);
            }
        } catch (Exception ex) {}
    }
}
