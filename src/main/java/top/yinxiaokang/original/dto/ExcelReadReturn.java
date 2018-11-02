package top.yinxiaokang.original.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author yinxk
 * @date 2018/11/2 22:34
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExcelReadReturn {
    Map<Integer, Integer> propertyIndexMapColumnIndex;
    Map<Integer, String> columnMapFirstColumnContent;
    List<Map<String, Object>> content;

}
