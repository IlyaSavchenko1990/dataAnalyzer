package com.drom.analyzer.services;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 07.09.2018
 */
public interface OptionsService {

    void buildFromArgs(String[] args) throws ParseException;

    CommandLine getSelectedOptions();

    Options getOptions();

    void printHelp();
}
