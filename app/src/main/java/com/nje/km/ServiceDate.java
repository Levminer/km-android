package com.nje.km;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A nap, amelyre a menetrendet lekérdezzük. Alapból a mai nap; ha a kiválasztott
 * nap már elmúlt (pl. éjfél után), visszaáll a mai napra.
 */
public final class ServiceDate {

    private static Calendar selected;

    private ServiceDate() {
    }

    public static Calendar get() {
        Calendar today = startOfDay(Calendar.getInstance());
        if (selected == null || selected.before(today)) {
            selected = null;
            return today;
        }
        return (Calendar) selected.clone();
    }

    public static void set(int year, int month, int dayOfMonth) {
        Calendar day = Calendar.getInstance();
        day.set(year, month, dayOfMonth);
        selected = startOfDay(day);
    }

    /** Dátum az API által várt formátumban, pl. 20261017. */
    public static String apiFormat() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(get().getTime());
    }

    /** Rövid dátum megjelenítéshez, pl. 10.17. */
    public static String displayFormat() {
        return new SimpleDateFormat("MM.dd", Locale.US).format(get().getTime());
    }

    private static Calendar startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
