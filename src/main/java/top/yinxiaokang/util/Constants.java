package top.yinxiaokang.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 常量类
 */
@SuppressWarnings("SpellCheckingInspection")
public class Constants {

    /**
     * 路径: C:/修账相关数据
     */
    public static String BASE_PATH = "C:/修账相关数据";

    /**
     * 路径: /每日业务分析
     */
    public static String EVERYDAY_BUSINESS = "/每日业务分析";

    /**
     * 路径: /每日账号分析
     */
    public static String EVERYDAY_ACCOUNT = "/每日账号分析";

    /**
     * 路径: /201810月
     */
    public static String OCTOBER = "/201810";

    /**
     * 路径: /201811月
     */
    public static String NOVEMBER = "/201811";

    /**
     * 文件扩展名 .xlsx
     */
    public static String XLSX = ".xlsx";

    /**
     * 文件扩展名 .xls
     */
    public static String XLS = ".xls";
    /**
     * 文件扩展名 .log
     */
    public static String LOG = ".log";


    /**
     * 昨天字符串日期  格式为: yyyy-MM-dd
     */
    private static String YESTERDAY_STR;

    private static String YESTERDAY_YEARMONTH_STR;


    static {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.plusDays(-1);
        YESTERDAY_YEARMONTH_STR = DateTimeFormatter.ofPattern("yyyyMM").format(localDate);
        YESTERDAY_STR = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
    }

    /**
     * 今天字符串日期  格式为: yyyy-MM-dd
     */
    private static String TODAY_STR;

    private static String TODAY_YEARMONTH_STR;


    static {
        LocalDate localDate = LocalDate.now();
        TODAY_YEARMONTH_STR = DateTimeFormatter.ofPattern("yyyyMM").format(localDate);
        TODAY_STR = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
    }

    /**
     * 基础账号信息文件路径
     */
    public static String BASE_ACCOUNT_INFORMATION = BASE_PATH + "/1427问题账号20181009-账号信息" + XLSX;

    /**
     * 每日账号  路径
     */
    public static String ACCOUNT_PATH = BASE_PATH + EVERYDAY_ACCOUNT;


    /**
     * 昨日 账号 路径
     */
    public static String YESTERDAY_ACCOUNT_PATH =
            ACCOUNT_PATH + "/" + YESTERDAY_YEARMONTH_STR;

    /**
     * 昨日 应该扣款账号文件路径
     */
    public static String YESTERDAY_SHOULD_PAYMENT_ACCOUNT =
            YESTERDAY_ACCOUNT_PATH + "/" + YESTERDAY_STR + "-应该正常扣款账号-oneday" + XLSX;
    /**
     * 今日 账号 路径
     */
    public static String TODAY_ACCOUNT_PATH =
            ACCOUNT_PATH + "/" + TODAY_YEARMONTH_STR;

    /**
     * 今日 应该扣款账号文件路径
     */
    public static String TODAY_SHOULD_PAYMENT_ACCOUNT =
            TODAY_ACCOUNT_PATH + "/" + TODAY_STR + "-应该正常扣款账号-oneday" + XLSX;


    /**
     * 每日业务 路径
     */
    public static String BUSINESS_PATH = BASE_PATH + EVERYDAY_BUSINESS;

    /**
     * 昨日业务 路径
     */
    public static String YESTERDAY_BUSINESS_PATH = BUSINESS_PATH + "/" + YESTERDAY_YEARMONTH_STR;

    /**
     * 昨日业务文件名及路径, 不包含扩展名
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS = YESTERDAY_BUSINESS_PATH + "/" + YESTERDAY_STR + "-业务推算和实际业务-标红代表不正常";
    /**
     * 昨日业务文件名及路径, 含特定扩展名
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS_LOG = YESTERDAY_SHOULD_PAYMENT_BUSINESS + LOG;
    /**
     * 昨日业务文件名及路径, 含特定扩展名
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS_XLS = YESTERDAY_SHOULD_PAYMENT_BUSINESS + XLS;


    public static void main(String[] args) {

        Constants constants = new Constants();
        System.out.println(constants);
    }

}
