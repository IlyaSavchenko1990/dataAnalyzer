package com.drom.analyzer.services.impl;

import com.drom.analyzer.enums.OptionsEnum;
import com.drom.analyzer.services.AnalyzerService;
import com.drom.analyzer.util.DateUtils;
import com.drom.analyzer.util.OptionsHelper;
import org.apache.commons.cli.CommandLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ilya Savchenko
 * @email ilyasavchenko1990@gmail.com
 * @date 03.09.2018
 */
@Service
public class LogAnalyzerService extends AnalyzerService<LogAnalyzerService.LogResult> {

    @Value("${date.pattern}")
    private String datePattern;
    @Value("${date.time.pattern}")
    private String dateTimePattern;
    @Value("${protocol.pattern}")
    private String protocolPattern;
    @Value("${response.code.pattern}")
    private String responseCodePattern;
    @Value("${request.time.pattern}")
    private String requestTimePattern;
    @Value("${code.time.transitional.pattern}")
    private String codeTimeTransitionalPattern;
    @Value("${date.format}")
    private String dateFormat = "yyyy HH:mm";

    private Date intervalDate;
    private int intervalType;
    private int intervalValue;
    private boolean autoIncrInterval;

    private float maxRequestTime;
    private float maxSuccessRate;

    public LogAnalyzerService() {
        super();
        reset();
        intervalType = Calendar.SECOND;
        intervalValue = 1;
        autoIncrInterval = true;
    }

    @Override
    public void setOptions() {
        CommandLine cmd = OptionsHelper.getCmd();
        if (cmd == null)
            throw new RuntimeException("Command line options are not found");

        if (!cmd.hasOption(OptionsEnum.t.name()) || !cmd.hasOption(OptionsEnum.u.name()))
            throw new NoSuchFieldError(String.format("-%s and -%s options are required. Use -%s option to print help",
                    OptionsEnum.t.name(), OptionsEnum.u.name(), OptionsEnum.h.name()));

        try {
            maxRequestTime = new Float(cmd.getOptionValue(OptionsEnum.t.name()));
            maxSuccessRate = new Float(cmd.getOptionValue(OptionsEnum.u.name()));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(String.format(
                    "wrong format for options \"-%s\" and \"-%s\". Use \"-%s\" option to print help",
                    OptionsEnum.t.name(), OptionsEnum.u.name(), OptionsEnum.h.name()));
        }

        if (!cmd.hasOption(OptionsEnum.i.name())) return;

        String intervalOptionValue = cmd.getOptionValue(OptionsEnum.i.name());
        String interval = getPatternMatchValue(intervalOptionValue, "\\d+");
        String intervalType = getPatternMatchValue(intervalOptionValue, "[hms]");

        try {
            if (interval == null || intervalType == null)
                throw new IllegalArgumentException();

            intervalValue = new Integer(interval);

            if ("h".equals(intervalType))
                this.intervalType = Calendar.HOUR;
            else if ("m".equals(intervalType))
                this.intervalType = Calendar.MINUTE;
            else if ("s".equals(intervalType))
                this.intervalType = Calendar.SECOND;
            else throw new IllegalArgumentException();

        } catch (Throwable e) {
            throw new IllegalArgumentException(String.format(
                    "wrong format for option \"-%s\". Use \"-%s\" option to print help",
                    OptionsEnum.i.name(), OptionsEnum.h.name()));
        }
    }

    @Override
    public void analyze(String line) {
        if (line == null) return;

        String responseCode = getResponseCode(line);
        String dateStr = getFullDate(line);

        Date date;
        try {
            date = new SimpleDateFormat(dateFormat).parse(dateStr);
        } catch (ParseException e) {
            return;
        }

        if (responseCode == null || date == null) return;
        if (intervalDate == null) setIntervalDate(date);

        String dateTime = getDateTime(dateStr);
        if (result.getBeginDate() == null) result.setBeginDate(dateTime);
        result.setEndDate(dateTime);

        checkAndNotify(date);

        Float requestTime = getRequestTime(line);
        updateResult(!(responseCode.startsWith("5") || requestTime == null || requestTime > maxRequestTime));
    }

    @Override
    protected void reset() {
        intervalDate = null;
        result = new LogResult();
    }

    protected static class LogResult implements AnalyzerService.Result {
        private String beginDate;
        private String endDate;
        private int count;
        private int successCount;
        private int failCount;
        private float successRate;

        LogResult() {
        }

        @Override
        public void count() {
            successRate = ((float) successCount / count) * 100;
        }

        @Override
        public String toPrint() {
            return getBeginDate() + " " + getEndDate() + " "
                    + String.format(java.util.Locale.US, "%.2f", getSuccessRate());
        }

        String getBeginDate() {
            return beginDate;
        }

        void setBeginDate(String beginDate) {
            this.beginDate = beginDate;
        }

        String getEndDate() {
            return endDate;
        }

        void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        int getCount() {
            return count;
        }

        void setCount(int count) {
            this.count = count;
        }

        int getSuccessCount() {
            return successCount;
        }

        void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        int getFailCount() {
            return failCount;
        }

        void setFailCount(int failCount) {
            this.failCount = failCount;
        }

        float getSuccessRate() {
            return successRate;
        }

        void setSuccessRate(float successRate) {
            this.successRate = successRate;
        }
    }

    private void updateResult(boolean success) {
        result.setCount(result.getCount() + 1);

        if (!success) result.setFailCount(result.getFailCount() + 1);
        else result.setSuccessCount(result.getSuccessCount() + 1);
    }

    private void checkAndNotify(Date date) {
        if (date.getTime() >= intervalDate.getTime()) {
            result.count();
            if (result.getSuccessRate() <= maxSuccessRate) {
                notifyListeners();
                reset();
            } else if (autoIncrInterval) {
                incrIntervalDate();
            } else reset();
        }
    }

    private void setIntervalDate(Date beginDate) {
        intervalDate = beginDate;
        incrIntervalDate();
    }

    private void incrIntervalDate() {
        if (intervalDate == null) return;

        if (intervalType == Calendar.HOUR)
            intervalDate = DateUtils.addHours(intervalDate, intervalValue);
        else if (intervalType == Calendar.MINUTE)
            intervalDate = DateUtils.addMinutes(intervalDate, intervalValue);
        else if (intervalType == Calendar.SECOND)
            intervalDate = DateUtils.addSeconds(intervalDate, intervalValue);
    }

    private String getResponseCode(String logLine) {
        String protocolAndCode = getPatternMatchValue(logLine, protocolPattern + responseCodePattern);
        if (protocolAndCode == null || "".equals(protocolAndCode)) return null;

        return getPatternMatchValue(protocolAndCode, responseCodePattern);
    }

    private String getFullDate(String logLine) {
        return getPatternMatchValue(logLine, datePattern + dateTimePattern);
    }

    private String getDateTime(String logLine) {
        return getPatternMatchValue(logLine, dateTimePattern);
    }

    private Float getRequestTime(String logLine) {
        String codeAndTime = getPatternMatchValue(logLine, codeTimeTransitionalPattern);
        if (codeAndTime == null || "".equals(codeAndTime)) return null;

        String requestTimeStr = getPatternMatchValue(codeAndTime, requestTimePattern);
        if (requestTimeStr == null || "".equals(requestTimeStr)) return null;

        try {
            return new Float(requestTimeStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getPatternMatchValue(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(str);

        return !matcher.find() ? null : matcher.group();
    }
}
