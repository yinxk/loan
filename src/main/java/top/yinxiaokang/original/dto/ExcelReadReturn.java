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
    Map<Integer, String> colIndexMapContent;
    Map<String, Integer> contentMapColIndex;
    Map<Integer, Integer> proIndexMapColIndex;
    List<Map<String, Object>> content;

}
