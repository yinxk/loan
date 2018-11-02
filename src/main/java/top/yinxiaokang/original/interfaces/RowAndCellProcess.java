package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

@FunctionalInterface
public interface RowAndCellProcess {
    void process(Row row, Map<Integer, String> columnMapFirstColumnContent, Map<Integer, Integer> propertyIndexMapColumnIndex);
}
