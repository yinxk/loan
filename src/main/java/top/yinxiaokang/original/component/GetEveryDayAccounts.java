package top.yinxiaokang.original.component;


import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.entity.SomedayInformation;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.BeanOrMapUtil;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.DateUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class GetEveryDayAccounts {

    private AccountCheck accountCheck = new AccountCheck();

    private String baseAccountDkzhs;
    private List<SthousingAccount> doneAccounts;

    private List<SomedayInformation> lastMonth;
    private List<SomedayInformation> yesterday;
    private List<SomedayInformation> today;

    public GetEveryDayAccounts() {
        StringBuilder sb = new StringBuilder(600);
        List<InitInformation> initInformations = Common.listBaseAccountInformationByExcelUtil();
        boolean isFirst = true;
        for (InitInformation initInformation : initInformations) {
            isFirst = Common.appendDkzhToSqlCanRead(sb, isFirst, initInformation.getDkzh(), false);
        }
        baseAccountDkzhs = sb.toString();
        doneAccounts = new ArrayList<>();
        List<Map<String, String>> maps = top.yinxiaokang.util.ExcelUtil.readStringExcel(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, 0, false, false);
        for (Map<String, String> map : maps) {
            SthousingAccount account = new SthousingAccount();
            account.setDkzh(map.get("贷款账号"));
            doneAccounts.add(account);
        }
        log.info("获取到的已处理的贷款账号数据 : {}", doneAccounts.size());
    }

    private SthousingAccount getDoneAccount(String dkzh) {
        for (SthousingAccount doneAccount : doneAccounts) {
            if (dkzh.equals(doneAccount.getDkzh())) {
                return doneAccount;
            }
        }
        return null;
    }


    private void listSomedayInformationToday() {
        LocalDate localDate = LocalDate.now();
        if (today != null) return;
        today = accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }

    private void listSomedayInformationYesterday() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.plusDays(-1);
        if (yesterday != null) return;
        yesterday = accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }

    private void fileterSomedayInformationList() {
        filterDoneAccounts(lastMonth, "lastMonth");
        filterDoneAccounts(yesterday, "yesterday");
        filterDoneAccounts(today, "today");
    }

    private void filterDoneAccounts(List<SomedayInformation> somedayInformations,String msgType) {
        Iterator<SomedayInformation> iterator = somedayInformations.iterator();
        while (iterator.hasNext()) {
            SomedayInformation next = iterator.next();
            SthousingAccount doneAccount = getDoneAccount(next.getDkzh());
            if (doneAccount != null) {
                iterator.remove();
                log.info("移除 {} 匹配到的已处理贷款账号: {} ", msgType, doneAccount.getDkzh());
            }
        }
    }

    private void listSomedayInformationLastMonth() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        if (lastMonth != null) return;
        lastMonth = accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }

    private Map<String, SomedayInformation> distinctTodaySomdayInformationList() {
        listPrepare();
        Map<String, SomedayInformation> map = new HashMap<>();

        for (SomedayInformation information : today) {
            map.put(information.getDkzh(), information);
        }
        for (SomedayInformation information : yesterday) {
            map.put(information.getDkzh(), information);
        }
        for (SomedayInformation information : lastMonth) {
            map.put(information.getDkzh(), information);
        }
        return map;
    }

    private void listPrepare() {
        listSomedayInformationLastMonth();
        listSomedayInformationYesterday();
        listSomedayInformationToday();
        fileterSomedayInformationList();
    }


    private Map<String, SomedayInformation> distinctYesterdaySomdayInformationList() {
        listPrepare();
        Map<String, SomedayInformation> map = new HashMap<>();
        for (SomedayInformation information : yesterday) {
            map.put(information.getDkzh(), information);
        }
        for (SomedayInformation information : lastMonth) {
            map.put(information.getDkzh(), information);
        }
        return map;
    }

    public List<SomedayInformation> listTodayAllAccounts() {
        Map<String, SomedayInformation> stringSomedayInformationMap = distinctTodaySomdayInformationList();
        return sortByNextKkrq(stringSomedayInformationMap);
    }

    private List<SomedayInformation> sortByNextKkrq(Map<String, SomedayInformation> stringSomedayInformationMap) {
        List<SomedayInformation> list = new ArrayList<>();
        for (Map.Entry<String, SomedayInformation> stringSomedayInformationEntry : stringSomedayInformationMap.entrySet()) {
            list.add(stringSomedayInformationEntry.getValue());
        }
        Collections.sort(list, (obj1, obj2) ->
                obj2.getNextkkrq().compareTo(obj1.getNextkkrq())
        );
        return list;
    }

    public List<SomedayInformation> listYesterdayAllAccounts() {
        Map<String, SomedayInformation> stringSomedayInformationMap = distinctYesterdaySomdayInformationList();
        return sortByNextKkrq(stringSomedayInformationMap);
    }


    private void toLogTodayDkzh(List<SomedayInformation> list) {
        log.info("生成今日贷款账号拼接成的执行SQL需要的字符串");
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (SomedayInformation information : list) {
            isFirst = Common.appendDkzhToSqlCanRead(sb, isFirst, information.getDkzh(), true);
        }
        byte[] bytes = sb.toString().getBytes();

        try (OutputStream outputStream = new FileOutputStream(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_LOG)) {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void toExcelTodayDkzh(List<SomedayInformation> list) {
        log.info("生成今日仅贷款账号的文件");

        List<Map> transform = new ArrayList<>();
        for (SomedayInformation information : list) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(information);
            transform.add(stringObjectMap);
        }

        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "dkzh");
        try (OutputStream outputStream = new FileOutputStream(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_XLS)) {
            ExcelUtil.exportExcel(keyMap, transform, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void toExceYesterdayDkzh(List<SomedayInformation> list) {
        log.info("生成昨日扣款未入账的贷款账号文件");
        List<Map> transform = new ArrayList<>();
        for (SomedayInformation information : list) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(information);
            transform.add(stringObjectMap);
        }
        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "dkzh");
        try (OutputStream outputStream = new FileOutputStream(Constants.YESTERDAY_SHOULD_PAYMENT_ACCOUNT_FAIL)) {
            ExcelUtil.exportExcel(keyMap, transform, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void toExcelTodayShouldPaymentAccounts(List<SomedayInformation> list) {
        list = new ArrayList<>(list);
        log.info("生成今日应该扣款账号相关信息文件");
        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "贷款账号");
        keyMap.put("dkffrq", "贷款发放日期");
        keyMap.put("qc", "期次");
        keyMap.put("nextkkrq", "扣款日期");
        keyMap.put("dkzhzt", "账户状态");
        keyMap.put("ffdaysfxd", "发放日和扣款日是否相等");
        LocalDate toDay = LocalDate.now();
        LocalDate threeDayAgo = toDay.minusDays(4);
        LocalDate tomorrow = toDay.plusDays(1);
        for (int i = 0; i < list.size(); i++) {
            SomedayInformation information = list.get(i);
            SomedayInformation preInformation = null;
            if (i > 0) {
                preInformation = list.get(i - 1);
            }
            LocalDate theNextKKrq = LocalDate.parse(information.getNextkkrq());
            boolean before = theNextKKrq.isAfter(threeDayAgo);
            boolean after = theNextKKrq.isBefore(tomorrow);

            if (theNextKKrq.isAfter(threeDayAgo) && theNextKKrq.isBefore(tomorrow)) {

                if (theNextKKrq.isBefore(toDay)) {
                    if (preInformation != null) {
                        LocalDate preNextKkrq = LocalDate.parse(preInformation.getNextkkrq());
                        if (!preNextKkrq.isEqual(theNextKKrq)) {
                            SomedayInformation somedayInformation = new SomedayInformation();
                            somedayInformation.setDkzh(information.getNextkkrq() + "扣款未入账");
                            list.add(i, somedayInformation);
                            i++;
                        }
                    }
                }

            } else {
                SomedayInformation somedayInformation = new SomedayInformation();
                somedayInformation.setDkzh(information.getNextkkrq() + "及之前需要手动处理");
                list.add(i, somedayInformation);
                break;
            }
        }
        try (OutputStream outputStream = new FileOutputStream(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_VIEW_XLS)) {
            ExcelUtil.exportExcel(keyMap, list, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void work() {
        List<SomedayInformation> todayAllAccounts = listTodayAllAccounts();
        toExcelTodayDkzh(todayAllAccounts);
        toLogTodayDkzh(todayAllAccounts);
        toExcelTodayShouldPaymentAccounts(todayAllAccounts);
        List<SomedayInformation> yesterdayAllAccounts = listYesterdayAllAccounts();
        toExceYesterdayDkzh(yesterdayAllAccounts);
        log.info("结束运行");
    }


    public static void main(String[] args) {
        GetEveryDayAccounts getEveryDayAccounts = new GetEveryDayAccounts();
        getEveryDayAccounts.work();
        log.info("test");
    }

}
