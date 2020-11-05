package com.aws.logger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogFilter {

   Logger log = LogManager.getLogger(LogFilter.class);


    public void logLevel() {

        log.info("********* LOG INFO MESSAGE ***********");

        log.debug("********* LOG DEBUG MESSAGE ***********");



        log.error("********* LOG ERROR MESSAGE ***********");
    }
}
