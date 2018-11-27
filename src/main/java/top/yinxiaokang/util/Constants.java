package top.yinxiaokang.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 常量类
 */
@SuppressWarnings({"SpellCheckingInspection", "WeakerAccess", "unused"})
@Slf4j
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
     * 路径: /修账
     */
    public static String TAKE_ACCOUNT = "/修账";

    /**
     * 路径: /转换版
     */
    public static String TAKE_ACCOUNT_TRANSFORM = "/转换版";

    /**
     * 路径: /需要填入数据
     */
    public static String TAKE_ACCOUNT_SHOULD_FILLING_IN_DATA = "/需要填入数据";

    /**
     * 路径: /需要填入数据
     */
    public static String TAKE_ACCOUNT_FILLED_DATA = "/已填入数据";
    /**
     * 路径: /已经处理过的贷款账号
     */
    public static String TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA = "/已经处理过的贷款账号";
    /**
     * 路径: /标记天蓝色
     */
    public static String TAKE_ACCOUNT_TAKED_FLAG_SKY_BLUE_ACCOUNTS_DATA = "/标记天蓝色";

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
     * 基础账号和实时贷款余额文件路径
     */
    public static String BASE_ACCOUNT_INFORMATION_SSDKYE = BASE_PATH + "/1427问题账号20181009-实时贷款余额" + XLSX;

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
            YESTERDAY_ACCOUNT_PATH + "/" + YESTERDAY_STR + "-应该正常扣款账号-oneday" + XLS;

    public static String YESTERDAY_SHOULD_PAYMENT_TO_FLAG_ACCOUNT =
            YESTERDAY_ACCOUNT_PATH + "/" + YESTERDAY_STR + "-应该正常扣款账号-业务分析需要版-oneday" + XLS;
    /**
     * 昨日 应该扣款之后未入账账号文件路径
     */
    public static String YESTERDAY_SHOULD_PAYMENT_ACCOUNT_FAIL =
            YESTERDAY_ACCOUNT_PATH + "/" + YESTERDAY_STR + "-扣款之后未入账账号" + XLS;
    /**
     * 今日 账号 路径
     */
    public static String TODAY_ACCOUNT_PATH =
            ACCOUNT_PATH + "/" + TODAY_YEARMONTH_STR;

    /**
     * 今日 应该扣款账号观看版文件路径
     */
    public static String TODAY_SHOULD_PAYMENT_ACCOUNT_VIEW_XLS =
            TODAY_ACCOUNT_PATH + "/" + TODAY_STR + "-应该正常扣款账号" + XLS;

    /**
     * 今日 应该扣款账号文件路径
     */
    public static String TODAY_SHOULD_PAYMENT_ACCOUNT_XLS =
            TODAY_ACCOUNT_PATH + "/" + TODAY_STR + "-应该正常扣款账号-oneday" + XLS;
    /**
     * 今日 给明天使用业务分析账号文件路径
     */
    public static String TODAY_SHOULD_PAYMENT_ACCOUNT_TO_FLAG_XLS =
            TODAY_ACCOUNT_PATH + "/" + TODAY_STR + "-应该正常扣款账号-业务分析需要版-oneday" + XLS;

    /**
     * 今日 应该扣款账号文件路径
     */
    public static String TODAY_SHOULD_PAYMENT_ACCOUNT_LOG =
            TODAY_ACCOUNT_PATH + "/" + TODAY_STR + "-应该正常扣款账号-oneday" + LOG;
    /**
     * 今日 应该扣款账号文件路径
     */
    public static String TODAY_SHOULD_PAYMENT_ACCOUNT_MESSAGES_LOG =
            TODAY_ACCOUNT_PATH + "/" + TODAY_STR + "-内部查询出来的各种信息数量" + LOG;


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
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS = YESTERDAY_BUSINESS_PATH + "/" + YESTERDAY_STR + "-业务推算和实际业务";
    /**
     * 昨日业务文件名及路径, 不包含扩展名
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BY_DKZH_BUSINESS = YESTERDAY_BUSINESS_PATH + "/单独账号" + "/" + TODAY_STR + "-业务推算和实际业务";
    /**
     * 所有业务文件名及路径
     */
    public static String All_DKZH_BUSINESS = YESTERDAY_BUSINESS_PATH + "/所有账号" + "/" + TODAY_STR + "-业务推算和实际业务";
    /**
     * 昨日业务文件名及路径, 含特定扩展名
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS_LOG = YESTERDAY_SHOULD_PAYMENT_BUSINESS + LOG;
    /**
     * 昨日业务文件名及路径, 含特定扩展名
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS_XLS = YESTERDAY_SHOULD_PAYMENT_BUSINESS + XLS;
    /**
     * 昨日业务文件名及路径, 含特定扩展名 _ 特殊情况
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS_BY_DKZH_LOG = YESTERDAY_SHOULD_PAYMENT_BY_DKZH_BUSINESS + LOG;
    /**
     * 昨日业务文件名及路径, 含特定扩展名 _ 特殊情况
     */
    public static String YESTERDAY_SHOULD_PAYMENT_BUSINESS_BY_DKZH_XLS = YESTERDAY_SHOULD_PAYMENT_BY_DKZH_BUSINESS + XLS;
    /**
     * 所有业务文件名及路径, 含特定扩展名 _ 特殊情况
     */
    public static String All_DKZH_BUSINESS_LOG = All_DKZH_BUSINESS + LOG;
    /**
     * 所有业务文件名及路径, 含特定扩展名 _ 特殊情况
     */
    public static String All_DKZH_BUSINESS_XLS = All_DKZH_BUSINESS + XLS;


    /**
     * 修账 路径
     */
    public static String TAKE_ACCOUNT_PATH = BASE_PATH + TAKE_ACCOUNT;

    /**
     * 修账 转换版路径
     */
    public static String TAKE_ACCOUNT_TRANSFORM_PATH = TAKE_ACCOUNT_PATH + TAKE_ACCOUNT_TRANSFORM;

    /**
     * 修账 需要填入数据路径
     */
    public static String TAKE_ACCOUNT_SHOULD_FILLING_IN_DATA_PATH = TAKE_ACCOUNT_PATH + TAKE_ACCOUNT_SHOULD_FILLING_IN_DATA;

    /**
     * 修账 已填入数据路径
     */
    public static String TAKE_ACCOUNT_FILLED_DATA_PATH = TAKE_ACCOUNT_PATH + TAKE_ACCOUNT_FILLED_DATA;
    /**
     * 修账 已经处理过的贷款账号
     */
    public static String TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH = TAKE_ACCOUNT_PATH + TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA + "/已处理贷款账号" + XLS;

    /**
     * 修账 标记天蓝色路径
     */
    public static String TAKE_ACCOUNT_TAKED_FLAG_SKY_BLUE_ACCOUNTS_DATA_PATH = TAKE_ACCOUNT_PATH + TAKE_ACCOUNT_TAKED_FLAG_SKY_BLUE_ACCOUNTS_DATA;


    public static void main(String[] args) {

        Constants constants = new Constants();
        log.info("");
    }

}
