package top.yinxiaokang.original;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import top.yinxiaokang.util.ImportExcelUtilLessFour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinxk
 * @date 2018/10/30 11:45
 */
public class ExcelTransform {

    private static String pathStr = "C:\\Users\\where\\Desktop\\修账相关数据\\修账\\";
    private static String fileStr = "2018-10-15-业务推算和实际业务-凭证调整数据-加说明";
    private static String xls = ".xls";

    public static void main(String[] args) {
        ExcelTransform excelTransform = new ExcelTransform();
        //File diretory = new File(pathStr);
        //if (!diretory.isDirectory()) {
        //    throw new RuntimeException("not directory");
        //}
        //File[] files = diretory.listFiles();
        //for (int i = 0; i < files.length; i++) {
        //    File file = files[i];
        //    if (file.isFile()) {
        //        excelTransform.doTransform(file.getPath(), file.getName());
        //    }
        //}

        excelTransform.doTransform(pathStr + fileStr + xls, fileStr + xls);

        System.out.println("运行结束!");

        //
    }


    public void doTransform(String pathFileName, String fileName) {
        List<Map<String, Object>> list = ImportExcelUtilLessFour.read(pathFileName, 1, false);
        Iterator<Map<String, Object>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            Object 序号 = next.get("序号");
            if (!(序号 instanceof Double)) {
                iterator.remove();
            }

        }
        iterator = list.iterator();
        String regex = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern pattern = Pattern.compile(regex);


        int notMatchNumber = 0;
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            String 行号 = (String) next.get("行号");
            Matcher matcher = pattern.matcher(行号);
            if (matcher.find()) {
                String group = matcher.group(1);
                if (StringUtils.isBlank(group)) {
                    iterator.remove();
                    continue;
                }
                System.out.println("匹配得到的贷款账号: ====" + group + "====");
                next.put("dkzh", group);
            } else {
                System.out.println("存在没有匹配" + ++notMatchNumber);
            }
        }


        Map<String, String> keyMap = new LinkedHashMap<>();

        keyMap.put("序号", "xh");
        keyMap.put("dkzh", "dkzh");
        keyMap.put("发生额差额合计", "fsecehj");
        keyMap.put("本金差额合计", "bjcehj");
        keyMap.put("利息差额合计", "lxcehj");
        keyMap.put("备注", "bz");
        keyMap.put("说明", "sm");
        keyMap.put("行号", "hh");

        String[] split = fileName.split("\\.");


        Workbook wb = new HSSFWorkbook();
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(pathStr + "转换版/" + split[0] + "-转换版" + xls))) {
            //ExcelUtil.exportExcel(keyMap, list, fileOutputStream);
            filterType(list, keyMap, wb);
            wb.write(fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(pathFileName + "=====转换完成=====");
    }

    private void filterType(List<Map<String, Object>> list, Map<String, String> keyMap, Workbook wb) {

        Sheet sheet1 = wb.createSheet("全部");
        Sheet sheet2 = wb.createSheet("本息颠倒");
        Sheet sheet3 = wb.createSheet("多扣");
        Sheet sheet4 = wb.createSheet("少扣");
        Sheet sheet5 = wb.createSheet("其他");
        // 本息颠倒
        List<Map<String, Object>> list2 = new ArrayList<>();
        /**
         * 多扣
         */
        List<Map<String, Object>> list3 = new ArrayList<>();
        /**
         * 少扣
         */
        List<Map<String, Object>> list4 = new ArrayList<>();
        /**
         * 其他
         */
        List<Map<String, Object>> list5 = new ArrayList<>();
        Row row1 = sheet1.createRow(0);
        Row row2 = sheet2.createRow(0);
        Row row3 = sheet3.createRow(0);
        Row row4 = sheet4.createRow(0);
        Row row5 = sheet5.createRow(0);


        Set<Map.Entry<String, String>> keyEntry = keyMap.entrySet();
        Iterator<Map.Entry<String, String>> keyIte = keyEntry.iterator();
        int k = 0;
        while (keyIte.hasNext()) {
            Map.Entry<String, String> key = keyIte.next();
            Cell cell1 = row1.createCell(k);
            Cell cell2 = row2.createCell(k);
            Cell cell3 = row3.createCell(k);
            Cell cell4 = row4.createCell(k);
            Cell cell5 = row5.createCell(k);

            k++;

            cell1.setCellValue(key.getValue());
            cell2.setCellValue(key.getValue());
            cell3.setCellValue(key.getValue());
            cell4.setCellValue(key.getValue());
            cell5.setCellValue(key.getValue());
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> contentMap = list.get(i);
            String 备注 = Optional.of(contentMap.get("备注")).map(Object::toString).orElse("");
            if (备注.contains("多扣")) {
                list3.add(contentMap);
            } else if (备注.contains("少扣")) {
                list4.add(contentMap);
            } else if (备注.contains("颠倒")) {
                list2.add(contentMap);
            } else {
                list5.add(contentMap);
            }
        }
        creatRowAndCell(list, keyMap, sheet1);
        creatRowAndCell(list2, keyMap, sheet2);
        creatRowAndCell(list3, keyMap, sheet3);
        creatRowAndCell(list4, keyMap, sheet4);
        creatRowAndCell(list5, keyMap, sheet5);


    }


    private void creatRowAndCell(List<Map<String, Object>> list, Map<String, String> keyMap, Sheet sheet) {
        for (int i = 0; i < list.size(); i++) {
            Row row = sheet.createRow(i+1);
            Map<String, Object> contentMap = list.get(i);
            Iterator<Map.Entry<String, String>> keyIte = keyMap.entrySet().iterator();
            int j= 0;
            while (keyIte.hasNext()) {
                Map.Entry<String, String> key = keyIte.next();
                if (contentMap.containsKey(key.getKey())) {
                    sheet.autoSizeColumn(j);
                    Cell cell = row.createCell(j++);
                    String content = Optional.ofNullable(contentMap.get(key.getKey())).map(Object::toString).orElse("");
                    cell.setCellValue(content);
                } else {
                    throw new RuntimeException("not have the key : " + key.getKey());
                }

            }
        }
    }
}
