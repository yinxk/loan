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
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.original.loan.repayment.RepaymentMethod;
import top.yinxiaokang.original.loan.repayment.RepaymentMonthRateScale;
import top.yinxiaokang.original.loan.repayment.RepaymentPlan;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.others.CurrentPeriodRange;
import top.yinxiaokang.util.Common;

import java.io.*;
import java.math.BigDecimal;
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

    private static final String KEY_ISGENERATE = "isGenerate";

    private static final String KEY_NOTGENERATE = "notGenerate";

    private static final String KEY_PREPAYMENT = "prepayment";

    /**
     * 误差范围
     */
    private static final BigDecimal ERROR_RANGE = new BigDecimal("0.02");


    public static void main(String[] args) {

        AccountCheckMain checkMain = new AccountCheckMain();

        //File f = new File("src/test/resources/初始有逾期.xlsx");
        File f = new File("src/test/resources/20180821-误差5块以内的.xlsx");

        Collection<Map> importExcel = new ArrayList<>();
        try {
            try (InputStream inputStream = new FileInputStream(f)) {
                ExcelLogs logs = new ExcelLogs();
                importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        doAnalyzeWuchaIn5(accountInformationsList, checkMain);
        logs.append("读取总条数: " + importExcel.size() + "\n");
        String fileName = "accountCheckLog.log";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(logs.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建失败!");
        }
        System.out.println("结束运行!");
    }

    private static void doAnalyzeWuchaIn5(List<AccountInformations> accountInformationsList, AccountCheckMain checkMain) {
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
        System.out.println(prepayment.size());
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

    private static void doAnalyze(AccountCheckMain checkMain, List<AccountInformations> informations, int dealNum) {
        for (AccountInformations item : informations) {
            logs.append("开始处理第: " + (++dealNum) + " 条 \n");
            if (item.getSthousingAccount() == null || item.getSthousingAccount().getDkffrq() == null) continue;
            List<Integer> reverseBxQc = checkMain.analyzeReverseBx(item);
            checkMain.analyze(item, reverseBxQc);
            if (reverseBxQc.size() == 0) {
                logs.append("本息相反的期次: 无\n");
            } else {
                logs.append("本息相反的期次: " + reverseBxQc.toString() + "\n");
            }
            logs.append("结束处理第: " + dealNum + " 条 \n\n");
        }
    }


    /**
     * 获取本息反了的期次
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
        List<SthousingDetail> details = informations.getDetails();
        int numId = 0;
        for (SthousingDetail detail : details) {
            if (detail.getDqqc().compareTo(informations.getInitFirstQc()) < 0)
                continue;
            RepaymentItem item = Common.getRepaymentItemByDqqc(repaymentItems, detail.getDqqc().intValue());
            if (item == null) continue;
            if (item.getHkbjje().subtract(detail.getLxje()).abs().compareTo(ERROR_RANGE) <= 0
                    && item.getHklxje().subtract(detail.getBjje()).abs().compareTo(ERROR_RANGE) <= 0) {
                reverseQc.add(detail.getDqqc().intValue());
            }

        }
        return reverseQc;
    }


    /**
     * 对一个账号的业务进行分析
     *
     * @param informations
     * @param reverseQc
     */
    public void analyze(AccountInformations informations, List<Integer> reverseQc) {

        logs.append("贷款账号: " + informations.getSthousingAccount().getDkzh() +
                " , 初始贷款余额 : " + informations.getInitInformation().getCsye() +
                " , 初始逾期本金 : " + informations.getInitInformation().getCsyqbj() +
                " , 初始期数: " + informations.getInitFirstQc() +
                " , 贷款发放日期: " + (informations.getSthousingAccount().getDkffrq() == null ? "" : Utils.SDF_YEAR_MONTH_DAY.format(informations.getSthousingAccount().getDkffrq())) +
                " , 贷款期数: " + informations.getSthousingAccount().getDkqs() +
                " , 初始期数正确性: " + (informations.getInitFirstQc().compareTo(informations.getSthousingAccount().getDkqs()) > 0 ? "错误" : "正确") + " \n");
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
