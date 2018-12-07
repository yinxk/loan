package top.yinxiaokang.original.component;

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
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class AfterAdjustment {

    private List<String> doneDkzhList;
    private List<AccountInformations> accountInformationList = new ArrayList<>();
    private List<Map<String, Object>> toExcelContent = new ArrayList<>();
    private Map<String, String> toExcelTitle = new LinkedHashMap<>();

    public AfterAdjustment() {
        AccountCheck accountCheck = new AccountCheck();
        List<InitInformation> initInformationList = Common.listBaseAccountInformationByExcelUtil();
        doneDkzhList = new ArrayList<>();
        List<Map<String, String>> maps = top.yinxiaokang.util.ExcelUtil.readStringExcel(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, 0, false, false);
        for (Map<String, String> map : maps) {
            doneDkzhList.add(map.get("贷款账号"));
        }

        for (InitInformation initInformation : initInformationList) {
            AccountInformations accountInformations = accountCheck.toAccountInformations(initInformation);
            if (accountInformations == null) {
                continue;
            }
            accountInformationList.add(accountInformations);
        }
        toExcelTitle.put("dkzh", "贷款账号");
        toExcelTitle.put("ywlx", "业务类型");
        toExcelTitle.put("ywfsrq", "业务发生日期");
        toExcelTitle.put("fse", "发生额");
        toExcelTitle.put("bjje", "本金金额");
        toExcelTitle.put("lxje", "利息金额");
        toExcelTitle.put("fxje", "罚息金额");
        toExcelTitle.put("xqdkye", "下期贷款余额");

        toExcelTitle.put("isTz", "是否调账");
        toExcelTitle.put("tsQmye", "推算期末余额");
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
        for (String dkzh : doneDkzhList) {
            AccountInformations information = getAccountInformationByDkzh(dkzh);
            if (information == null) {
                throw new RuntimeException("不存在贷款账号: " + dkzh);
            }
            doWork(information);
        }
        toExcel();
    }

    private void doWork(AccountInformations information) {
        List<SthousingDetail> details = information.getDetails();
        InitInformation initInformation = information.getInitInformation();
        BigDecimal csye = initInformation.getCsye();
        BigDecimal tsQmdkye = csye;
        BigDecimal fseHj = BigDecimal.ZERO;
        BigDecimal bjjeHj = BigDecimal.ZERO;
        for (SthousingDetail detail : details) {
            fseHj = fseHj.add(detail.getFse());
            bjjeHj = bjjeHj.add(detail.getBjje());
            tsQmdkye = tsQmdkye.subtract(detail.getBjje());
            Map<String, Object> detailMap = BeanOrMapUtil.transBean2Map(detail);

            detailMap.put("isTz", StringUtils.isBlank(detail.getRemark()) ? "" : "调账业务");
            detailMap.put("tsQmye", tsQmdkye);
            toExcelContent.add(detailMap);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("dkzh", "贷款账号结束行");
        map.put("ywfsrq", "合计");
        map.put("fse", fseHj);
        map.put("bjje", bjjeHj);
        map.put("lxje", "初始余额-本金合计:");
        map.put("fxje", csye.subtract(bjjeHj));
        map.put("xqdkye", "目前贷款余额:");
        map.put("isTz", "目前贷款余额-除去本金余额:");
        map.put("tsQmye", information.getSthousingAccount().getDkye().subtract(csye.subtract(bjjeHj)));
        toExcelContent.add(map);

    }

    private void toExcel() {
        ExcelUtil.writeToExcelByAll(Constants.BASE_PATH + "/已处理分析" + Constants.XLSX, "已处理账号分析", toExcelTitle, toExcelContent);
    }


    public static void main(String[] args) {
        AfterAdjustment afterAdjustment = new AfterAdjustment();
        afterAdjustment.doWork();

    }
}
