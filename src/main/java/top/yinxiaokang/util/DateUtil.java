package top.yinxiaokang.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/10/8 10:01
 */
public class DateUtil {
    /**
     * 常用格式1
     * yyyy-MM-dd HH:mm:ss 格式
     */
    public static DateTimeFormatter DTF_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 常用格式2
     * yyyy-MM-dd
     */
    public static DateTimeFormatter DTF_YEAR_MONTH_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * Date转换为LocalDate
     *
     * @param date
     * @return
     */
    public static LocalDate date2LocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * LocalDate转换为Date
     *
     * @param localDate
     * @return
     */
    public static Date localDate2Date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date转换为LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime转化为Date
     *
     * @param localDateTime
     * @return
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date转换为LocalTime
     *
     * @param date
     * @return
     */
    public static LocalTime date2LocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    /**
     * LocalTime转换为Date(其中LocalDate为系统当前日期)
     *
     * @param localTime
     * @return
     */
    public static Date localTime2Date(LocalTime localTime) {
        return localDateTime2Date(LocalDateTime.of(LocalDate.now(), localTime));
    }
}
