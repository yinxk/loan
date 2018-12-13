package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.yinxiaokang.original.dto.AccountInformations;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.BeanOrMapUtil;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@SuppressWarnings("WeakerAccess")
public class AfterAdjustment {

    private List<Map<String, String>> doneDkzhList;
    private List<AccountInformations> accountInformationList = new ArrayList<>();
    private List<Map<String, Object>> toExcelContent = new ArrayList<>();
    private Map<String, String> toExcelTitle = new LinkedHashMap<>();
    private List<String> filterDkzh = new ArrayList<>();

    public AfterAdjustment() {
        AccountCheck accountCheck = new AccountCheck();
        List<InitInformation> initInformationList = Common.listBaseAccountInformationByExcelUtil();
        doneDkzhList = top.yinxiaokang.util.ExcelUtil.readStringExcel(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, 0, false, false);

        for (InitInformation initInformation : initInformationList) {
            AccountInformations accountInformations = accountCheck.toAccountInformations(initInformation);
            if (accountInformations == null) {
                continue;
            }
            accountInformationList.add(accountInformations);
        }
        toExcelTitle.put("dkzh", "贷款账号");
        toExcelTitle.put("dkywmxlx", "业务类型");
        toExcelTitle.put("dqqc", "期次");
        toExcelTitle.put("ywfsrq", "业务发生日期");
        toExcelTitle.put("fse", "发生额");
        toExcelTitle.put("bjje", "本金金额");
        toExcelTitle.put("lxje", "利息金额");
        toExcelTitle.put("fxje", "罚息金额");
        toExcelTitle.put("xqdkye", "下期贷款余额");

        toExcelTitle.put("isTz", "是否调账");
        toExcelTitle.put("tsQmye", "推算期末余额");
        toExcelTitle.put("ce", "期末贷款-推算期末余额");
        toExcelTitle.put("nextRow1", "新加行");
    }

    AccountInformations getAccountInformationByDkzh(String dkzh) {
        for (AccountInformations information : accountInformationList) {
            SthousingAccount sthousingAccount = information.getSthousingAccount();
            if (sthousingAccount.getDkzh().equals(dkzh)) {
                return information;
            }
        }
        return null;
    }


    private void doWork() {
        List<Map<String, Object>> goToSort = new ArrayList<>();
        for (Map<String, String> doneAccount : doneDkzhList) {
            AccountInformations information = getAccountInformationByDkzh(doneAccount.get("贷款账号"));
            if (information == null) {
                throw new RuntimeException("不存在贷款账号: " + doneAccount);
            }
            List<SthousingDetail> details = information.getDetails();
            BigDecimal bjjeSum = details.stream().map(SthousingDetail::getBjje).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal abs = information.getInitInformation().getCsye().subtract(bjjeSum).subtract(information.getSthousingAccount().getDkye()).abs();
            Map<String, Object> map = new HashMap<>();
            map.put("dkzh", information.getSthousingAccount().getDkzh());
            map.put("abs", abs);
            map.put("file", doneAccount.get("账号对应文件"));
            goToSort.add(map);
        }
        Collections.sort(goToSort, (o1, o2) -> ((BigDecimal) o2.get("abs")).compareTo((BigDecimal) o1.get("abs")));


        for (Map<String, Object> map : goToSort) {
            String dkzh = (String) map.get("dkzh");
            AccountInformations information = getAccountInformationByDkzh(dkzh);
            if (information == null) {
                throw new RuntimeException("不存在贷款账号: " + dkzh);
            }
            doWork(information, (String) map.get("file"));
        }

        toExcel();
    }

    private void doWork(AccountInformations information, String fileName) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SthousingDetail> details = information.getDetails();
        SthousingAccount account = information.getSthousingAccount();
        InitInformation initInformation = information.getInitInformation();

        BigDecimal csye = initInformation.getCsye();
        BigDecimal tsQmdkye = csye;
        BigDecimal fseHj = BigDecimal.ZERO;
        BigDecimal bjjeSum = BigDecimal.ZERO;
        for (SthousingDetail detail : details) {
            if (StringUtils.isNotBlank(detail.getRemark())) {
                if (detail.getDkywmxlx().equals("01")) {
                    bjjeSum = bjjeSum.subtract(detail.getBjje());
                    continue;
                }
            }
            bjjeSum = bjjeSum.add(detail.getBjje());
        }
        BigDecimal abs = csye.subtract(bjjeSum).subtract(account.getDkye()).abs();
        if (abs.compareTo(new BigDecimal("0.05")) <= 0) {
            log.info("贷款账号 {} 业务本金金额+贷款余额 = 初始余额", account.getDkzh());
            filterDkzh.add(account.getDkzh());
            return;
        }

        for (SthousingDetail detail : details) {
            fseHj = fseHj.add(detail.getFse());
            if (detail.getDkywmxlx().equals("01")) {
                tsQmdkye = tsQmdkye.add(detail.getBjje());
            } else {
                tsQmdkye = tsQmdkye.subtract(detail.getBjje());
            }

            Map<String, Object> detailMap = BeanOrMapUtil.transBean2Map(detail);
            detailMap.put("ywfsrq", simpleDateFormat.format((Date) detailMap.get("ywfsrq")));

            detailMap.put("isTz", StringUtils.isBlank(detail.getRemark()) ? "" : "调账业务");
            detailMap.put("tsQmye", tsQmdkye);
            detailMap.put("dkzh", account.getDkzh());
            detailMap.put("nextRow1", fileName);
            detailMap.put("ce", detail.getXqdkye().subtract(tsQmdkye));
            toExcelContent.add(detailMap);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("dkzh", "贷款账号结束行");
        map.put("ywfsrq", "合计");
        map.put("fse", fseHj);
        map.put("bjje", bjjeSum);
        map.put("lxje", "初始余额-本金合计:");
        map.put("fxje", csye.subtract(bjjeSum));
        map.put("xqdkye", "目前贷款余额:");
        map.put("isTz", account.getDkye());
        map.put("tsQmye", "目前贷款余额-除去本金余额:");
        map.put("nextRow1", account.getDkye().subtract(csye.subtract(bjjeSum)));
        toExcelContent.add(map);
        map = new HashMap<>();
        map.put("dkzh", "====================");
        toExcelContent.add(map);
    }

    private void toExcel() {
        log.info("过滤的贷款账号数量: {}  写入Excel中贷款账号数量: {} ", filterDkzh.size(), doneDkzhList.size() - filterDkzh.size());
        ExcelUtil.writeToExcelByAll(Constants.BASE_PATH + "/已处理分析" + Constants.XLS, "已处理账号分析", toExcelTitle, toExcelContent);
    }


    public static void main(String[] args) {
        AfterAdjustment afterAdjustment = new AfterAdjustment();
        afterAdjustment.doWork();

    }
}
