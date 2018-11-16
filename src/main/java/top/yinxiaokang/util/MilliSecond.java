package top.yinxiaokang.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class MilliSecond {

    private static long betweenNowAndNextDateTime(LocalTime localTime) {
        LocalDate today = LocalDate.now();
        LocalDateTime nextDateTime = LocalDateTime.of(today, localTime);
        LocalDateTime now = LocalDateTime.now();
        if (nextDateTime.isAfter(now)) {
            return betweenTwoDateTime(now, nextDateTime);
        }
        LocalDate tomorrow = today.plusDays(1);
        LocalDateTime tomorrowDateTime = LocalDateTime.of(tomorrow, localTime);
        return betweenTwoDateTime(now, tomorrowDateTime);
    }

    private static long betweenTwoDateTime(LocalDateTime start, LocalDateTime end) {
        long milli = end.toInstant(ZoneOffset.of("+8")).toEpochMilli() - start.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return milli >= 0 ? milli : -milli;
    }


    public static long betweenNowAndNext915() {
        LocalTime localTime = LocalTime.of(9, 15);
        return betweenNowAndNextDateTime(localTime);
    }

    public static long betweenNowAndNext1001() {
        LocalTime localTime = LocalTime.of(10, 1);
        return betweenNowAndNextDateTime(localTime);
    }

    public static long betweenNowAndNext2301() {
        LocalTime localTime = LocalTime.of(23, 1);
        return betweenNowAndNextDateTime(localTime);
    }
}
