package top.yinxiaokang.original;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import top.yinxiaokang.util.ImportExcelUtilLessFour;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/10/30 21:23
 */
public class FlagRedRecode {
    public static void main(String[] args) {
        new FlagRedRecode().work();
    }

    public static String pathStrComputer = "C:\\修账相关数据\\程序导出\\201810月\\";
    public static String pathStrHandWork = "C:\\修账相关数据\\手动导出\\201810月\\";
    public static String xls = ".xls";
    public static String xlsx = ".xlsx";
    public static String flagRedFileName = "2018-10-31-扣款之后未入账账号";
    public static String inFileName = "2018-10-31-业务推算和实际业务-标红代表不正常";

    public List<String> listFlagRedDkzh() {
        List<Map<String, Object>> list = ImportExcelUtilLessFour.read(pathStrHandWork + flagRedFileName + xlsx, 0, false);
        Iterator<Map<String, Object>> iterator = list.iterator();
        List<String> dkzhs = new ArrayList<>();
        while (iterator.hasNext()) {
            Map next = iterator.next();
            String dkzh = (String) next.get("dkzh");
            dkzhs.add(dkzh);
        }
        return dkzhs;
    }

    public List<Map<String, Object>> listOneDayData() {
        return ImportExcelUtilLessFour.read(pathStrComputer + inFileName + xls, 0, false);
    }

    public Set getKeyMap(List<Map<String, Object>> list) {
        Iterator<Map<String, Object>> iterator = list.iterator();
        if (iterator.hasNext()) {
            Map next = iterator.next();
            return next.keySet();
        }
        return null;
    }

    public List<List<Integer>> flagRedTags(List<Map<String, Object>> oneDayDataList, List<String> dkzhs) {
        List<List<Integer>> result = new ArrayList<>();
        Iterator<String> iterator = dkzhs.iterator();
        while (iterator.hasNext()) {
            String dkzh = iterator.next();
            List<Integer> integers = flagReadTagByOne(oneDayDataList, dkzh);
            if (integers.size() > 0)
                result.add(integers);
        }
        return result;
    }

    private List<Integer> flagReadTagByOne(List<Map<String, Object>> oneDayDataList, String dkzh) {
        List<Integer> oneDkzhTags = new ArrayList<>();
        int startTag = -1;
        for (int i = 0; i < oneDayDataList.size(); i++) {
            Map<String, Object> item = oneDayDataList.get(i);
            String dkzhStr = (String) item.get("贷款账号");

            if (dkzh.equals(dkzhStr)) {
                startTag = i;
                oneDkzhTags.add(i);
            }
            if (startTag != -1 && i > startTag) {
                if (StringUtils.isEmpty(dkzhStr))
                    oneDkzhTags.add(i);
                else
                    break;
            }
        }
        return oneDkzhTags;
    }

    private void toExcel(List<Map<String, Object>> oneDayDataList, Set keyMap, List<List<Integer>> flagRedTags) {
        Workbook wb = new HSSFWorkbook();
        CellStyle cellStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        cellStyle.setFont(font);
        try (OutputStream fileOut = new FileOutputStream(pathStrComputer + inFileName + "-标记版" + xls)) {
            Sheet sheet = wb.createSheet();
            Row row0 = sheet.createRow(0);
            Iterator iterator = keyMap.iterator();
            int k = 0;
            while (iterator.hasNext()) {
                String next = (String) iterator.next();
                Cell cell = row0.createCell(k++);
                cell.setCellValue(next);
            }
            for (int i = 0; i < oneDayDataList.size(); i++) {
                Map<String, Object> map = oneDayDataList.get(i);
                Row row = sheet.createRow(i + 1);
                Collection<Object> values = map.values();
                int j = 0;

                for (Object value : values) {
                    Object obj = Optional.ofNullable(value).orElse("");
                    Cell cell = row.createCell(j++);
                    cell.setCellValue(obj.toString());
                    for (List<Integer> oneDkzhTags : flagRedTags) {
                        for (Integer tag : oneDkzhTags) {
                            if (tag == i) {
                                cell.setCellStyle(cellStyle);
                            }
                        }
                    }
                }
            }

            wb.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void work() {
        List<String> dkzhs = listFlagRedDkzh();
        List<Map<String, Object>> oneDayDataList = listOneDayData();
        Set keyMap = getKeyMap(oneDayDataList);
        List<List<Integer>> flagRedTags = flagRedTags(oneDayDataList, dkzhs);
        toExcel(oneDayDataList, keyMap, flagRedTags);
        System.out.println("标记运行结束!");
    }


}
