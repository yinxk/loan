package top.yinxiaokang.original.mains;

import top.yinxiaokang.original.CommLoanAlgorithm;
import top.yinxiaokang.original.LoanRepaymentAlgorithm;
import top.yinxiaokang.original.Utils;
import top.yinxiaokang.original.dto.AccountInformations;
import top.yinxiaokang.original.entity.StOverdue;
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
import top.yinxiaokang.util.Common;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static top.yinxiaokang.util.Common.NO_MESS;
import static top.yinxiaokang.util.Common.NO_MESS_CHINESE;
import static top.yinxiaokang.util.FileCommon.*;

/**
 * @author yinxk
 * @date 2018/8/6 11:45
 */
public class AccountCheckMain {
    private static AccountCheck accountCheck = new AccountCheck();

    private static final String KEY_ISGENERATE = "isGenerate";

    private static final String KEY_NOTGENERATE = "notGenerate";

    private static final String KEY_PREPAYMENT = "prepayment";

    public AccountCheckMain() {
    }


    public static void main(String[] args) {

        AccountCheckMain checkMain = new AccountCheckMain();

        Collection<Map> importExcel = Common.xlsToList(inFileName);


        File logFile = new File(logName);
        if (logFile.isFile() && logFile.exists()) {
            System.out.println("文件存在, 删除文件!");
            logFile.delete();
        }

        int size = importExcel.size();
        logs.append("=============================================写入时间: "+ LocalDateTime.now().toString() + "=============================================\n");
        logs.append("读取总条数: " + size + "\n");

        List<InitInformation> initInformationList = Common.importExcelToInitInformationList(importExcel);
        // 置空, 让虚拟机GC的时候清理掉
        importExcel = null;
        List<InitInformation> errorList = new ArrayList<>();
        List<AccountInformations> accountInformationsList = new ArrayList<>();
        for (InitInformation initInformation : initInformationList) {
            AccountInformations accountInformations = accountCheck.toAccountInformations(initInformation);
            if (accountInformations == null) {
                errorList.add(initInformation);
                continue;
            }
            accountInformationsList.add(accountInformations);
        }
        //doAnalyzeInitHasOverdue(accountInformationsList, checkMain);
        doAnalyze(accountInformationsList, checkMain);
        logs.append("读取总条数: " + size + "\n");
        if (errorList.size() > 0) {
            logs.append("错误信息 :");
            for (InitInformation initInformation : errorList) {
                logs.append(initInformation);
            }
            logs.append("\n");
        }
        logsToFile();
        listToXlsx();
        System.out.println("************************************************************************************************************************************************************");
        System.out.println("**************************************************************************结束运行!*************************************************************************");
        System.out.println("************************************************************************************************************************************************************");
    }

