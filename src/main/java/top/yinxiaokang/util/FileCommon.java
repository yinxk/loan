package top.yinxiaokang.util;

import com.sargeraswang.util.ExcelUtil.ExcelCell;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import top.yinxiaokang.original.excelbean.AllAccountDkye;
import top.yinxiaokang.original.excelbean.OneThousand;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/8/27 9:52
 */
public class FileCommon {
    /**
     * 文本日志
     */
    public static StringBuffer logs = new StringBuffer();

    /**
     * 输入文件名称
     */
    public static String inFileName = "src/test/resources/1000多个问题贷款账号.xlsx";
    //public static String inFileName = "src/test/resources/初始有逾期.xlsx";
    //public static String inFileName = "src/test/resources/20180821-误差5块以内的.xlsx";
    //public static String inFileName = "src/test/resources/包含所有的账号的初始余额和导入的逾期本金.xlsx";
//    public static String inFileName = "src/test/resources/从30多期跳到170多期.xlsx";

    /**
     * 输出文件名称
     */
//    private static String outFileName = "从30多期跳到170多期跳过两期";
//    private static String outFileName = "allDkzhDkyePart1";
    private static String outFileName = "1400多个贷款账号分析";

    // region excel
    public static String outXLSXName = outFileName + ".xlsx";
    public static OutputStream outXLSXStream = null;
    public static List<OneThousand> datasetOneThousand = new ArrayList<>();
    public static List<AllAccountDkye> datasetAllAccountDkye = new ArrayList<>();
    public static List<OneThousand> dataset = datasetOneThousand;
    public static Map<String, String> KEY_MAP = null;
    // endregion

    static {
        try {
            outXLSXStream = new FileOutputStream(new File(outXLSXName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        KEY_MAP = new LinkedHashMap<>();
        KEY_MAP.put("dkzh", "贷款账号");
        KEY_MAP.put("csdkye", "初始贷款余额");
        KEY_MAP.put("csyqbj", "初始逾期本金");
        KEY_MAP.put("dkffrq", "贷款发放日期");
        KEY_MAP.put("dkqs", "贷款期数");
        KEY_MAP.put("csqs", "推算初始期数");
        KEY_MAP.put("csqszqx", "推算初始期数是否正确");

        KEY_MAP.put("hklx", "推算还款类型");
        KEY_MAP.put("rq", "推算日期");
        KEY_MAP.put("qc", "推算期次");
        KEY_MAP.put("fse", "推算发生额");
        KEY_MAP.put("bj", "推算本金");
        KEY_MAP.put("lx", "推算利息");
        KEY_MAP.put("qmdkye", "推算期末贷款余额");

        KEY_MAP.put("sjhklx", "实际还款类型");
        KEY_MAP.put("sjrq", "实际日期");
        KEY_MAP.put("sjqc", "实际期次");
        KEY_MAP.put("sjfse", "实际发生额(去掉罚息)");
        KEY_MAP.put("sjbj", "实际本金");
        KEY_MAP.put("sjlx", "实际利息");
        KEY_MAP.put("sjqmdkye", "实际期末贷款余额");

        KEY_MAP.put("subfse", "发生额差额(推算-实际)");
        KEY_MAP.put("subbj", "本金差额(推算-实际)");
        KEY_MAP.put("sublx", "利息差额(推算-实际)");
        KEY_MAP.put("subqmye", "期末贷款余额差额(推算-实际)");

        KEY_MAP.put("bz", "备注");

        KEY_MAP.put("subFseTotal", "发生额差额合计");
        KEY_MAP.put("subBjTotal", "本金差额合计");
        KEY_MAP.put("subLxTotal", "利息差额合计");
        KEY_MAP.put("subDkyeTotal", "期末贷款余额差额合计");

        KEY_MAP.put("tsdkye", "推算的贷款余额");
        KEY_MAP.put("sjdkye", "实际贷款余额");
        KEY_MAP.put("subDkye", "推算贷款余额-实际贷款余额");
    }
//    static {
//        KEY_MAP = new LinkedHashMap<>();
//        KEY_MAP.put("dkzh", "贷款账号");
//        KEY_MAP.put("dkffrq", "贷款发放日期");
//        KEY_MAP.put("dkqs", "贷款期数");
//        KEY_MAP.put("csdkye", "初始贷款余额");
//        KEY_MAP.put("csyqbj", "初始逾期本金");
//        KEY_MAP.put("csqs", "应该开始期数");
//        KEY_MAP.put("tsdkye", "推算贷款余额");
//        KEY_MAP.put("sjdkye", "实际贷款余额");
//        KEY_MAP.put("subdkye", "推算-实际(贷款余额)");
//    }

    public static String logName = outFileName + ".log";


    /**
     * 输出到excel
     */
    public static void listToXlsx() {
        System.out.println("开始=====>" + outXLSXName);
        ExcelUtil.exportExcel(KEY_MAP, dataset, outXLSXStream);
        System.out.println("结束=====>" + outXLSXName);
        try {
            outXLSXStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入一部分日志到文件
     */
    public static void logsToFile() {

        try (FileWriter writer = new FileWriter(logName, true)) {
            System.out.print(logs.toString());
            writer.write(logs.toString());
            logs = new StringBuffer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建失败!");
        }
    }
}
