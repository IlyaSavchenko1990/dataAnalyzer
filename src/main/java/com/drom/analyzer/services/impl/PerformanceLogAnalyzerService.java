package com.drom.analyzer.services.impl;

import com.drom.analyzer.enums.OptionsEnum;
import com.drom.analyzer.services.LogAnalyzerService;
import com.drom.analyzer.util.DateUtils;
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
public class PerformanceLogAnalyzerService extends LogAnalyzerService<PerformanceLogAnalyzerService.LogResult> {

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
    @Value("${date.format}")
    private String dateFormat = "yyyy HH:mm";

    private Date intervalDate;
    private int intervalType;
    private int intervalValue;
    private boolean autoIncrInterval;

    private float maxRequestTime;
    private float maxSuccessRate;

    public PerformanceLogAnalyzerService() {
        super();
        reset();
        intervalType = Calendar.SECOND;
        intervalValue = 1;
        autoIncrInterval = true;
    }

    @Override
    public void setSelectedOptions(CommandLine cmd) {
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
        result.setLineCount(result.getLineCount() + 1);

        LogParseResult parseResult = parse(line);

        Date date;
        try {
            date = new SimpleDateFormat(dateFormat).parse(parseResult.getFullDate());
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    String.format("incorrect date format at line %s", result.getLineCount()));
        }

        if (intervalDate == null) setIntervalDate(date);

        if (result.getBeginDate() == null) result.setBeginDate(parseResult.getDateTime());
        result.setEndDate(parseResult.getDateTime());

        checkAndNotify(date);

        updateResult(!(parseResult.getResponseCode().startsWith("5")
                || parseResult.getRequestTime() == null || parseResult.getRequestTime() > maxRequestTime));
    }

    @Override
    protected void reset() {
        intervalDate = null;
        if (result == null)
            result = new LogResult();

        result.reset();
    }

    private class LogParseResult {
        private String fullDate;
        private String dateTime;
        private String responseCode;
        private Float requestTime;

        public LogParseResult() {
        }

        public String getFullDate() {
            return fullDate;
        }

        public void setFullDate(String fullDate) {
            this.fullDate = fullDate;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public String getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(String responseCode) {
            this.responseCode = responseCode;
        }

        public Float getRequestTime() {
            return requestTime;
        }

        public void setRequestTime(Float requestTime) {
            this.requestTime = requestTime;
        }
    }

    protected static class LogResult implements LogAnalyzerService.Result {
        private String beginDate;
        private String endDate;
        private long lineCount;
        private int intervalCount;
        private int successCount;
        private int failCount;
        private float successRate;

        LogResult() {
        }

        public void reset() {
            beginDate = null;
            endDate = null;
            intervalCount = 0;
            successCount = 0;
            failCount = 0;
            successRate = 0f;
        }

        @Override
        public void count() {
            successRate = ((float) successCount / intervalCount) * 100;
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

        public long getLineCount() {
            return lineCount;
        }

        public void setLineCount(long lineCount) {
            this.lineCount = lineCount;
        }

        String getEndDate() {
            return endDate;
        }

        void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        int getIntervalCount() {
            return intervalCount;
        }

        void setIntervalCount(int intervalCount) {
            this.intervalCount = intervalCount;
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
        result.setIntervalCount(result.getIntervalCount() + 1);

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

    private LogParseResult parse(String line) {
        Pattern pattern = Pattern.compile(datePattern + dateTimePattern + ".*" + protocolPattern
                + ".*" + responseCodePattern + "\\d?\\s?" + requestTimePattern);
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find())
            throw new IllegalArgumentException(
                    String.format("incorrect log format at line %s", result.getLineCount()));

        LogParseResult parseResult = new LogParseResult();
        parseResult.setFullDate(matcher.group(1) + matcher.group(2));
        parseResult.setDateTime(matcher.group(2));
        parseResult.setResponseCode(matcher.group(4).trim());
        parseResult.setRequestTime(matcher.group(5) == null ? null : new Float(matcher.group(5)));

        return parseResult;
    }

    private String getPatternMatchValue(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(str);

        return !matcher.find() ? null : matcher.group();
    }
}
