package top.yinxiaokang.original.component;

import top.yinxiaokang.original.dao.BaseDao;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.util.ExcelUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ImportToTable {
    public static void main(String[] args) {
        String path = "C:\\修账相关数据\\提前还款数据 - V5-截止6-10日前 -计算结果终极版.xlsx";

        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(path, 0, false, false);

        List<Map<String, Object>> content = excelReadReturn.getContent();
        System.out.println(content.size());

        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        BaseDao baseDao = new BaseDao(connection);

        String sql = " insert into tmp_table(id,zjhm,fse,ywfsrq) values(?,?,?,?) ";

        int i = 1;
        for (Map<String, Object> map : content) {
            Object zjhm = map.get("身份证号码");
            Object fse = map.get("提前还款金额");
            Object ywfsrq = map.get("业务发生日期");

            try {
                int i1 = baseDao.updateCommon(sql, i, zjhm, fse, ywfsrq);
                i++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("运行结束!");


    }
}
