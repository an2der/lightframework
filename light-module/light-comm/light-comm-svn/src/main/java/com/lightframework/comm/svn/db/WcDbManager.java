package com.lightframework.comm.svn.db;

import com.lightframework.common.LightException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WcDbManager {

    private Connection connection;

    private String wcDir;

    public WcDbManager(String wcDir) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", Paths.get(wcDir,".svn","wc.db").toString()));
            this.wcDir = wcDir;
        } catch (Exception ex) {
            throw new LightException(ex);
        }
    }

    /**
     * 清除SVN客户端库中的锁
     *
     * @return
     */
    public void cleanLocks() {
        try {
            //清空svn目录中工作队列标记删除的文件
            List<Map<String, Object>> workQueues = select("select cast(work as varchar) work from WORK_QUEUE", null);
            if (workQueues != null && workQueues.size() > 0) {
                workQueues.forEach(item -> {
                    String work = String.valueOf(item.get("work"));
                    if (work.contains("file-remove")) {
                        File file = new File(wcDir,work.replace("(", "")
                                .replace(")", "")
                                .replaceFirst("file-remove [0-9]+ ", ""));
                        if(file.exists()) {
                            file.delete();
                        }
                    }
                });
            }
            //清空svn本地仓库的锁
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.executeUpdate("delete from WORK_QUEUE; delete from WC_LOCK;");
            connection.commit();
        } catch (Exception e) {
            throw new LightException(e);
        }finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
            }
        }
    }

    /**
     * 执行查询，并将值反射到map
     *
     * @param sql
     * @param parameters
     * @return
     */
    private List<Map<String, Object>> select(String sql, Object[] parameters) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        PreparedStatement ps;
        ResultSet rs;

        ps = connection.prepareStatement(sql);
        if (ps != null) {
            if (parameters != null) {
                for (int i = 1; i <= parameters.length; i++) {
                    ps.setObject(i, parameters[i - 1]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            List<String> columnList = new ArrayList<String>();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                columnList.add(rsmd.getColumnName(i + 1));
            }
            while (rs.next()) {
                Map<String, Object> obj = new HashMap<>();
                // 遍历一个记录中的所有列
                for (int i = 0; i < columnList.size(); i++) {
                    String column = columnList.get(i);
                    String colKey = underlineToHump(column);
                    obj.put(colKey, rs.getObject(column));
                }
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 驼峰转下划线
     * @param str   目标字符串
     * @return: String
     */
    private static String humpToUnderline(String str) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group();
            str = str.replaceAll(target, "_"+target.toLowerCase());
        }
        return str;
    }

    /**
     * 下划线转驼峰
     * @param str   目标字符串
     * @return: String
     */
    private static String underlineToHump(String str) {
        String regex = "_(.)";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group(1);
            str = str.replaceAll("_"+target, target.toUpperCase());
        }
        return str;
    }
}
