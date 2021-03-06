package top.yinxiaokang.original.dao;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.util.BeanOrMapUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 之前对这些小工具的依赖特别小, 所以之前什么东西都揉到一个类中 .<br />
 * 当对这些小工具的依赖越来越强时, 越来越多的杂乱的类 ,还是需要整理和简单的封装一下的.<br />
 * 至于之前的 , 就先那样吧 <br />
 * 封装通用的查询方法 , 使用的地方很多
 */
@SuppressWarnings({"WeakerAccess", "JavaDoc", "unused"})
@Slf4j
public class BaseDao {
    /**
     * 数据库连接
     */
    protected Connection conn;

    public BaseDao(Connection conn) {
        this.conn = conn;
    }


    /**
     * 获取一个list
     *
     * @param clazz
     * @param sql
     * @param params
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> List<T> list(Class<T> clazz, String sql, Object... params) throws SQLException, IllegalAccessException, InstantiationException {
        return listIsShowLog(clazz, sql, true, params);
    }

    /**
     * 获取一个list, 由于可变参数的原因, 这个方法重载会出现编译器不知道匹配哪一个方法, 所有该方法改一下名字
     *
     * @param clazz
     * @param sql
     * @param params
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> List<T> listIsShowLog(Class<T> clazz, String sql, boolean showLog, Object... params) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> list = new ArrayList<>();
        ResultSet rs = selectCommon(sql, showLog, params);
        while (rs.next()) {
            T t = clazz.newInstance();
            setBean(rs, t);
            list.add(t);
        }
        return list;
    }


    /**
     * 通用查询方法
     *
     * @param sql
     * @param paras
     * @return
     * @throws SQLException
     */
    public ResultSet selectCommon(String sql, boolean showLog, Object... paras)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < paras.length; i++) {
            ps.setObject(i + 1, paras[i]);
        }
        if (showLog) {
            log.info(sql);
            log.info("parameters : " + Arrays.asList(paras));
        }
        return ps.executeQuery();
    }


    /**
     * 通用更新方法
     *
     * @param sql
     * @param paras
     * @return
     * @throws SQLException
     */
    public int updateCommon(String sql, Object... paras)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < paras.length; i++) {
            ps.setObject(i + 1, paras[i]);
        }
        log.info(sql);
        log.info("parameters : " + Arrays.asList(paras));
        return ps.executeUpdate();
    }

    /**
     * 设置bean
     *
     * @param rs
     * @param obj
     * @throws SQLException
     */
    protected void setBean(ResultSet rs, Object obj)
            throws SQLException {
        Map<String, Object> rowData = new HashMap<>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            //rowData.put(Utils.toLowerCase(rs.getMetaData().getColumnName(i)), rs.getObject(i));
            rowData.put(rs.getMetaData().getColumnName(i).toLowerCase(), rs.getObject(i));
        }
        BeanOrMapUtil.transMap2Bean(rowData, obj);
    }
}
