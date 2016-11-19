package net.lepko.mods.classicmelee.logger;

import org.apache.logging.log4j.Level;

public class Logger {

    private static org.apache.logging.log4j.Logger logger;

    public static void load(org.apache.logging.log4j.Logger log) {
        logger = log;
    }

    public static void log(String msg, Object... params) {
        logger.log(Level.ALL, msg, params);
    }
}
