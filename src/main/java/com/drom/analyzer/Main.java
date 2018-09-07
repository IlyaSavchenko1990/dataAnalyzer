package com.drom.analyzer;

import com.drom.analyzer.enums.OptionsEnum;
import com.drom.analyzer.services.LogAnalyzerService;
import com.drom.analyzer.services.OptionsService;
import com.drom.analyzer.services.OutputService;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
@Component
public class Main {

    @Autowired
    private LogAnalyzerService analyzer;

    @Autowired
    private AnalyzerExecutor executor;

    @Autowired
    private OptionsService options;

    @Autowired
    private OutputService out;

    public static void main(String[] args) {
        LogManager.getLogManager().getLogger("").setLevel(Level.OFF);
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(ApplicationConfig.class);

        ctx.getBean(Main.class).start(args);
    }

    private void start(String[] args) {
        try {
            options.buildFromArgs(args);
        } catch (ParseException e) {
            out.printException(null, e);
            options.printHelp();
            return;
        }

        if (options.getSelectedOptions().hasOption(OptionsEnum.h.name())) {
            options.printHelp();
            return;
        }

        try {
            analyzer.setSelectedOptions(options.getSelectedOptions());
            analyzer.addListener(out);
            executor.execute(analyzer);
        } catch (Throwable e) {
            out.printException(null, e);
        }
    }
}
