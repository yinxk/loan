package top.yinxiaokang.original.component;


import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.entity.SomedayInformation;
import top.yinxiaokang.original.entity.StOverdue;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@SuppressWarnings({"SpellCheckingInspection", "unused", "unchecked", "Duplicates"})
public class GetEveryDayAccounts {

    private AccountCheck accountCheck = new AccountCheck();

    private String baseAccountDkzhs;
    private List<SthousingAccount> doneAccounts;

    private List<SomedayInformation> lastMonth;
    private List<SomedayInformation> yesterday;
    private List<SomedayInformation> today;
    // 查询逾期的没有扣到当月应该扣款的期次的账号, 这些账号不能写入-oneday文件, 应该写入昨日扣款未入账和今日扣款应该扣账号
    private List<SomedayInformation> overdues;

    private StringBuilder sb = new StringBuilder();

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
        listPrepare();
    }

    private SthousingAccount getDoneAccount(String dkzh) {
        for (SthousingAccount doneAccount : doneAccounts) {
            if (dkzh.equals(doneAccount.getDkzh())) {
                return doneAccount;
            }
        }
        return null;
    }

    // region 准备数据
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

    private void listSomedayInformationLastMonth() {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        if (lastMonth != null) return;
        lastMonth = accountCheck.listSomedayInformation(localDate.getDayOfMonth(), DateUtil.localDate2Date(localDate), baseAccountDkzhs);
    }

    private void listSomedayInformationOverdueAccounts() {
        List<StOverdue> stOverdues = accountCheck.listOverdueDkzhsInTheDkzhsStr(baseAccountDkzhs);
        String overdueDkzhsStr;
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (StOverdue overdueDkzh : stOverdues) {
            isFirst = Common.appendDkzhToSqlCanRead(sb, isFirst, overdueDkzh.getDkzh(), false);
        }
        overdueDkzhsStr = sb.toString();
        overdues = accountCheck.listSomedayInformationByOverdueDkzh(overdueDkzhsStr);
    }

    private void fileterSomedayInformationList() {
        filterDoneAccounts(lastMonth, "lastMonth");
        filterDoneAccounts(yesterday, "yesterday");
        filterDoneAccounts(today, "today");
        filterDoneAccounts(overdues, "overdues");
    }

    private void filterDoneAccounts(List<SomedayInformation> somedayInformations, String msgType) {
        Iterator<SomedayInformation> iterator = somedayInformations.iterator();
        while (iterator.hasNext()) {
            SomedayInformation next = iterator.next();
            SthousingAccount doneAccount = getDoneAccount(next.getDkzh());
            if (doneAccount != null) {
                iterator.remove();
                log.info("移除 {} 匹配到的已处理贷款账号: {} ", msgType, doneAccount.getDkzh());
                String theLog = "移除 %s 匹配到的已处理贷款账号: %s \n";
                sb.append(String.format(theLog, msgType, doneAccount.getDkzh()));
            }
        }
    }

    private void listPrepare() {
        listSomedayInformationLastMonth();
        listSomedayInformationYesterday();
        listSomedayInformationToday();
        listSomedayInformationOverdueAccounts();
        fileterSomedayInformationList();
    }

    // endregion
    private Map<String, SomedayInformation> distinctTodaySomdayInformationList() {
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

    private Map<String, SomedayInformation> distinctYesterdaySomdayInformationList() {
        Map<String, SomedayInformation> map = new HashMap<>();
        for (SomedayInformation information : yesterday) {
            map.put(information.getDkzh(), information);
        }
        for (SomedayInformation information : lastMonth) {
            map.put(information.getDkzh(), information);
        }
        return map;
    }

    /**
     * 今日需要正常扣款的所有贷款账号
     */
    private List<SomedayInformation> listTodayAllAccounts() {
        Map<String, SomedayInformation> stringSomedayInformationMap = distinctTodaySomdayInformationList();
        return sortByNextKkrq(stringSomedayInformationMap);
    }

    /**
     * 经过昨日扣款, 今日还需要正常扣款的所有贷款账号, 也就是说是昨日正常扣款未入账的所有贷款账号
     */
    private List<SomedayInformation> listYesterdayAllAccounts() {
        Map<String, SomedayInformation> stringSomedayInformationMap = distinctYesterdaySomdayInformationList();
        return sortByNextKkrq(stringSomedayInformationMap);
    }

    private List<SomedayInformation> sortByNextKkrq(Map<String, SomedayInformation> stringSomedayInformationMap) {
        List<SomedayInformation> list = new ArrayList<>();
        for (Map.Entry<String, SomedayInformation> stringSomedayInformationEntry : stringSomedayInformationMap.entrySet()) {
            list.add(stringSomedayInformationEntry.getValue());
        }
        list.sort((obj1, obj2) ->
                obj2.getNextkkrq().compareTo(obj1.getNextkkrq()));
        return list;
    }

    private void toLogTodayDkzh(List<SomedayInformation> list) {
        log.info("生成今日需要正常扣款贷款账号拼接成的执行SQL需要的字符串");
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

    private void toLogTodayAllMessage() {
        log.info("生成今日查询出来各种列表的总数量信息");
        sb.append("已经处理的贷款账号数量: ")
                .append(doneAccounts.size()).append("\n")
                .append("上月需要扣款贷款账号数量: ")
                .append(lastMonth.size()).append("\n")
                .append("昨天需要扣款贷款账号数量: ")
                .append(yesterday.size()).append("\n")
                .append("今天需要扣款贷款账号数量: ")
                .append(today.size()).append("\n")
                .append("问题账号中还存在逾期未入账的贷款账号数量: ")
                .append(overdues.size()).append("\n");

        Set<String> over = new HashSet<>();
        for (SomedayInformation doneAccount : overdues) {
            over.add(doneAccount.getDkzh());
        }

        Set<String> done = new HashSet<>();
        for (SthousingAccount doneAccount : doneAccounts) {
            done.add(doneAccount.getDkzh());
        }

        Set<String> disLastMonthToOver = distinctToOneList(over, lastMonth);
        Set<String> disYesterdayToOver = distinctToOneList(over, yesterday);
        Set<String> disTodayToOver = distinctToOneList(over, today);
        Set<String> disDoneAccountsToOver = distinctToOneList(over, doneAccounts);

        Set<String> disLastMonthToDone = distinctToOneList(done, lastMonth);
        Set<String> disYesterdayToDone = distinctToOneList(done, yesterday);
        Set<String> disTodayToDone = distinctToOneList(done, today);

        sb.append("上月和逾期重复贷款账号数量: ").append(disLastMonthToOver.size()).append("   账号: ").append(Arrays.toString(disLastMonthToOver.toArray())).append("\n");
        sb.append("昨天和逾期重复贷款账号数量: ").append(disYesterdayToOver.size()).append("   账号: ").append(Arrays.toString(disYesterdayToOver.toArray())).append("\n");
        sb.append("今天和逾期重复贷款账号数量: ").append(disTodayToOver.size()).append("   账号: ").append(Arrays.toString(disTodayToOver.toArray())).append("\n");
        sb.append("已处理和逾期重复贷款账号数量: ").append(disDoneAccountsToOver.size()).append("   账号: ").append(Arrays.toString(disDoneAccountsToOver.toArray())).append("\n");

        sb.append("上月和已处理重复贷款账号数量: ").append(disLastMonthToDone.size()).append("   账号: ").append(Arrays.toString(disLastMonthToDone.toArray())).append("\n");
        sb.append("昨天和已处理重复贷款账号数量: ").append(disYesterdayToDone.size()).append("   账号: ").append(Arrays.toString(disYesterdayToDone.toArray())).append("\n");
        sb.append("今天和已处理重复贷款账号数量: ").append(disTodayToDone.size()).append("   账号: ").append(Arrays.toString(disTodayToDone.toArray())).append("\n");


        byte[] bytes = sb.toString().getBytes();

        try (OutputStream outputStream = new FileOutputStream(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_MESSAGES_LOG)) {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Set<String> distinctToOneList(Set<String> base, List<?> list) {
        Set<String> newSet = new HashSet<>();
        Set<String> disSet = new HashSet<>();
        boolean b = newSet.addAll(base);
        if (b) {
            for (Object o : list) {
                String dkzh = "";
                if (o instanceof SomedayInformation) {
                    dkzh = ((SomedayInformation) o).getDkzh();
                } else if (o instanceof SthousingAccount) {
                    dkzh = ((SthousingAccount) o).getDkzh();
                }
                if (base.contains(dkzh)) {
                    disSet.add(dkzh);
                }
            }
        }
        return disSet;
    }

    private void toExcelTodayDkzh(List<SomedayInformation> list) {
        log.info("生成今日需要正常扣款仅贷款账号的文件");

        List<Map<String, Object>> transform = new ArrayList<>();
        for (SomedayInformation information : list) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(information);
            transform.add(stringObjectMap);
        }

        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "dkzh");
        ExcelUtil.writeToExcelByAll(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_XLS, null, keyMap, transform);
    }

    private void toExcelTodayToFlagDkzh(List<SomedayInformation> list) {
        log.info("生成今日准备给明天使用的业务分析贷款账号的文件");

        List<SomedayInformation> transformList = new ArrayList<>();
        transformList.addAll(list);

        // 该文件只是用来分析第二天的业务  可以添加逾期应该扣款的贷款账号
        transformList.addAll(overdues);
        String theLog = "逾期账号 %s 今日需要正常扣款, 会直接转逾期 \n";
        Map<String, SomedayInformation> appendOverdue = new HashMap<>();
        for (SomedayInformation somedayInformation : transformList) {
            String dkzh = somedayInformation.getDkzh();
            if (appendOverdue.containsKey(dkzh)) {
                log.error("逾期账号 {} 今日需要正常扣款, 会直接转逾期", dkzh);
                sb.append(String.format(theLog, dkzh));
            } else {
                appendOverdue.put(dkzh, somedayInformation);
            }
        }

        transformList = sortByNextKkrq(appendOverdue);


        List<Map<String, Object>> transform = new ArrayList<>();
        for (SomedayInformation information : transformList) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(information);
            transform.add(stringObjectMap);
        }


        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "dkzh");
        ExcelUtil.writeToExcelByAll(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_TO_FLAG_XLS, null, keyMap, transform);
    }

    private void toExceYesterdayDkzh(List<SomedayInformation> list) {
        log.info("生成昨日扣款未入账的贷款账号文件");
        List<Map<String, Object>> transform = new ArrayList<>();
        for (SomedayInformation information : list) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(information);
            stringObjectMap.put(stringObjectMap.get("dkzh").toString(), stringObjectMap.get("dkzh"));
            transform.add(stringObjectMap);
        }
        // 加入 逾期的贷款账号
        for (SomedayInformation overdue : overdues) {
            Map<String, Object> over = new HashMap<>();
            over.put("dkzh", overdue.getDkzh());
            over.put(overdue.getDkzh(), overdue.getDkzh());
            transform.add(over);
        }
        Map<String, Object> distinctMap = new HashMap<>();
        for (Map<String, Object> stringObjectMap : transform) {
            distinctMap.put(stringObjectMap.get("dkzh").toString(), stringObjectMap);
        }
        List<Map<String, Object>> toExcel = new ArrayList<>();
        for (Map.Entry<String, Object> entry : distinctMap.entrySet()) {
            toExcel.add((Map<String, Object>) entry.getValue());
        }

        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "dkzh");
        top.yinxiaokang.util.ExcelUtil.writeToExcelByAll(Constants.YESTERDAY_SHOULD_PAYMENT_ACCOUNT_FAIL, null, keyMap, toExcel);
    }

    private void toExcelTodayShouldPaymentAccounts(List<SomedayInformation> list) {

        list = new ArrayList<>(list);

        // 该文件只是用来观看  可以添加逾期应该扣款的贷款账号
//        list.addAll(overdues);
//        String theLog = "逾期账号 %s 今日需要正常扣款, 会直接转逾期 \n";
//        Map<String, SomedayInformation> appendOverdue = new HashMap<>();
//        for (SomedayInformation somedayInformation : list) {
//            String dkzh = somedayInformation.getDkzh();
//            if (appendOverdue.containsKey(dkzh)) {
//                log.error("逾期账号 {} 今日需要正常扣款, 会直接转逾期", dkzh);
//                sb.append(String.format(theLog, dkzh));
//            } else {
//                appendOverdue.put(dkzh, somedayInformation);
//            }
//        }

//        list = sortByNextKkrq(appendOverdue);

        log.info("生成今日应该扣款账号相关信息文件观看版");
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
            com.sargeraswang.util.ExcelUtil.ExcelUtil.exportExcel(keyMap, list, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void work() {
        List<SomedayInformation> todayAllAccounts = listTodayAllAccounts();
        toExcelTodayDkzh(todayAllAccounts);
        toExcelTodayToFlagDkzh(todayAllAccounts);
        toLogTodayDkzh(todayAllAccounts);
        toExcelTodayShouldPaymentAccounts(todayAllAccounts);
        List<SomedayInformation> yesterdayAllAccounts = listYesterdayAllAccounts();
        toExceYesterdayDkzh(yesterdayAllAccounts);
        toLogTodayAllMessage();
        log.info("结束运行");
    }


    public static void main(String[] args) {
        GetEveryDayAccounts getEveryDayAccounts = new GetEveryDayAccounts();
        getEveryDayAccounts.work();
        log.info("test");
    }

}
