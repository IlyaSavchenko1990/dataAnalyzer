package com.drom.analyzer.services.impl;

import com.drom.analyzer.enums.OptionsEnum;
import com.drom.analyzer.services.OptionsService;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 04.09.2018
 */
@Service
public class OptionsServiceImpl implements OptionsService {

    @Value("${app.name}")
    private String appName;
    private CommandLine cmd;
    private Options options;

    @Override
    public void buildFromArgs(String[] args) throws ParseException {
        buildOptions();
        cmd = new DefaultParser().parse(options, args);
    }

    @Override
    public CommandLine getSelectedOptions() {
        if (cmd == null)
            throw new IllegalStateException("options command line is not initialized!");

        return cmd;
    }

    @Override
    public Options getOptions() {
        if (options == null) buildOptions();
        return options;
    }

    @Override
    public void printHelp() {
        new HelpFormatter().printHelp(appName, getOptions());
    }


    protected void buildOptions() {
        options = new Options();

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.t.name())
                .hasArg()
                .argName(OptionsEnum.t.getArgName())
                .desc(OptionsEnum.t.getDescription())
                .build());

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.u.name())
                .hasArg()
                .argName(OptionsEnum.u.getArgName())
                .desc(OptionsEnum.u.getDescription())
                .build());

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.f.name())
                .hasArg()
                .argName(OptionsEnum.f.getArgName())
                .desc(OptionsEnum.f.getDescription())
                .build());

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.i.name())
                .hasArg()
                .argName(OptionsEnum.i.getArgName())
                .desc(OptionsEnum.i.getDescription())
                .build());

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.in.name())
                .desc(OptionsEnum.in.getDescription())
                .build());

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.nw.name())
                .desc(OptionsEnum.nw.getDescription())
                .build());

        options.addOption(Option.builder()
                .longOpt(OptionsEnum.h.name())
                .desc(OptionsEnum.h.getDescription())
                .build());
    }
}
