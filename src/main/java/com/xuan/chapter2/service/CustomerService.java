package com.xuan.chapter2.service;

import com.xuan.chapter2.helper.DatabaseHelper;
import com.xuan.chapter2.model.Customer;
import com.xuan.chapter2.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * service层 做数据库查询操作，生成model里面的类的对象
 * Created by xuan on 2017/8/4 0004.
 */
public class CustomerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

//    private static final String DRIVER;
//    private static final String URL;
//    private static final String USERNAME;
//    private static final String PASSWORD;

    /**
     * 这一部分代码，每个service组建都要做 是重复的，比方customerService，ManagerService都对应着同一个数据库里面的不同表
     * 所以下面是通用的，应该提取出去
     */
//    static {
//        Properties props = PropsUtil.loadProps("config.properties");
//        DRIVER = props.getProperty("jdbc.driver");
//        URL = props.getProperty("jdbc.url");
//        USERNAME = props.getProperty("jdbc.username");
//        PASSWORD = props.getProperty("jdbc.password");
//    }
    /**
     * 获取客户列表
     * @param
     * @return
     */
    public List<Customer> getCustomerList() {
        List<Customer> customerList = null;
//        Connection conn = DatabaseHelper.getConnection(); 把connection也屏蔽了
        String sql = "select * from customer";

//        customerList = DatabaseHelper.queryEntityList(Customer.class, conn, sql);
        customerList = DatabaseHelper.queryEntityList(Customer.class, sql);

        return customerList;
//        Connection conn = null;
//        try {
//            customerList = new ArrayList<Customer>();
//            /**
//             * 每个Service都需要的创建数据库连接 被独立出去了 getConnection()也解耦了，以后可以用连接池了
//             */
//            conn = DatabaseHelper.getConnection();
//            /**
//             * 执行一个查询代价太高
//             */
//            String sql = "select * from customer";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                /**
//                 * 这里的代码虽不完全相同，但是逻辑是一样的，又不能自动生成，求助于反射，将这部分代码去掉DbUtils
//                 */
//                Customer customer = new Customer();
//                customer.setId(rs.getLong("id"));
//                customer.setName(rs.getString("name"));
//                customer.setContact(rs.getString("contact"));
//                customer.setTelephone(rs.getString("telephone"));
//                customer.setEmail(rs.getString("email"));
//                customer.setRemark(rs.getString("remark"));
//                customerList.add(customer);
//            }
//        } catch (SQLException e) {
//            LOGGER.error("execute sql failure", e);
//        } finally {
//            DatabaseHelper.closeConnection(conn);
            /**
             * 还是都要的逻辑独立出去，也是解耦了，以后可以改成归还到连接池里面也不会影响这里的代码了
             */
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException e) {
//                    LOGGER.error("close connection failure", e);
//                }
//            }
//        }
//        return customerList;
    }

    /**
     * 获取客户
     * @param id
     * @return
     */
    public Customer getCustomer(long id) {
        String sql = "select * from customer where id=?";   //DbUtils query 格式是 "select * from customer where id = ?", 2
        return DatabaseHelper.queryEntity(Customer.class, sql, id);
    }

    /**
     * 创建客户
     * @param fieldMap
     * @return
     */
    public boolean createCustomer(Map<String, Object> fieldMap) {
        return DatabaseHelper.insertEntity(Customer.class, fieldMap);
    }

    /**
     * 更新客户
     * @param id
     * @param fieldMap
     * @return
     */
    public boolean updateCustomer(long id, Map<String, Object> fieldMap) {
        return DatabaseHelper.updateEntity(Customer.class, id, fieldMap);
    }

    /**
     * 删除客户
     * @param id
     * @return
     */
    public boolean deleteCustomer(long id) {
        return DatabaseHelper.deleteEntity(Customer.class, id);
    }
}
