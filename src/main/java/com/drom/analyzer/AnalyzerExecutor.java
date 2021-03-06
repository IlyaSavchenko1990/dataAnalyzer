package com.drom.analyzer;

import com.drom.analyzer.enums.OptionsEnum;
import com.drom.analyzer.services.LogAnalyzerService;
import com.drom.analyzer.services.OptionsService;
import com.drom.analyzer.services.OutputService;
import org.apache.commons.cli.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
@Component
class AnalyzerExecutor implements LogAnalyzerService.Executor {

    @Autowired
    private OutputService out;

    @Autowired
    private OptionsService options;

    @Override
    public void execute(LogAnalyzerService analyzer) throws IOException {
        CommandLine cmd = options.getSelectedOptions();

        if (cmd.hasOption(OptionsEnum.f.name()))
            readFile(cmd.getOptionValue(OptionsEnum.f.name()), analyzer);
        else
            readInput(analyzer, !cmd.hasOption(OptionsEnum.nw.name()));
    }

    private void readInput(LogAnalyzerService analyzer, boolean wait) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(System.in))) {
            if (!reader.ready()) {
                throw new IOException("input data is empty!");
            }

            read(reader, analyzer, wait);
        }
    }

    private void readFile(String filePath, LogAnalyzerService analyzer) throws IOException {
        File file = new File(filePath);
        if (!file.exists())
            throw new FileNotFoundException(String.format("File with path \"%s\" not found!", filePath));

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            read(reader, analyzer, false);
        }
    }

    private void read(BufferedReader reader, LogAnalyzerService analyzer, boolean wait) throws IOException {
        while (true) {
            String line = reader.readLine();
            analyzer.analyze(line);
            if (line == null || "".equals(line)) {
                if (wait) {
                    try {
                        long sleepTimeOut = 5000L;
                        out.print(String.format("Waiting new input data for %s ms. Press ctrl+c to stop", sleepTimeOut));
                        Thread.sleep(sleepTimeOut);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("App is not able to wait input: thread is interrupted");
                    }
                } else break;
            }
        }
    }
}
