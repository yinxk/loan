package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

@FunctionalInterface
public interface BaseExcelReader {
    void process(Workbook workbook);
}
