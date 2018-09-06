package com.drom.analyzer.services;

import com.drom.analyzer.util.OptionsHelper;
import org.apache.commons.cli.HelpFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Prints data and errors to stdout
 *
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
@Service
public class OutputService implements AnalyzerService.Listener {

    @Value("${app.name}")
    private String appName;

    @Override
    public void call(String message) {
        print(message);
    }

    public void print(String outputMessage) {
        System.out.println(outputMessage);
    }

    public void printException(String comment, Throwable throwable) {
        print(String.format((comment == null ? "" : comment + " ") + "%s",
                throwable.getMessage() == null ? throwable : throwable.getMessage()));
    }

    public void printHelp() {
        new HelpFormatter().printHelp(appName, OptionsHelper.getOptions());
    }
}
