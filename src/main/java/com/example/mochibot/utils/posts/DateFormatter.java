package com.example.mochibot.utils.posts;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

    public static String getFormattedDate() {
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        String dayOfWeek = currentDate.getDayOfWeek().toString().toLowerCase();
        dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1);

        String formattedDate = currentDate.format(formatter);

        return dayOfWeek + ", " + formattedDate;
    }
}
