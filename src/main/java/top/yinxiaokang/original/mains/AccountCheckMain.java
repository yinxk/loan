package top.yinxiaokang.original.mains;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import top.yinxiaokang.original.LoanRepaymentAlgorithm;
import top.yinxiaokang.original.Utils;
import top.yinxiaokang.original.dto.AccountInformations;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.enums.LoanBusinessType;
import top.yinxiaokang.original.excelbean.OneThousand;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.original.loan.repayment.RepaymentMethod;
import top.yinxiaokang.original.loan.repayment.RepaymentMonthRateScale;
import top.yinxiaokang.original.loan.repayment.RepaymentPlan;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.others.CurrentPeriodRange;
import top.yinxiaokang.util.Common;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/8/6 11:45
 */
public class AccountCheckMain {
    private AccountCheck accountCheck = new AccountCheck();
    /**
     * 日志
     */
    private static StringBuffer logs = new StringBuffer();
    private static String outFileName = "从30多期跳到170多期";
    //private static String outFileName = "1400多个贷款账号分析";
    /**
     * excel
     */
    private static String outXLSXName = outFileName + ".xlsx";
    private static List<OneThousand> dataset = new ArrayList<>();
    private static OutputStream outXLSXStream = null;
    private static Map<String, String> KEY_MAP = null;

    static {
        KEY_MAP = new LinkedHashMap<>();
        KEY_MAP.put("dkzh", "贷款账号");
        KEY_MAP.put("csdkye", "初始贷款余额");
        KEY_MAP.put("csyqbj", "初始逾期本金");
        KEY_MAP.put("hklx", "还款类型");
        KEY_MAP.put("rq", "日期");
        KEY_MAP.put("qc", "期次");
        KEY_MAP.put("fse", "发生额");
        KEY_MAP.put("bj", "本金");
        KEY_MAP.put("lx", "利息");
        KEY_MAP.put("qmdkye", "期末贷款余额");
    }

    private static String logName = outFileName + ".log";

    private static final String KEY_ISGENERATE = "isGenerate";

    private static final String KEY_NOTGENERATE = "notGenerate";

    private static final String KEY_PREPAYMENT = "prepayment";

    /**
     * 误差范围
     */
    private static final BigDecimal ERROR_RANGE = new BigDecimal("0.02");


