package com.drom.analyzer.util;

import com.drom.analyzer.enums.OptionsEnum;
import org.apache.commons.cli.*;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 04.09.2018
 */
public class OptionsHelper {

    private static CommandLine cmd;
    private static Options options;

    public static void build(String[] args) throws ParseException {
        cmd = new DefaultParser().parse(getOptions(), args);
    }

    public static CommandLine getCmd() {
        return cmd;
    }

    public static Options getOptions() {
        if (options != null) return options;

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

        return options;
    }
}
