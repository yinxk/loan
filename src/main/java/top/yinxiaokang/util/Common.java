package top.yinxiaokang.util;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.enums.LoanBusinessType;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/8/21 10:05
 */
public class Common {
    /**
     * 误差范围
     */
    public static final BigDecimal ERROR_RANGE = new BigDecimal("0.02");

    /**
     * 横杠 , 没有信息
     */
    public static final String NO_MESS = "---";
    public static final String NO_MESS_CHINESE = "－－－－";
    public static final String FULL_TWO_SPACE = "　　";
    public static final String FULL_SPACE = "　";


    /**
     * 判断贷款账号是否存在每天的map中
     * @param dkzh
     * @param oneDayMap
     * @return
     */
    public static boolean isDkzhInOneDayMap(String dkzh, Collection<Map> oneDayMap) {
        Iterator<Map> iterator = oneDayMap.iterator();
        boolean isIn = false;
        while (iterator.hasNext()) {
            Map next = iterator.next();
            if (dkzh.equals(((String) next.get("dkzh")))) {
                isIn = true;
            }
        }
        return isIn;
    }


    public static List<InitInformation> listBaseAccountInformation() {
        List<Map<String, Object>> readList = ImportExcelUtilLessFour.read(Constants.BASE_ACCOUNT_INFORMATION, 0, false, false);
        return Common.importExcelToInitInformationList(readList);
    }

    /**
     * 将读取到的初始的excel的map转换为初始对象List
     *
     * @param importExcel
     * @return
     */
    public static List<InitInformation> importExcelToInitInformationList(Collection<Map> importExcel) {
        ArrayList<InitInformation> initHasOverdueList = new ArrayList<>();
        int i = 0;
        for (Map m : importExcel) {
            i = transformMap2Bean(initHasOverdueList, i, m);
        }
        return initHasOverdueList;
    }

    /**
     * 将读取到的初始的excel的map转换为初始对象List
     *
     * @param readList
     * @return
     */
    public static List<InitInformation> importExcelToInitInformationList(List<Map<String, Object>> readList) {
        ArrayList<InitInformation> initHasOverdueList = new ArrayList<>();
        int i = 0;
        for (Map m : readList) {
            i = transformMap2Bean(initHasOverdueList, i, m);
        }
        return initHasOverdueList;
    }

    private static int transformMap2Bean(ArrayList<InitInformation> initHasOverdueList, int i, Map m) {
        InitInformation initHasOverdue = new InitInformation();
        initHasOverdue.setDkzh((String) m.get("dkzh"));
        initHasOverdue.setCsye(new BigDecimal((String) m.get("csye")));
        initHasOverdue.setCsyqbj(new BigDecimal((String) m.get("csyqbj")));
        initHasOverdueList.add(initHasOverdue);
        System.out.println("转换excel导入的map为 初始信息对象  " + ++i);
        return i;
    }


    /**
     * 根据期次获取对应还款计划的某一期
     *
     * @param list
     * @param dqqc
     * @return
     */
    public static RepaymentItem getRepaymentItemByDqqc(List<RepaymentItem> list, Integer dqqc) {
        for (RepaymentItem item : list) {
            if (dqqc.equals(item.getHkqc())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 读取excel , 返回集合
     *
     * @param fileName 需要 包含扩展名
     * @return
     */
    public static Collection<Map> xlsToList(String fileName) {
        Collection<Map> importExcel = new ArrayList<>();
        try {
            try (InputStream inputStream = new FileInputStream(new File(fileName))) {
                ExcelLogs logs = new ExcelLogs();
                importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return importExcel;
    }

    /**
     * 获取提前还款或者结清的业务,并根据提前还款的业务发生日期进行排序
     *
     * @param details
     * @return
     */
    public static List<SthousingDetail> listPrepayment(List<SthousingDetail> details) {
        List<SthousingDetail> prepaymentList = new ArrayList<>();
        for (SthousingDetail detail : details) {
            if (LoanBusinessType.提前还款.getCode().equals(detail.getDkywmxlx()) || LoanBusinessType.结清.getCode().equals(detail.getDkywmxlx())) {
                prepaymentList.add(detail);
            }
        }
        Collections.sort(prepaymentList, Comparator.comparing(SthousingDetail::getYwfsrq));
        return prepaymentList;
    }

}
