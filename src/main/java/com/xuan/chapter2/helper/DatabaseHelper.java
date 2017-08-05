package com.xuan.chapter2.helper;

import com.xuan.chapter2.util.CollectionUtil;
import com.xuan.chapter2.util.PropsUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by xuan on 2017/8/5 0005.
 */
public final class DatabaseHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);
    //ThreadLocal 线程安全的，原理是什么呢
    private static final ThreadLocal<Connection> CONNECTION_HOLDER =
            new ThreadLocal<Connection>();
    //数据库连接池
    private static final BasicDataSource DATA_SOURCE;


    private static final String DRIVER;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = PropsUtil.loadProps("config.properties");
        DRIVER = props.getProperty("jdbc.driver");
        URL = props.getProperty("jdbc.url");
        USERNAME = props.getProperty("jdbc.username");
        PASSWORD = props.getProperty("jdbc.password");

        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(DRIVER);
        DATA_SOURCE.setUrl(URL);
        DATA_SOURCE.setUsername(USERNAME);
        DATA_SOURCE.setPassword(PASSWORD);

        //检查一下jdbc驱动
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            LOGGER.error("can not load jdbc driver", e);
        }
    }

    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    public static void executeSqlFile(String sqlFilePath) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(sqlFilePath)
                )
        );
        try {
            String sql;
            while ((sql = reader.readLine()) != null) {
                executeUpdate(sql);
            }
        } catch (IOException e) {
            LOGGER.error("execute sql file failure", e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T queryEntity(Class<T> entityClass, String sql, Object... params) {
        T entity = null;

        try {
            entity = QUERY_RUNNER.query(getConnection(), sql, new BeanHandler<T>(entityClass), params);
        } catch (SQLException e) {
            LOGGER.error("query entity error");
            throw new RuntimeException(e);
        }

        return entity;
    }

    public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object... params) {
        List<T> entityList = null;
        try {
            entityList = QUERY_RUNNER.query(getConnection(), sql, new BeanListHandler<T>(entityClass), params);
        } catch (SQLException e) {
            LOGGER.error("query entity list failure", e);
            throw new RuntimeException(e);
        }

        return entityList;
    }

    public static Connection getConnection() {
        Connection conn = CONNECTION_HOLDER.get();
        if (conn == null) {
            try {
//                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);不是从DriverManager获取Connection了
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("get connection failure", e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
//        try {
//            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//        } catch (SQLException e) {
//            LOGGER.error("get connection failure", e);
//        }
        return conn;
    }
    //可能存在多表查询，需要更强大的查询方法 查询出多个对象，每个对象用一个map表实，里面存了列名与值
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> result = null;

        try {
            result = QUERY_RUNNER.query(getConnection(), sql, new MapListHandler(), params);
        } catch (SQLException e) {
            LOGGER.error("execute query failure", e);
            throw new RuntimeException(e);
        }

        return result;
    }

    public static int executeUpdate(String sql, Object... params) {
        int rows = 0;

        try {
            rows = QUERY_RUNNER.update(getConnection(), sql, params);
        } catch (SQLException e) {
            LOGGER.error("execute update failure", e);
            throw new RuntimeException(e);
        }

        return rows;
    }

    public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {
        if (CollectionUtil.isEmpty(fieldMap)) {
            LOGGER.error("can not insert entity: fieldMap is empty");
            return false;
        }

        String sql = "insert into " + getTableName(entityClass);
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String columnName : fieldMap.keySet()) {
            columns.append(columnName).append(", ");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(", "), columns.length(), ")");
        values.replace(values.lastIndexOf(", "), values.length(), ")");
        sql += columns + " values " + values;

        //keyset处理完了 处理map的values
        //values()返回Collection对象，容器toArray转化为数组
        Object[] params = fieldMap.values().toArray();
        return executeUpdate(sql, params) == 1;
    }

    public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {
        if (CollectionUtil.isEmpty(fieldMap)) {
            LOGGER.error("can not update entity: fieldMap is empty");
            return false;
        }
        String sql = "update " + getTableName(entityClass) + " set ";
        StringBuilder columns = new StringBuilder();
        for (String columnName : fieldMap.keySet()) {
            columns.append(columnName).append("=?, ");
        }
        sql += columns.substring(0, columns.lastIndexOf(", ")) + " where id=?";
        List<Object> paramList = new ArrayList<Object>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();
        return executeUpdate(sql, params) == 1;
    }

    public static <T> boolean deleteEntity(Class<T> entityClass, long id) {
        String sql = "delete from " + getTableName(entityClass) + " where id=?";
        return executeUpdate(sql, id) == 1;
    }

    private static <T> String getTableName(Class<T> entityClass) {
//        return entityClass.getSimpleName().toLowerCase(); 查询时不分大小写
        return entityClass.getSimpleName();
    }

    //数据库连接个数有限，而且频繁创建和销毁是浪费巨大系统资源的，应该考虑池化，数据库连接池产品选择apache.dbcp2，添加maven依赖
//    public static void closeConnection() {
//        Connection conn = CONNECTION_HOLDER.get();
//        if (conn != null) {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                LOGGER.error("close connection failure", e);
//                throw new RuntimeException(e);
//            } finally {
//                CONNECTION_HOLDER.remove();
//            }
//        }
//    }
}

