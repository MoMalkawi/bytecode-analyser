package org.rsminion.tools.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Level;

@Getter
public class Logger {

    private static @Setter boolean nativeLogging;

    private static final java.util.logging.Logger nativeLogger = java.util.logging.Logger.
            getLogger("updaterLogs.txt");

    public static void log(String m, Status status) {
        if(nativeLogging) nativeLogger.log(status.level, m);
        else System.out.println("["+status.label+"] " + m);
        //GUI stuff here...
    }

    public static void error(String m) {
        log(m, Status.ERROR);
    }

    public static void warning(String m) {
        log(m, Status.WARNING);
    }

    public static void info(String m) {
        log(m, Status.NORMAL);
    }

    @AllArgsConstructor
    public enum Status {
        NORMAL("Info", Level.INFO),
        WARNING("Warning", Level.WARNING),
        ERROR("Error", Level.SEVERE);

        private String label;
        private Level level;
    }

}
