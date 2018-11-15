package top.yinxiaokang.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class MilliSecond {

    public static long betweenNowAndTomorrow(LocalTime localTime) {
        long start = System.currentTimeMillis();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime tomorrowDateTime = LocalDateTime.of(tomorrow, localTime);
        long end = tomorrowDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return end - start;
    }


    public static long betweenNowAndTomorrow915() {
        LocalTime localTime = LocalTime.of(9, 15);
        return betweenNowAndTomorrow(localTime);
    }

    public static long betweenNowAndTomorrow1001() {
        LocalTime localTime = LocalTime.of(10, 1);
        return betweenNowAndTomorrow(localTime);
    }

    public static long betweenNowAndTomorrow2301() {
        LocalTime localTime = LocalTime.of(23, 1);
        return betweenNowAndTomorrow(localTime);
    }
}
