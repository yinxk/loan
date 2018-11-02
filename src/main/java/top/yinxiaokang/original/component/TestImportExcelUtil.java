package top.yinxiaokang.original.component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import top.yinxiaokang.util.ExcelUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestImportExcelUtil {
    public static void main(String[] args) {

        //List<Map<String, Cell>> maps = ImportExcelUtilLessFour.readHaveCell("C:\\修账相关数据\\修账\\2018-10-20-业务推算和实际业务-凭证调整数据-加说明.xls", 1, false, false);
        System.out.println("333");
        String regexNumber = "^\\d+$";
        String regexDkzh = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern patternDkzh = Pattern.compile(regexDkzh);
        Pattern patternNumber = Pattern.compile(regexNumber);

        ExcelUtil.copyExcelAndUpdate("C:\\修账相关数据\\修账\\2018-10-20-业务推算和实际业务-凭证调整数据-加说明.xls", 1, false, null, row -> {
            Cell cell = row.getCell(0);
            cell.setCellType(CellType.STRING);
            String value = cell.getStringCellValue();
            Matcher matcher = patternNumber.matcher(value);
            if (matcher.find()) {
                Cell cell1 = row.getCell(1);
                cell1.setCellType(CellType.STRING);

                Matcher matcherDkzh = patternDkzh.matcher(cell1.getStringCellValue());
                if (matcherDkzh.find()) {
                    String dkzh = matcherDkzh.group(1);

                    Cell cell10 = row.getCell(10);
                    cell10.setCellType(CellType.STRING);
                    String cellValue = cell10.getStringCellValue();

                    cell10.setCellValue("jsdjlfkjsdlfjsdl " + cellValue);
                    System.out.println(value);
                } else {
                    throw new RuntimeException("匹配出错");
                }

            }

        });

    }
}
