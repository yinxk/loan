package top.yinxiaokang.original.component;


import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.entity.SomedayInformation;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.DateUtil;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class GetEveryDayAccounts {

    AccountCheck accountCheck = new AccountCheck();

    private String baseAccountDkzhs;

    public GetEveryDayAccounts() {
        StringBuilder sb = new StringBuilder(600);
        List<InitInformation> initInformations = Common.listBaseAccountInformation();
        boolean isFirst = true;
        for (InitInformation initInformation : initInformations) {
            if (isFirst) {
                sb.append(initInformation.getDkzh());
                isFirst = false;
                continue;
            }
            sb.append(",");
            sb.append(initInformation.getDkzh());
        }
        baseAccountDkzhs = sb.toString();
    }

    private List<SomedayInformation> listSomedayInformationToday() {
        LocalDate localDate = LocalDate.now();
        return accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }

    private List<SomedayInformation> listSomedayInformationYesterday() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.plusDays(-1);
        return accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }

    private List<SomedayInformation> listSomedayInformationLastMonth() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }


    public static void main(String[] args) {
        log.info("test");
    }

}
