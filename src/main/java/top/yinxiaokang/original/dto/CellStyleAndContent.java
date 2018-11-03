package top.yinxiaokang.original.dto;

import lombok.*;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * @author yinxk
 * @date 2018/11/3 9:54
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CellStyleAndContent {
    private Object content;
    private CellStyle cellStyle;
}
