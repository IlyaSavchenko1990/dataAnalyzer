package com.drom.analyzer;

import com.drom.analyzer.services.AnalyzerService;
import com.drom.analyzer.services.OutputService;
import com.drom.analyzer.util.OptionsHelper;
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
    private AnalyzerService analyzer;

    @Autowired
    private AnalyzerExecutor executor;

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
            OptionsHelper.build(args);
        } catch (ParseException e) {
            out.printException(null, e);
            out.printHelp();
            return;
        }

        try {
            analyzer.addListener(out);
            analyzer.setOptions();
            executor.execute(analyzer);
        } catch (Throwable e) {
            out.printException(null, e);
        }
    }
}
