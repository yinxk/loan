package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Row;

@FunctionalInterface
public interface ExcelProcess {
    void doUpdate(Row row);
}
