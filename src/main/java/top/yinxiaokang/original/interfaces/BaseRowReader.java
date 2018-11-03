package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

@FunctionalInterface
public interface BaseRowReader {
    void process(Workbook workbook, Row row, Map<Integer, String> keyMap, Map<String, Integer> keyMapReverse);
}
