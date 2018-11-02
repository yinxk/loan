package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

@FunctionalInterface
public interface RowAndCellProcess {
    void process(Row row, Cell cell, Object cellContent,Map<Integer, String> keyMap);
}