    public AccountCheckMain() {
        try {
            outXLSXStream = new FileOutputStream(new File(outXLSXName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        AccountCheckMain checkMain = new AccountCheckMain();

//        File f = new File("src/test/resources/1000多个问题贷款账号.xlsx");
        File f = new File("src/test/resources/从30多期跳到170多期.xlsx");
        //File f = new File("src/test/resources/初始有逾期.xlsx");
        //File f = new File("src/test/resources/20180821-误差5块以内的.xlsx");

        Collection<Map> importExcel = new ArrayList<>();
        try {
            try (InputStream inputStream = new FileInputStream(f)) {
                ExcelLogs logs = new ExcelLogs();
                importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File logFile = new File(logName);
        if (logFile.isFile() && logFile.exists()) {
            System.out.println("文件存在, 删除文件!");
            logFile.delete();
        }

        logs.append("读取总条数: " + importExcel.size() + "\n");

        ArrayList<InitInformation> initHasOverdueList = new ArrayList<>();

        for (Map m : importExcel) {
            InitInformation initHasOverdue = new InitInformation();
            initHasOverdue.setDkzh((String) m.get("dkzh"));
            initHasOverdue.setCsye(new BigDecimal((String) m.get("csye")));
            initHasOverdue.setCsqs(new BigDecimal((String) m.get("csqs")));
            initHasOverdue.setCsyqbj(new BigDecimal((String) m.get("csyqbj")));
            initHasOverdueList.add(initHasOverdue);
        }

        List<AccountInformations> accountInformationsList = new ArrayList<>();
        for (InitInformation initInformation : initHasOverdueList) {
            AccountInformations accountInformations = checkMain.toAccountInformations(initInformation);
            accountInformationsList.add(accountInformations);
        }
        //doAnalyzeInitHasOverdue(accountInformationsList, checkMain);
        //doAnalyzeWuchaIn5(accountInformationsList, checkMain);
        doAnalyzeOneThousandDkzh(accountInformationsList, checkMain);
        logs.append("读取总条数: " + importExcel.size() + "\n");
        logsToFile();
        listToXlsx();
        System.out.println("结束运行!");
    }


    /**
     * 输出到excel
     */
    private static void listToXlsx() {
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
    private static void logsToFile() {

        try (FileWriter writer = new FileWriter(logName, true)) {
            System.out.print(logs.toString());
            writer.write(logs.toString());
            logs = new StringBuffer();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建失败!");
        }
    }

    /**
     * 分析误差在5块内的
     *
     * @param accountInformationsList
     * @param checkMain
     */
    private static void doAnalyzeWuchaIn5(List<AccountInformations> accountInformationsList, AccountCheckMain checkMain) {
        int dealNum = 0;
        logs.append("==================================================--start--==========================================\n");
        doAnalyze(checkMain, accountInformationsList, dealNum);
        logs.append("==================================================--end--==========================================\n");
    }

    /**
     * 1000多个有问题的贷款账号
     *
     * @param accountInformationsList
     * @param checkMain
     */
    private static void doAnalyzeOneThousandDkzh(List<AccountInformations> accountInformationsList, AccountCheckMain checkMain) {
        int dealNum = 0;
        logs.append("==================================================--start--==========================================\n");
        doAnalyze(checkMain, accountInformationsList, dealNum);
        logs.append("==================================================--end--==========================================\n");
    }

    /**
     * 对于初始有逾期的进行分析
     *
     * @param accountInformationsList
     * @param checkMain
     */
    private static void doAnalyzeInitHasOverdue(List<AccountInformations> accountInformationsList, AccountCheckMain checkMain) {
        Map<String, List<AccountInformations>> generateOrNotGenerateList = checkMain.isGenerateOrNotGenerateList(accountInformationsList);
        List<AccountInformations> isGenerate = generateOrNotGenerateList.get(KEY_ISGENERATE);
        List<AccountInformations> notGenerate = generateOrNotGenerateList.get(KEY_NOTGENERATE);
        List<AccountInformations> prepayment = generateOrNotGenerateList.get(KEY_PREPAYMENT);
        System.out.println("存在提前还款业务的账号数量: " + prepayment.size());
        int dealNum = 0;
        logs.append("==================================================start--已经产生业务==========================================\n");
        doAnalyze(checkMain, isGenerate, dealNum);
        logs.append("==================================================end--已经产生业务==========================================\n");
        logs.append("==================================================start--没有产生业务==========================================\n");
        doAnalyze(checkMain, notGenerate, dealNum);
        logs.append("==================================================end--没有产生业务==========================================\n");

        logs.append("已经产生业务账号数: " + isGenerate.size() + "\n");
        logs.append("没有产生业务账号数: " + notGenerate.size() + "\n");
    }

    /**
     * 分析器 ,  每种经过该方法,  该方法再调用其他需要的模块
     *
     * @param checkMain
     * @param informations
     * @param dealNum
     */
    private static void doAnalyze(AccountCheckMain checkMain, List<AccountInformations> informations, int dealNum) {
        int writeTag = 0;
        for (AccountInformations item : informations) {
            logs.append("开始分析第 " + (++dealNum) + " 条 \n");
            if (item.getSthousingAccount() == null || item.getSthousingAccount().getDkffrq() == null) continue;
            List<Integer> reverseBxQc = checkMain.analyzeReverseBx(item);
            logs.append("贷款账号: " + item.getSthousingAccount().getDkzh() +
                    " , 初始贷款余额 : " + item.getInitInformation().getCsye() +
                    " , 初始逾期本金 : " + item.getInitInformation().getCsyqbj() +
                    " , 初始期数: " + item.getInitFirstQc() +
                    " , 贷款发放日期: " + (item.getSthousingAccount().getDkffrq() == null ? "" : Utils.SDF_YEAR_MONTH_DAY.format(item.getSthousingAccount().getDkffrq())) +
                    " , 贷款期数: " + item.getSthousingAccount().getDkqs() +
                    " , 初始期数正确性: " + (item.getInitFirstQc().compareTo(item.getSthousingAccount().getDkqs()) > 0 ? "错误" : "正确") + " \n");

            OneThousand oneThousand = new OneThousand();
            oneThousand.setDkzh(item.getSthousingAccount().getDkzh());
            oneThousand.setCsdkye(item.getInitInformation().getCsye());
            oneThousand.setCsyqbj(item.getInitInformation().getCsyqbj());
            dataset.add(oneThousand);
            //checkMain.analyze(item, reverseBxQc);
            //checkMain.analyzeInitHasOverdueLx(item, reverseBxQc);
            checkMain.analyzeOneThousandDkzh(item, reverseBxQc);
            if (reverseBxQc.size() == 0) {
                logs.append("本息相反的期次: 无\n");
            } else {
                logs.append("本息相反的期次: " + reverseBxQc.toString() + "\n");
            }
            logs.append("结束分析第 " + dealNum + " 条 \n\n");
            if ((++writeTag) % 100 == 0) {
                logsToFile();
            }
        }
    }

    /**
     * 获取本息反了的期次(只能参考)
     *
     * @param informations
     * @return
     */
    public List<Integer> analyzeReverseBx(AccountInformations informations) {
        List<Integer> reverseQc = new ArrayList<>();
        List<RepaymentItem> repaymentItems = accountCheck.repaymentItems(informations.getSthousingAccount(),
                informations.getCurrentPeriodRanges(),
                informations.getInitInformation().getCsye(),
                informations.getInitInformation().getCsyqbj(),
                false);
        // 前一项是否为提前还款
        boolean isPreItemPrepayment = false;
        // 已经根据期次顺序排序
        List<SthousingDetail> details = informations.getDetails();
        for (SthousingDetail detail : details) {
            // 过滤不是我们系统的期次的业务
            if (detail.getDqqc().compareTo(informations.getInitFirstQc()) < 0)
                continue;
            RepaymentItem item = Common.getRepaymentItemByDqqc(repaymentItems, detail.getDqqc().intValue());
            if (item == null)
                continue;

            // 存在提前还款 , 需要新的还款计划 , 根据业务中提前还款剩余的余额进行推算, 期次也是 ,如果该业务的期次或者期末余额有一项不对, 那么提前还款后的本息倒置得不到有效的结果
            if (LoanBusinessType.提前还款.getCode().equals(detail.getDkywmxlx())) {
                repaymentItems = RepaymentPlan.listRepaymentPlan(detail.getXqdkye(), informations.getSthousingAccount().getDkffrq()
                        , informations.getSthousingAccount().getDkqs().subtract(detail.getDqqc()).intValue(), informations.getSthousingAccount().getDkll(),
                        RepaymentMethod.getRepaymentMethodByCode(informations.getSthousingAccount().getDkhkfs()), detail.getDqqc().intValue(), RepaymentMonthRateScale.NO);
                isPreItemPrepayment = true;
            }
            // 提前还款或者结清没有本息倒置的情况, 与还款计划比较, 自动过滤了, 不需要考虑

            // 前一项为提前还款 , 由于提前还款后第一期利息比还款计划多, 那么只能比较本金来 , 可能是本息颠倒
            if (isPreItemPrepayment) {
                isPreItemPrepayment = false;
                if (item.getHkbjje().subtract(detail.getLxje()).abs().compareTo(ERROR_RANGE) <= 0) {
                    reverseQc.add(detail.getDqqc().intValue());
                }
            } else if (item.getHkbjje().subtract(detail.getLxje()).abs().compareTo(ERROR_RANGE) <= 0
                    && item.getHklxje().subtract(detail.getBjje()).abs().compareTo(ERROR_RANGE) <= 0) {
                reverseQc.add(detail.getDqqc().intValue());
            }

        }
        return reverseQc;
    }


    /**
     * 1000多个问题账号分析 , 不根据业务, 推算正常应该还款的业务
     *
     * @param informations
     * @param reverseQc
     */
    public void analyzeOneThousandDkzh(AccountInformations informations, List<Integer> reverseQc) {
        // 业务记录
        List<SthousingDetail> details = informations.getDetails();
        BigDecimal csye = informations.getInitInformation().getCsye();
        BigDecimal dkyeByCsye = csye;
        // 根据业务推算的余额 , 减去初始逾期本金对我们系统的业务进行分析 , 计算
        BigDecimal dkyeByYeWu = csye.subtract(informations.getInitInformation().getCsyqbj());

        // 还款计划
        List<RepaymentItem> repaymentItems = informations.getRepaymentItems();
        // 提前还款的业务, 已排序
        List<SthousingDetail> prepaymentList = listPrepayment(details);
        // 提前还款次数
        int preTag = 0;
        // 现在时间
//        Date now = new Date();

        Date now = null;
        analyOneThousand0(informations, repaymentItems, prepaymentList, preTag, now, null);


    }

    private void analyOneThousand0(AccountInformations informations, List<RepaymentItem> repaymentItems, List<SthousingDetail> prepaymentList, int preTag, Date now, Date preDetailYwfsrq) {
        SthousingDetail preDetail;
        for (int i = 0; i < repaymentItems.size(); i++) {
            RepaymentItem repaymentItem = repaymentItems.get(i);
            RepaymentItem pre = null;
            if (i > 0) {
                pre = repaymentItems.get(i - 1);
            }
            preDetail = null;
            if (preTag < prepaymentList.size()) {
                preDetail = prepaymentList.get(preTag);
            }
            if (now != null && Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()).compareTo(Utils.SDF_YEAR_MONTH_DAY.format(now)) > 0) {
                break;
            }
            // 跳过的期次
//            if (repaymentItem.getHkqc() == 36 || repaymentItem.getHkqc() == 37) {
//                repaymentItem.setQcdkye(pre.getQmdkye());
//                repaymentItem.setQmdkye(pre.getQmdkye());
//                continue;
//            }
            if (preDetail == null || Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()).compareTo(Utils.SDF_YEAR_MONTH_DAY.format(preDetail.getYwfsrq())) < 0) {
                // 前一期是提前还款  ,  后面一期的利息要多点
                if (preDetailYwfsrq != null) {
                    int lxts = LoanRepaymentAlgorithm.differentDaysByMillisecond(preDetailYwfsrq, repaymentItem.getHkrq()) - 30;
                    BigDecimal lx = LoanRepaymentAlgorithm.calInterestByInterestDays(repaymentItem.getQcdkye(),
                            informations.getSthousingAccount().getDkll(), lxts);
                    repaymentItem.setHklxje(repaymentItem.getHklxje().add(lx));
                    repaymentItem.setFse(repaymentItem.getFse().add(lx));
                    preDetailYwfsrq = null;
                }
                logs.append("正常还款    日期: " + Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()) + "  期次: " + repaymentItem.getHkqc() +
                        "  发生额: " + repaymentItem.getFse() + "  本金: " + repaymentItem.getHkbjje() + "  利息: " + repaymentItem.getHklxje() +
                        "  期末余额: " + repaymentItem.getQmdkye() + "\n");
                OneThousand oneThousand = new OneThousand();
                oneThousand.setHklx("正常还款");
                oneThousand.setRq(repaymentItem.getHkrq());
                oneThousand.setQc(repaymentItem.getHkqc());
                oneThousand.setFse(repaymentItem.getFse());
                oneThousand.setBj(repaymentItem.getHkbjje());
                oneThousand.setLx(repaymentItem.getHklxje());
                oneThousand.setQmdkye(repaymentItem.getQmdkye());
                dataset.add(oneThousand);
            } else {
                BigDecimal qmdkye = pre.getQmdkye().subtract(preDetail.getBjje());
                boolean isJieQing = false;

                OneThousand oneThousand = new OneThousand();
                if (LoanBusinessType.提前还款.getCode().equals(preDetail.getDkywmxlx())) {
                    logs.append("提前还款    ");
                    oneThousand.setHklx("提前还款");
                }
                if (LoanBusinessType.结清.getCode().equals(preDetail.getDkywmxlx())) {
                    logs.append("结清    ");
                    oneThousand.setHklx("结清");
                    isJieQing = true;
                }
                logs.append("日期: " + Utils.SDF_YEAR_MONTH_DAY.format(preDetail.getYwfsrq()) + "  期次: " + repaymentItem.getHkqc() +
                        "  发生额: " + preDetail.getFse() + "  本金: " + preDetail.getBjje() + " 利息: " + preDetail.getLxje() +
                        "  期末余额: " + qmdkye + "\n");
                oneThousand.setRq(preDetail.getYwfsrq());
                oneThousand.setQc(repaymentItem.getHkqc());
                oneThousand.setFse(preDetail.getFse());
                oneThousand.setBj(preDetail.getBjje());
                oneThousand.setLx(preDetail.getLxje());
                oneThousand.setQmdkye(qmdkye);
                dataset.add(oneThousand);
                if (isJieQing) {
                    break;
                }
                preTag++;
                repaymentItems = RepaymentPlan.listRepaymentPlan(qmdkye,
                        repaymentItem.getHkrq(),
                        informations.getSthousingAccount().getDkqs().intValue() - repaymentItem.getHkqc(),
                        informations.getSthousingAccount().getDkll(),
                        RepaymentMethod.getRepaymentMethodByCode(informations.getSthousingAccount().getDkhkfs()),
                        repaymentItem.getHkqc(),
                        RepaymentMonthRateScale.NO);
                analyOneThousand0(informations, repaymentItems, prepaymentList, preTag, now, preDetail.getYwfsrq());
                break;
            }
        }
    }


    /**
     * 获取提前还款或者结清的业务,并根据提前还款的业务发生日期进行排序
     *
     * @param details
     * @return
     */
    public List<SthousingDetail> listPrepayment(List<SthousingDetail> details) {
        List<SthousingDetail> prepaymentList = new ArrayList<>();
        for (SthousingDetail detail : details) {
            if (LoanBusinessType.提前还款.getCode().equals(detail.getDkywmxlx()) || LoanBusinessType.结清.getCode().equals(detail.getDkywmxlx())) {
                prepaymentList.add(detail);
            }
        }
        Collections.sort(prepaymentList, Comparator.comparing(SthousingDetail::getYwfsrq));
        return prepaymentList;
    }

    /**
     * 有些业务没有连续扣款, 那么根据业务推算余额进行计算利息 , 一个账号多的利息(参考)
     *
     * @param informations
     * @param reverseQc
     */
    public void analyzeInitHasOverdueLx(AccountInformations informations, List<Integer> reverseQc) {
        // 业务记录
        List<SthousingDetail> details = informations.getDetails();
        BigDecimal csye = informations.getInitInformation().getCsye();
        BigDecimal dkyeByCsye = csye;
        boolean isCsYw = false;
        // 根据业务推算的余额 , 减去初始逾期本金对我们系统的业务进行分析,计算
        BigDecimal dkyeByYeWu = csye.subtract(informations.getInitInformation().getCsyqbj());
        // 利息差额合计
        BigDecimal lxSum = BigDecimal.ZERO;
        // 每一期利息
        BigDecimal lxItem = BigDecimal.ZERO;
        // 每一期利息差额
        BigDecimal lxItemSub = BigDecimal.ZERO;
        for (SthousingDetail detail : details) {
            // 过滤不是我们系统的期次的业务
            if (detail.getDqqc().compareTo(informations.getInitFirstQc()) < 0) {
                logs.append(detail + " 推算期末余额: " + dkyeByCsye.subtract(detail.getBjje()) +
                        " 推末-业末: " + dkyeByCsye.subtract(detail.getBjje()).subtract(detail.getXqdkye()) + "  根据期次判断,该期为导入的数据\n");
                dkyeByCsye = dkyeByCsye.subtract(detail.getBjje());
                isCsYw = true;
                continue;
            }
            if (isCsYw) {
                logs.append("推算导入系统逾期记录最后余额 - 等于初始余额减初始逾期本金 : " + (dkyeByCsye.subtract(dkyeByYeWu)) + "\n");
                isCsYw = false;
            }
            lxItem = LoanRepaymentAlgorithm.calLxByDkye(dkyeByYeWu, informations.getSthousingAccount().getDkll(), RepaymentMonthRateScale.NO);

            if (Arrays.asList(LoanBusinessType.结清.getCode(), LoanBusinessType.提前还款.getCode()).contains(detail.getDkywmxlx())) {
                lxItem = detail.getLxje();
            }
            logs.append(detail);
            if (reverseQc.contains(detail.getDqqc().intValue())) {
                lxItemSub = detail.getBjje().subtract(lxItem);
                logs.append(" 推算期末余额: " + dkyeByYeWu.subtract(detail.getLxje()) + " 推末-业末: " + dkyeByYeWu.subtract(detail.getLxje()).subtract(detail.getXqdkye()) +
                        " 推算期初余额: " + dkyeByYeWu + " 推算利息: " + lxItem + " 业务利息-推算利息: " + lxItemSub + " 该期本息倒置\n");
                dkyeByYeWu = dkyeByYeWu.subtract(detail.getLxje());

            } else {
                lxItemSub = detail.getLxje().subtract(lxItem);
                logs.append(" 推算期末余额: " + dkyeByYeWu.subtract(detail.getBjje()) + " 推末-业末: " + dkyeByYeWu.subtract(detail.getLxje()).subtract(detail.getXqdkye()) +
                        " 推算期初余额: " + dkyeByYeWu + " 推算利息: " + lxItem + " 业务利息-推算利息: " + lxItemSub + " \n");
                dkyeByYeWu = dkyeByYeWu.subtract(detail.getBjje());
            }
            lxSum = lxSum.add(lxItemSub);

        }
        logs.append("利息差额总额: " + lxSum + " 推算贷款余额:" + dkyeByYeWu + " 实际贷款余额: " + informations.getSthousingAccount().getDkye() +
                " 推算余额-实际余额:" + dkyeByYeWu.subtract(informations.getSthousingAccount().getDkye()) +
                " 差额是否与初始逾期本金相等: " + (dkyeByYeWu.subtract(informations.getSthousingAccount().getDkye()).abs().compareTo(informations.getInitInformation().getCsyqbj()) == 0 ? "是" : "否") + "\n");
    }

    /**
     * 对一个账号的业务进行分析(存在误差的业务的分析)
     *
     * @param informations
     * @param reverseQc
     */
    public void analyze(AccountInformations informations, List<Integer> reverseQc) {

        // 业务记录
        List<SthousingDetail> details = informations.getDetails();
        // 初始还款计划 , 没有做提前还款
        List<RepaymentItem> repaymentItems = informations.getRepaymentItems();
        // 初始余额
        BigDecimal csye = informations.getInitInformation().getCsye();
        // 根据业务推算的余额
        BigDecimal dkyeByYeWu = csye;
        // 误差
        BigDecimal wuCha = BigDecimal.ZERO;

        BigDecimal subtractBjje = null;
        BigDecimal subtractLxje = null;
        BigDecimal subtractQmdkye = null;

        BigDecimal oddYwdkye = BigDecimal.ZERO;
        BigDecimal lxEight = BigDecimal.ZERO;
        BigDecimal lxTen = BigDecimal.ZERO;
        BigDecimal monthRateEight = LoanRepaymentAlgorithm.convertYearRateToMonthRate(informations.getSthousingAccount().getDkll(), RepaymentMonthRateScale.YES);
        BigDecimal monthRateTen = LoanRepaymentAlgorithm.convertYearRateToMonthRate(informations.getSthousingAccount().getDkll(), RepaymentMonthRateScale.NO);

        // 现在实际余额
        BigDecimal nowDkye = informations.getSthousingAccount().getDkye();

        for (SthousingDetail detail : details) {
            oddYwdkye = detail.getBjje().add(detail.getXqdkye());
            lxEight = oddYwdkye.multiply(monthRateEight).setScale(2, BigDecimal.ROUND_HALF_UP);
            lxTen = oddYwdkye.multiply(monthRateTen).setScale(2, BigDecimal.ROUND_HALF_UP);

            dkyeByYeWu = dkyeByYeWu.subtract(detail.getBjje());
            // 存在提前还款 , 需要新的还款计划
            if (LoanBusinessType.提前还款.getCode().equals(detail.getDkywmxlx())) {
                repaymentItems = RepaymentPlan.listRepaymentPlan(dkyeByYeWu, informations.getSthousingAccount().getDkffrq()
                        , informations.getSthousingAccount().getDkqs().subtract(detail.getDqqc()).intValue(), informations.getSthousingAccount().getDkll(),
                        RepaymentMethod.getRepaymentMethodByCode(informations.getSthousingAccount().getDkhkfs()), detail.getDqqc().intValue(), RepaymentMonthRateScale.NO);
            }
            RepaymentItem repaymentItemByDqqc = Common.getRepaymentItemByDqqc(repaymentItems, detail.getDqqc().intValue());
            logs.append(detail);
            if (repaymentItemByDqqc != null) {
                subtractBjje = repaymentItemByDqqc.getHkbjje().subtract(detail.getBjje());
                subtractLxje = repaymentItemByDqqc.getHklxje().subtract(detail.getLxje());
                subtractQmdkye = repaymentItemByDqqc.getQmdkye().subtract(detail.getXqdkye());
                wuCha = wuCha.add(subtractBjje);
                logs.append(",利息(8): " + lxEight + " 利息(8)-业务利息: " + lxEight.subtract(detail.getLxje()) + " 利息(10): " + lxTen + " 利息(10)-业务利息: " + lxTen.subtract(detail.getLxje()) +
                        " 发生额(计划): " + repaymentItemByDqqc.getFse() + " 本金:" + repaymentItemByDqqc.getHkbjje() + " 利息: " + repaymentItemByDqqc.getHklxje() +
                        " 期末余额: " + repaymentItemByDqqc.getQmdkye() + "  本金误差(计划-业务): " + subtractBjje +
                        " 利息误差: " + subtractLxje + " 期末余额误差: " + subtractQmdkye);
            }
            logs.append("\n");
        }
        logs.append("实际余额-业务推算余额 : " + (nowDkye.subtract(dkyeByYeWu)) + "  本金差额总额: " + wuCha + "\n");
    }


    /**
     * 将每个账号有关的信息转换整理
     *
     * @param initInformation
     * @return
     */
    public AccountInformations toAccountInformations(InitInformation initInformation) {
        AccountInformations accountInformations = new AccountInformations();
        SthousingAccount account = accountCheck.getSthousingAccount(initInformation.getDkzh());
        // 仅仅针对单个账号
        try {
            account.setDkffrq(Utils.SDF_YEAR_MONTH_DAY.parse("2015-01-21"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //System.out.println(account);
        if (account == null)
            return accountInformations;
        List<CurrentPeriodRange> ranges = accountCheck.listHSRange(account, null);
        BigDecimal yhqs = accountCheck.yhqs(ranges);
        BigDecimal initFirstQc = yhqs.add(BigDecimal.ONE);
        // 该账号已入账的业务记录
        List<SthousingDetail> sthousingDetails = accountCheck.listDetails(account);
        Collections.sort(sthousingDetails, Comparator.comparing(SthousingDetail::getDqqc));
        // 还款计划
        List<RepaymentItem> repaymentItems = accountCheck.repaymentItems(account, ranges, initInformation.getCsye(), initInformation.getCsyqbj(), true);
        accountInformations.setSthousingAccount(account);
        accountInformations.setCurrentPeriodRanges(ranges);
        accountInformations.setYhqs(yhqs);
        accountInformations.setSyqs(accountCheck.syqs(yhqs, account));
        accountInformations.setInitFirstQc(initFirstQc);
        accountInformations.setDetails(sthousingDetails);
        accountInformations.setInitInformation(initInformation);
        accountInformations.setRepaymentItems(repaymentItems);
        return accountInformations;
    }


    public Map<String, List<AccountInformations>> isGenerateOrNotGenerateList(List<AccountInformations> list) {
        Map<String, List<AccountInformations>> result = new HashMap<>();
        List<AccountInformations> isGenerate = new ArrayList<>();
        List<AccountInformations> notGenerate = new ArrayList<>();
        List<AccountInformations> isPrepayment = new ArrayList<>();

        for (AccountInformations item : list) {
            isGenerate(item);
            if (item.isGenerated()) {
                isGenerate.add(item);
            } else {
                notGenerate.add(item);
            }
            if (item.isPrepayment()) {
                isPrepayment.add(item);
            }

        }
        result.put(KEY_ISGENERATE, isGenerate);
        result.put(KEY_NOTGENERATE, notGenerate);
        result.put(KEY_PREPAYMENT, isPrepayment);

        return result;
    }

    public AccountInformations isGenerate(AccountInformations accountInformations) {

        // 该账号已入账的业务记录
        List<SthousingDetail> sthousingDetails = accountInformations.getDetails();

        // 是否连续
        boolean isContinuous = true;
        // 是否已产生入账的业务
        boolean isGenerated = false;
        // 是否有提前还款
        boolean isPrepayment = false;
        BigDecimal qsqs = accountInformations.getInitFirstQc();
        for (SthousingDetail detail : sthousingDetails) {
            if (detail.getDqqc().compareTo(accountInformations.getInitFirstQc()) >= 0) {
                if (LoanBusinessType.提前还款.getCode().equals(detail.getDkywmxlx()))
                    isPrepayment = true;
                isGenerated = true;
                if (detail.getDqqc().compareTo(qsqs) != 0) {
                    isContinuous = false;
                }
                qsqs = qsqs.add(BigDecimal.ONE);

            }
        }
        accountInformations.setGenerated(isGenerated);
        accountInformations.setContinuous(isContinuous);
        accountInformations.setPrepayment(isPrepayment);
        return accountInformations;
    }
}
