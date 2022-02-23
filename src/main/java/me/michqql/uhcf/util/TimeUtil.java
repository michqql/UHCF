package me.michqql.uhcf.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    // days, hours, minutes, seconds
    public static int[] parseString(String string) {
        String[] units = string.split("[ ]+");

        int[] times = new int[4];

        for(String str : units) {
            if(str.length() == 0)
                continue;

            String unit = str.substring(str.length() - 1);
            String timeAsString = str.substring(0, str.length() - 1);
            int time = 0;
            try {
                time = Integer.parseInt(timeAsString);
            } catch (NumberFormatException ignore) {}

            switch (unit) {
                case "d" -> times[0] += time;
                case "h" -> times[1] += time;
                case "m" -> times[2] += time;
                case "s" -> times[3] += time;
            }
        }

        return times;
    }

    public static long toMillis(int[] times) {
        if(times.length != 4)
            return 0L;

        return TimeUnit.DAYS.toMillis(times[0]) + TimeUnit.HOURS.toMillis(times[1]) +
                TimeUnit.MINUTES.toMillis(times[2]) + TimeUnit.SECONDS.toMillis(times[3]);
    }
}
