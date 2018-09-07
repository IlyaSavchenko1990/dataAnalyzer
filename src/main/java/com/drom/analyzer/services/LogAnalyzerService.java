package com.drom.analyzer.services;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
public abstract class LogAnalyzerService<T extends LogAnalyzerService.Result> {

    protected List<Listener> listeners;
    protected T result;

    public abstract void setSelectedOptions(CommandLine cmd);

    public abstract void analyze(String line);

    protected abstract void reset();

    public void addListener(Listener listener) {
        if (this.listeners == null || this.listeners.isEmpty())
            this.listeners = new ArrayList<Listener>();

        this.listeners.add(listener);
    }

    public void setListeners(List<Listener> listeners) {
        this.listeners = listeners;
    }

    protected void notifyListeners() {
        if (listeners == null || listeners.isEmpty()) return;
        listeners.forEach(listener -> listener.call(result.toPrint()));
    }

    public interface Result {
        void count();

        String toPrint();
    }

    public interface Executor {
        void execute(LogAnalyzerService analyzer) throws IOException;
    }

    public interface Listener {
        void call(String message);
    }
}