    /**
     * 一般情况
     *
     * @param accountInformationsList
     * @param checkMain
     */
    private static void doAnalyze(List<AccountInformations> accountInformationsList, AccountCheckMain checkMain) {
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
        // 对于即将扣款分析, 排序 , 其他不用排序
        //Collections.sort(informations, Comparator.comparing(o -> o.getSthousingAccount().getDkxffrq()));
        for (AccountInformations item : informations) {
            logs.append("开始分析第 " + (++dealNum) + " 条 \n");
            if (item.getSthousingAccount() == null || item.getSthousingAccount().getDkffrq() == null) continue;
            List<Integer> reverseBxQc = accountCheck.analyzeReverseBx(item);
            logs.append("贷款账号: " + item.getSthousingAccount().getDkzh() +
                    " , 初始贷款余额 : " + item.getInitInformation().getCsye() +
                    " , 初始逾期本金 : " + item.getInitInformation().getCsyqbj() +
                    " , 初始期数: " + item.getInitFirstQc() +
                    " , 贷款发放日期: " + (item.getSthousingAccount().getDkffrq() == null ? "" : Utils.SDF_YEAR_MONTH_DAY.format(item.getSthousingAccount().getDkffrq())) +
                    " , 贷款期数: " + item.getSthousingAccount().getDkqs() +
                    " , 初始期数正确性: " + (item.getInitFirstQc().compareTo(item.getSthousingAccount().getDkqs()) > 0 ? "错误" : "正确") + " \n");

            //region 对于1400多个账号或者是对于30多期跳到170多期的账号
            OneThousand oneThousand = new OneThousand();
            oneThousand.setDkzh(item.getSthousingAccount().getDkzh());
            oneThousand.setCsdkye(item.getInitInformation().getCsye());
            oneThousand.setCsyqbj(item.getInitInformation().getCsyqbj());
            oneThousand.setDkffrq(item.getSthousingAccount().getDkffrq());
            oneThousand.setDkqs(item.getSthousingAccount().getDkqs());
            oneThousand.setCsqs(item.getInitFirstQc());
            oneThousand.setCsqszqx((item.getInitFirstQc().compareTo(item.getSthousingAccount().getDkqs()) > 0 ? "错误" : "正确"));
            dataset.add(oneThousand);
            //endregion

            //checkMain.analyze(item, reverseBxQc);
            //checkMain.analyzeInitHasOverdueLx(item, reverseBxQc);

            // region 根据条件查询账号
            //List<SthousingDetail> details = item.getDetails();
            //BigDecimal sum = BigDecimal.ZERO;
            //for ( int i = 0 ; i < details.size();i++) {
            //    sum = sum.add(details.get(i).getBjje());
            //}
            //if (sum .compareTo( item.getInitInformation().getCsye()) <= 0) {
            //    continue;
            //}
            //if (item.getInitInformation().getCsyqbj().compareTo(BigDecimal.ZERO) > 0) continue;
            // endregion


            checkMain.analyzeOneThousandDkzh(item, reverseBxQc);


//            checkMain.analyzeOneThousandDkzhFuturePayment(item);
//            BigDecimal dkyeByYw = checkMain.analyzeAllDkzh(item, reverseBxQc);


            // region 分析所有的贷款账号的余额与推算余额的差异  但是全部读入内存 , heap 不够啊
//            AllAccountDkye allAccountDkye = new AllAccountDkye();
//            allAccountDkye.setDkzh(item.getSthousingAccount().getDkzh());
//            allAccountDkye.setDkffrq(item.getSthousingAccount().getDkffrq());
//            allAccountDkye.setDkqs(item.getSthousingAccount().getDkqs());
//            allAccountDkye.setCsdkye(item.getInitInformation().getCsye());
//            allAccountDkye.setCsyqbj(item.getInitInformation().getCsyqbj());
//            allAccountDkye.setCsqs(item.getInitFirstQc().intValue());
//            allAccountDkye.setTsdkye(dkyeByYw);
//            allAccountDkye.setSjdkye(item.getSthousingAccount().getDkye());
//            allAccountDkye.setSubdkye(dkyeByYw.subtract(item.getSthousingAccount().getDkye()));
//            datasetAllAccountDkye.add(allAccountDkye);

            // endregion
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
     * 分析即将发生扣款的时间等
     *
     * @param informations
     */
    public void analyzeOneThousandDkzhFuturePayment(AccountInformations informations) {

        SthousingAccount sthousingAccount = informations.getSthousingAccount();
        Date dkxffrq = sthousingAccount.getDkxffrq();
        BigDecimal xdqqc = sthousingAccount.getDqqc();
        Date date = CommLoanAlgorithm.periodOfafterTime(dkxffrq, xdqqc.intValue());
        LocalDate thisDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<StOverdue> stOverdues = accountCheck.listOverdueByDkzh(sthousingAccount.getDkzh());
        StOverdue firstOver = null;
        for (StOverdue overdue : stOverdues) {
            if (!"已入账".equals(overdue.getYwzt())) {
                firstOver = overdue;
                break;
            }
        }

        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());

        // 计算的下一次扣款日期 , 在 该月份以内
        if (thisDate.compareTo(start) >= 0 && thisDate.compareTo(end) <= 0) {
            String s = "扣款日期:  %-10s";
            String format = String.format(s, thisDate);
            logs.append(format);
        }

        if (firstOver != null) {
            String s = "  %s";
            String format = String.format(s, firstOver);
            logs.append(format);
        }

        logs.append("\n");


    }


    /**
     * 分析所有的贷款账号, 根据初始余额和已入账的业务进行推算余额是否正常
     *
     * @param informations
     * @param reverseQc
     * @return 推算贷款余额
     */
    public BigDecimal analyzeAllDkzh(AccountInformations informations, List<Integer> reverseQc) {
        // 业务记录
        List<SthousingDetail> details = informations.getDetails();
        BigDecimal csye = informations.getInitInformation().getCsye();
        BigDecimal dkyeByCsye = csye;
        BigDecimal dkye = informations.getSthousingAccount().getDkye();
        BigDecimal sub = BigDecimal.ZERO;
        for (int i = 0; i < details.size(); i++) {
            SthousingDetail detail = details.get(i);
            logs.append(detail);
            String bxdz = "";
            if (reverseQc.contains(detail.getDqqc().intValue())) {
                sub = detail.getLxje();
                bxdz = "  该期本息倒置";
            } else {
                sub = detail.getBjje();
            }

            dkyeByCsye = dkyeByCsye.subtract(sub);
            logs.append("  推算期末余额: " + dkyeByCsye + "  贷款余额差额: " + dkyeByCsye.subtract(detail.getXqdkye()) + "\n");
        }
        BigDecimal subDkye = dkyeByCsye.subtract(dkye);
        logs.append("推算贷款余额: " + dkyeByCsye + "  实际贷款余额: " + dkye + "  推-实: " + subDkye + "\n");
        return dkyeByCsye;
    }

    /**
     * 1400多个问题账号分析 , 不根据业务, 推算正常应该还款的业务
     *
     * @param informations
     * @param reverseQc
     */
    public void analyzeOneThousandDkzh(AccountInformations informations, List<Integer> reverseQc) {
        // 业务记录
        List<SthousingDetail> details = informations.getDetails();
        BigDecimal csye = informations.getInitInformation().getCsye();
        BigDecimal dkyeByCsye = csye.add(BigDecimal.ZERO);
        // 根据业务推算的余额 , 减去初始逾期本金对我们系统的业务进行分析 , 计算
        BigDecimal dkyeByYeWu = csye.subtract(informations.getInitInformation().getCsyqbj());

        // 还款计划
        List<RepaymentItem> repaymentItems = informations.getRepaymentItems();
        // 提前还款的业务, 已排序
        List<SthousingDetail> prepaymentList = Common.listPrepayment(details);

        // 现在时间
        Date now = new Date();
//        Date now = null;

        // 推算应该发生的业务
        List<SthousingDetail> shouldDetails = new ArrayList<>();

        //导入系统的时候存在逾期记录
        if (informations.isInitHasOverdue()) {
            List<StOverdue> initOverdueList = informations.getInitOverdueList();
            for (StOverdue overdue : initOverdueList) {
                SthousingDetail detail = new SthousingDetail();
                detail.setBjje(overdue.getYqbj());
                detail.setLxje(overdue.getYqlx());
                detail.setFxje(overdue.getYqfx());
                detail.setDqqc(overdue.getYqqc());
                detail.setYwfsrq(overdue.getSsrq());
                detail.setDkywmxlx("初始导入");
                detail.setFse(overdue.getYqbj().add(overdue.getYqlx()));
                dkyeByCsye = dkyeByCsye.subtract(overdue.getYqbj());
                detail.setXqdkye(dkyeByCsye);
                shouldDetails.add(detail);
            }
        }

        if (informations.getInitInformation().getCsyqbj().compareTo(BigDecimal.ZERO) > 0 ) {
            if (dkyeByCsye.compareTo(dkyeByYeWu) == 0) {
                logs.append("初始逾期本金和初始导入系统业务本金和相等\n");
            } else {
                logs.append("初始逾期本金和初始导入系统业务本金和=========================\n");
            }

        }
        //analyOneThousand0(informations, repaymentItems, prepaymentList, 0, now, null, shouldDetails);

        analyOneThousand11(repaymentItems, shouldDetails, prepaymentList, now, 0);
        analyOneThousand12(shouldDetails, informations);

        BigDecimal subFse = BigDecimal.ZERO;
        BigDecimal subBj = BigDecimal.ZERO;
        BigDecimal subLx = BigDecimal.ZERO;
        BigDecimal subDkye = BigDecimal.ZERO;
        BigDecimal eachSubFse = BigDecimal.ZERO;
        BigDecimal eachSubBj;
        BigDecimal eachSubLx;
        BigDecimal eachSubDkye;
        BigDecimal shouldDkye = csye;


        int size = shouldDetails.size() > details.size() ? shouldDetails.size() : details.size();
        for (int i = 0; i < size; i++) {
            SthousingDetail shouldDetail = null;
            if (i < shouldDetails.size()) {
                shouldDetail = shouldDetails.get(i);
            }
            SthousingDetail detail = null;
            String log1 = "%-4s  日期: %-12s  期次: %-3s  发生额: %-10s  本金: %-10s  利息: %-10s  期末余额: %-20s";
            String log2 = "%-4s  业务日期: %-12s  期次: %-3s  发生额(去罚): %-10s  本金: %-10s  利息: %-10s  期末余额: %-20s  发生额差(前-后): %-10s  本金差: %-10s  利息差: %-10s  期末余额差: %-10s";
            if (i < details.size()) {
                detail = details.get(i);
            }
            OneThousand oneThousand = new OneThousand();
            BigDecimal detailFse = BigDecimal.ZERO;
            if (shouldDetail == null && detail != null) {
                String formatLog = String.format(log1, NO_MESS_CHINESE, NO_MESS, NO_MESS, NO_MESS, NO_MESS, NO_MESS, NO_MESS);
                logs.append(formatLog);
                detailFse = detail.getBjje().add(detail.getLxje());
                formatLog = String.format(log2, "06".equals(detail.getDkywmxlx()) ? Common.FULL_SPACE + "结清" + Common.FULL_SPACE : LoanBusinessType.getNameByCode(detail.getDkywmxlx()), Utils.SDF_YEAR_MONTH_DAY.format(detail.getYwfsrq()), detail.getDqqc(),
                        detailFse, detail.getBjje(), detail.getLxje(), detail.getXqdkye(),
                        NO_MESS, NO_MESS, NO_MESS, NO_MESS);
                logs.append(formatLog);


                oneThousand.setSjhklx(LoanBusinessType.getNameByCode(detail.getDkywmxlx()));
                oneThousand.setSjrq(detail.getYwfsrq());
                oneThousand.setSjqc(detail.getDqqc().intValue());
                oneThousand.setSjfse(detailFse);
                oneThousand.setSjbj(detail.getBjje());
                oneThousand.setSjlx(detail.getLxje());
                oneThousand.setSjqmdkye(detail.getXqdkye());
                if (reverseQc.contains(detail.getDqqc().intValue())) {
                    logs.append("    该期本息倒置");
                    oneThousand.setBz("该期本息倒置");
                }
            } else if (shouldDetail != null && detail == null) {
                String formatLog = String.format(log1, shouldDetail.getDkywmxlx(), shouldDetail.getYwfsrq() == null ? "------" : Utils.SDF_YEAR_MONTH_DAY.format(shouldDetail.getYwfsrq()),
                        shouldDetail.getDqqc(), shouldDetail.getFse(), shouldDetail.getBjje(), shouldDetail.getLxje(), shouldDetail.getXqdkye());
                logs.append(formatLog);
                formatLog = String.format(log2, NO_MESS_CHINESE, NO_MESS, NO_MESS,
                        NO_MESS, NO_MESS, NO_MESS, NO_MESS,
                        NO_MESS, NO_MESS, NO_MESS, NO_MESS);
                logs.append(formatLog);
                oneThousand.setHklx(shouldDetail.getDkywmxlx());
                oneThousand.setRq(shouldDetail.getYwfsrq());
                oneThousand.setQc(shouldDetail.getDqqc().intValue());
                oneThousand.setFse(shouldDetail.getFse());
                oneThousand.setBj(shouldDetail.getBjje());
                oneThousand.setLx(shouldDetail.getLxje());
                oneThousand.setQmdkye(shouldDetail.getXqdkye());

            } else if (shouldDetail != null && detail != null) {
                detailFse = detail.getBjje().add(detail.getLxje());
                String formatLog = String.format(log1, shouldDetail.getDkywmxlx(), shouldDetail.getYwfsrq() == null ? "------" : Utils.SDF_YEAR_MONTH_DAY.format(shouldDetail.getYwfsrq()),
                        shouldDetail.getDqqc(), shouldDetail.getFse(), shouldDetail.getBjje(), shouldDetail.getLxje(), shouldDetail.getXqdkye());
                logs.append(formatLog);

                eachSubBj = shouldDetail.getBjje().subtract(detail.getBjje());
                eachSubLx = shouldDetail.getLxje().subtract(detail.getLxje());
                eachSubDkye = shouldDetail.getXqdkye().subtract(detail.getXqdkye());

                if (shouldDetail.getDqqc().compareTo(informations.getInitFirstQc()) >= 0) {
                    eachSubFse = shouldDetail.getFse().subtract(detailFse);
                    subFse = subFse.add(eachSubFse);
                }

                subBj = subBj.add(eachSubBj);
                subLx = subLx.add(eachSubLx);
                subDkye = subDkye.add(eachSubDkye);
                shouldDkye = shouldDetail.getXqdkye();
                formatLog = String.format(log2, "06".equals(detail.getDkywmxlx()) ? Common.FULL_SPACE + "结清" + Common.FULL_SPACE : LoanBusinessType.getNameByCode(detail.getDkywmxlx()), Utils.SDF_YEAR_MONTH_DAY.format(detail.getYwfsrq()), detail.getDqqc(),
                        detailFse, detail.getBjje(), detail.getLxje(), detail.getXqdkye(),
                        eachSubFse, eachSubBj, eachSubLx, eachSubDkye);
                logs.append(formatLog);

                oneThousand.setHklx(shouldDetail.getDkywmxlx());
                oneThousand.setRq(shouldDetail.getYwfsrq());
                oneThousand.setQc(shouldDetail.getDqqc().intValue());
                oneThousand.setFse(shouldDetail.getFse());
                oneThousand.setBj(shouldDetail.getBjje());
                oneThousand.setLx(shouldDetail.getLxje());
                oneThousand.setQmdkye(shouldDetail.getXqdkye());
                oneThousand.setSjhklx(LoanBusinessType.getNameByCode(detail.getDkywmxlx()));
                oneThousand.setSjrq(detail.getYwfsrq());
                oneThousand.setSjqc(detail.getDqqc().intValue());
                oneThousand.setSjfse(detailFse);
                oneThousand.setSjbj(detail.getBjje());
                oneThousand.setSjlx(detail.getLxje());
                oneThousand.setSjqmdkye(detail.getXqdkye());
                oneThousand.setSubfse(eachSubFse);
                oneThousand.setSubbj(eachSubBj);
                oneThousand.setSublx(eachSubLx);
                oneThousand.setSubqmye(eachSubDkye);
                if (reverseQc.contains(detail.getDqqc().intValue())) {
                    logs.append("    该期本息倒置");
                    oneThousand.setBz("该期本息倒置");
                }
            }
            datasetOneThousand.add(oneThousand);

            logs.append("\n");


        }
        String log = "发生额差 : %s, 本金差额: %s , 利息差额: %s , 贷款余额差额: %s  推算应该贷款余额:  %s  实际贷款余额: %s  推-实际: %s \n";
        String format = String.format(log, subFse, subBj, subLx, subDkye, shouldDkye, informations.getSthousingAccount().getDkye(), shouldDkye.subtract(informations.getSthousingAccount().getDkye()));
        OneThousand oneThousand1 = new OneThousand();
        oneThousand1.setSubFseTotal(subFse);
        oneThousand1.setSubBjTotal(subBj);
        oneThousand1.setSubLxTotal(subLx);
        oneThousand1.setSubDkyeTotal(subDkye);
        oneThousand1.setTsdkye(shouldDkye);
        oneThousand1.setSjdkye(informations.getSthousingAccount().getDkye());
        oneThousand1.setSubDkye(shouldDkye.subtract(informations.getSthousingAccount().getDkye()));
        dataset.add(oneThousand1);
        logs.append(format);
    }

    /**
     * 主要生成shouldDetails , 只是为了把提前还款和结清的数据穿插入其中 ,数据不正确 , 后期在处理shouldDetails
     *
     * @param repaymentItems
     * @param shouldDetails
     * @param prepaymentList
     * @param now
     * @param prepaymentTag
     */
    private void analyOneThousand11(List<RepaymentItem> repaymentItems, List<SthousingDetail> shouldDetails,
                                    List<SthousingDetail> prepaymentList, Date now, int prepaymentTag) {
        SthousingDetail prepaymentDetail;
        for (RepaymentItem repaymentItem : repaymentItems) {
            // 截止日期
            if (now != null && Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()).compareTo(Utils.SDF_YEAR_MONTH_DAY.format(now)) > 0) {
                break;
            }

            prepaymentDetail = null;
            if (prepaymentTag < prepaymentList.size()) {
                prepaymentDetail = prepaymentList.get(prepaymentTag);
            }
            // 若 不存在提前还款(or结清)业务 或者 该期还款日期在提前还款(or结清)业务发生日期之前, 那么该期计划应该存在
            if (prepaymentDetail == null ||
                    Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()).compareTo(Utils.SDF_YEAR_MONTH_DAY.format(prepaymentDetail.getYwfsrq())) < 0) {
                SthousingDetail detail = new SthousingDetail();
                detail.setDkywmxlx("正常还款");
                detail.setYwfsrq(repaymentItem.getHkrq());
                detail.setDqqc(new BigDecimal(repaymentItem.getHkqc()));
                detail.setFse(repaymentItem.getFse());
                detail.setBjje(repaymentItem.getHkbjje());
                detail.setLxje(repaymentItem.getHklxje());
                detail.setXqdkye(repaymentItem.getQmdkye());
                shouldDetails.add(detail);
            } else {
                boolean isJieQing = false;
                SthousingDetail detail = new SthousingDetail();
                if (LoanBusinessType.提前还款.getCode().equals(prepaymentDetail.getDkywmxlx())) {
                    detail.setDkywmxlx("提前还款");
                }
                if (LoanBusinessType.结清.getCode().equals(prepaymentDetail.getDkywmxlx())) {
                    detail.setDkywmxlx(Common.FULL_SPACE + "结清" + Common.FULL_SPACE);
                    isJieQing = true;
                }

                detail.setYwfsrq(prepaymentDetail.getYwfsrq());
                detail.setDqqc(new BigDecimal(repaymentItem.getHkqc()));
                // 主要获取发生额 , 其余值没有意义
                detail.setFse(prepaymentDetail.getFse());
                detail.setBjje(BigDecimal.ZERO);
                detail.setLxje(BigDecimal.ZERO);
                detail.setXqdkye(BigDecimal.ZERO);
                shouldDetails.add(detail);
                if (isJieQing) {
                    break;
                }
                prepaymentTag++;
            }
        }
    }


    /**
     * 根据生成的站位业务, 后期处理
     * @param shouldDetails 生成的站位业务
     * @param informations
     */
    private void analyOneThousand12(List<SthousingDetail> shouldDetails, AccountInformations informations) {
        SthousingDetail thisDetail;
        SthousingDetail preDetail = null;
        SthousingDetail nextDetail = null;
        List<RepaymentItem> repaymentItems = null;
        int repaymentTag = -1 ;
        for (int i = 0; i < shouldDetails.size(); i++) {
            thisDetail = shouldDetails.get(i);
            if (i > 0) {
                preDetail = shouldDetails.get(i - 1);
            }
            if (i < shouldDetails.size() - 1) {
                nextDetail = shouldDetails.get(i + 1);
            }

            // 代表重新生成了还款计划
            if (repaymentItems != null && repaymentTag > -1) {



                RepaymentItem repaymentItem = repaymentItems.get(repaymentTag);
                thisDetail.setFse(repaymentItem.getFse());
                thisDetail.setLxje(repaymentItem.getHklxje());
                thisDetail.setBjje(repaymentItem.getHkbjje());
                thisDetail.setXqdkye(repaymentItem.getQmdkye());

                // 重新生成还款计划之后第一期
                if (repaymentTag == 0) {
                    int jxts = LoanRepaymentAlgorithm.betweenTwoDateDays(preDetail.getYwfsrq(), thisDetail.getYwfsrq()) - 30;
                    BigDecimal lx = LoanRepaymentAlgorithm.calInterestByInterestDays(preDetail.getXqdkye(), informations.getSthousingAccount().getDkll(), jxts);
                    thisDetail.setFse(thisDetail.getFse().add(lx));
                    thisDetail.setLxje(thisDetail.getLxje().add(lx));
                }
                repaymentTag++;
            }

            if (thisDetail.getDkywmxlx().contains("结清")) {
                BigDecimal qcdkye = preDetail == null ?
                        informations.getInitInformation().getCsye().subtract(informations.getInitInformation().getCsyqbj()) :
                        preDetail.getXqdkye();
                int jxts = LoanRepaymentAlgorithm.betweenTwoDateDays(preDetail.getYwfsrq(), thisDetail.getYwfsrq());
                BigDecimal lx = LoanRepaymentAlgorithm.calInterestByInterestDays(qcdkye, informations.getSthousingAccount().getDkll(), jxts);
                BigDecimal bjje = qcdkye;
                BigDecimal qmdkye = qcdkye.subtract(bjje);
                thisDetail.setFse(bjje.add(lx));
                thisDetail.setLxje(lx);
                thisDetail.setBjje(bjje);
                thisDetail.setXqdkye(qmdkye);
            }

            // 在提前还款之前生成的是正确的
            if ("提前还款".equals(thisDetail.getDkywmxlx())) {
                BigDecimal qcdkye = preDetail == null ?
                        informations.getInitInformation().getCsye().subtract(informations.getInitInformation().getCsyqbj()) :
                        preDetail.getXqdkye();
                int jxts = LoanRepaymentAlgorithm.betweenTwoDateDays(preDetail.getYwfsrq(), thisDetail.getYwfsrq());
                BigDecimal lx = LoanRepaymentAlgorithm.calInterestByInterestDays(qcdkye, informations.getSthousingAccount().getDkll(), jxts);
                BigDecimal bjje = thisDetail.getFse().subtract(lx);
                BigDecimal qmdkye = qcdkye.subtract(bjje);

                thisDetail.setLxje(lx);
                thisDetail.setBjje(bjje);
                thisDetail.setXqdkye(qmdkye);


                String dkffrqStr = Utils.SDF_YEAR_MONTH_DAY.format(informations.getSthousingAccount().getDkffrq());
                String ywfsrqStr = Utils.SDF_YEAR_MONTH_DAY.format(thisDetail.getYwfsrq());
                String str1 = ywfsrqStr.substring(0, 8);
                String str2 = dkffrqStr.substring(8, 10);
                String dkxffrqStr = str1 + str2;
                Date dkxffrq = null;
                try {
                    dkxffrq = Utils.SDF_YEAR_MONTH_DAY.parse(dkxffrqStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (nextDetail != null) {
                    if ("正常还款".equals(nextDetail.getDkywmxlx())) {
                        // 生成还款计划
                        repaymentItems = RepaymentPlan.listRepaymentPlan(qmdkye,
                                dkxffrq,
                                informations.getSthousingAccount().getDkqs().intValue() - thisDetail.getDqqc().intValue(),
                                informations.getSthousingAccount().getDkll(),
                                RepaymentMethod.getRepaymentMethodByCode(informations.getSthousingAccount().getDkhkfs()),
                                thisDetail.getDqqc().intValue(),
                                RepaymentMonthRateScale.NO);
                        repaymentTag = 0;
                    }
                }

            }
        }
    }


    /**
     * 安装正常情况推算应该发生的业务(这种思路有点复杂, 并且逻辑混乱)
     *
     * @param informations 对于一个账号, 包含组织好的各种信息
     * @param repaymentItems 还款计划
     * @param prepaymentList 提前还款,根据业务发生日期排好序的
     * @param prepaymentTag 提前还款标记
     * @param now 截止的某一时间
     * @param preDetailYwfsrq 前一项业务的业务发生日期
     * @param shouldDetails 应该发生的业务的集合
     */
    private void analyOneThousand0(AccountInformations informations, List<RepaymentItem> repaymentItems,
                                   List<SthousingDetail> prepaymentList, int prepaymentTag, Date now, Date preDetailYwfsrq, List<SthousingDetail> shouldDetails) {
        SthousingDetail prepaymentDetail;
        for (int i = 0; i < repaymentItems.size(); i++) {
            RepaymentItem repaymentItem = repaymentItems.get(i);
            RepaymentItem pre = null;
            if (i > 0) {
                pre = repaymentItems.get(i - 1);
            }
            prepaymentDetail = null;
            if (prepaymentTag < prepaymentList.size()) {
                prepaymentDetail = prepaymentList.get(prepaymentTag);
            }
            if (now != null && Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()).compareTo(Utils.SDF_YEAR_MONTH_DAY.format(now)) > 0) {
                break;
            }
            //// 跳过的期次
            //if (repaymentItem.getHkqc() == 36 || repaymentItem.getHkqc() == 37) {
            //    repaymentItem.setQcdkye(pre.getQmdkye());
            //    repaymentItem.setQmdkye(pre.getQmdkye());
            //    continue;
            //}
            if (prepaymentDetail == null ||
                    Utils.SDF_YEAR_MONTH_DAY.format(repaymentItem.getHkrq()).compareTo(Utils.SDF_YEAR_MONTH_DAY.format(prepaymentDetail.getYwfsrq())) < 0) {
                // 前一期是提前还款  ,  后面一期的利息要多点
                if (preDetailYwfsrq != null) {
                    int lxts = LoanRepaymentAlgorithm.differentDaysByMillisecond(preDetailYwfsrq, repaymentItem.getHkrq()) - 30;
                    BigDecimal lx = LoanRepaymentAlgorithm.calInterestByInterestDays(repaymentItem.getQcdkye(),
                            informations.getSthousingAccount().getDkll(), lxts);
                    repaymentItem.setHklxje(repaymentItem.getHklxje().add(lx));
                    repaymentItem.setFse(repaymentItem.getFse().add(lx));
                    preDetailYwfsrq = null;
                }
                SthousingDetail detail = new SthousingDetail();
                detail.setDkywmxlx("正常还款");
                detail.setYwfsrq(repaymentItem.getHkrq());
                detail.setDqqc(new BigDecimal(repaymentItem.getHkqc()));
                detail.setFse(repaymentItem.getFse());
                detail.setBjje(repaymentItem.getHkbjje());
                detail.setLxje(repaymentItem.getHklxje());
                detail.setXqdkye(repaymentItem.getQmdkye());
                shouldDetails.add(detail);

            } else {
                BigDecimal qcdkye = pre == null ? repaymentItem.getQcdkye() : pre.getQmdkye();
                int lxts = LoanRepaymentAlgorithm.calInterestDays(informations.getSthousingAccount().getDkffrq(), prepaymentDetail.getYwfsrq());
                if (preDetailYwfsrq != null){
                    lxts = LoanRepaymentAlgorithm.differentDaysByMillisecond(preDetailYwfsrq, prepaymentDetail.getYwfsrq());
                }
                BigDecimal lx = LoanRepaymentAlgorithm.calInterestByInterestDays(qcdkye, informations.getSthousingAccount().getDkll(), lxts);
                BigDecimal bjje = prepaymentDetail.getFse().subtract(lx);
                BigDecimal qmdkye = qcdkye.subtract(bjje);
                //BigDecimal qmdkye = pre == null ? repaymentItem.getQcdkye().subtract(prepaymentDetail.getBjje()) : pre.getQmdkye().subtract(prepaymentDetail.getBjje());
                boolean isJieQing = false;
                SthousingDetail detail = new SthousingDetail();
                if (LoanBusinessType.提前还款.getCode().equals(prepaymentDetail.getDkywmxlx())) {
                    detail.setDkywmxlx("提前还款");
                }
                if (LoanBusinessType.结清.getCode().equals(prepaymentDetail.getDkywmxlx())) {
                    detail.setDkywmxlx(Common.FULL_SPACE + "结清" + Common.FULL_SPACE);
                    isJieQing = true;
                }

                detail.setYwfsrq(prepaymentDetail.getYwfsrq());
                detail.setDqqc(new BigDecimal(repaymentItem.getHkqc()));
                //detail.setFse(prepaymentDetail.getFse());
                //detail.setBjje(prepaymentDetail.getBjje());
                //detail.setLxje(prepaymentDetail.getLxje());
                detail.setFse(prepaymentDetail.getFse());
                detail.setBjje(bjje);
                detail.setLxje(lx);
                detail.setXqdkye(qmdkye);
                shouldDetails.add(detail);
                if (isJieQing) {
                    break;
                }
                prepaymentTag++;
                repaymentItems = RepaymentPlan.listRepaymentPlan(qmdkye,
                        repaymentItem.getHkrq(),
                        informations.getSthousingAccount().getDkqs().intValue() - repaymentItem.getHkqc(),
                        informations.getSthousingAccount().getDkll(),
                        RepaymentMethod.getRepaymentMethodByCode(informations.getSthousingAccount().getDkhkfs()),
                        repaymentItem.getHkqc(),
                        RepaymentMonthRateScale.NO);
                analyOneThousand0(informations, repaymentItems, prepaymentList, prepaymentTag, now, prepaymentDetail.getYwfsrq(), shouldDetails);
                break;
            }
        }
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
