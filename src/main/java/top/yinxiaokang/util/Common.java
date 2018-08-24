package top.yinxiaokang.util;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yinxk
 * @date 2018/8/21 10:05
 */
public class Common {
    /**
     * 根据期次获取对应还款计划的某一期
     *
     * @param list
     * @param dqqc
     * @return
     */
    public static RepaymentItem getRepaymentItemByDqqc(List<RepaymentItem> list, Integer dqqc) {
        for (RepaymentItem item : list) {
            if (dqqc == item.getHkqc()) {
                return item;
            }
        }
        return null;
    }

    /**
     * 读取excel , 返回集合
     * @param fileName 需要 包含扩展名
     * @return
     */
    public static Collection<Map> xlsToList(String fileName){
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
}
