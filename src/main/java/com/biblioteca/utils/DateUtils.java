package com.biblioteca.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils{
    public static final DateTimeFormatter BRAZILIAN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String format(LocalDate date) {
        return date == null ? "" : date.format(BRAZILIAN_DATE);
    }
}