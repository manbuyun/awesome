package com.manbuyun.awesome;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.DateTimePrinter;

/**
 * @author jinhai
 * @date 2019/05/10
 */
public class Main {

    public static void main(String[] args) {
        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd HH").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser()};
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy-MM-dd").getPrinter();
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .append(printer, parsers)
                .toFormatter();

        DateTime dateTime = dateTimeFormatter.parseDateTime("2019-05-05 19:00");

        System.out.println(dateTime.minusDays(6).toString("yyyy-MM-dd HH:mm:ss"));
    }
}