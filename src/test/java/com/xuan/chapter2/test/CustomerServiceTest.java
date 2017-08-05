package com.xuan.chapter2.test;

import com.xuan.chapter2.helper.DatabaseHelper;
import com.xuan.chapter2.model.Customer;
import com.xuan.chapter2.service.CustomerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuan on 2017/8/4 0004.
 */
public class CustomerServiceTest {
    private final CustomerService customerService;

    public CustomerServiceTest() {
        customerService = new CustomerService();
    }

    @Before
    public void init() throws IOException {
        // 初始化数据库
        //测试开发使用不通过的数据库，但是表名应该相同
        //每个单元测试类都需要初始化数据库，因此把批量执行sql语句抽象出来
        String file = "sql/customer_init.sql";  //为什么可以这么写，是因为maven java 和resources是根目录
        DatabaseHelper.executeSqlFile(file);
//        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));  //为了使用readline
//        String sql;
//        while ((sql = br.readLine()) != null) {
//            DatabaseHelper.executeUpdate(sql);
//        }
    }

    @Test
    public void getCustomerListTest() {
        List<Customer> customerList = customerService.getCustomerList();
        Assert.assertEquals(2, customerList.size());
    }
    @Test
    public void getCustomerTest() {
        long id = 2;
        Customer customer = customerService.getCustomer(id);
        Assert.assertNotNull(customer);
    }
    @Test
    public void createCustomerTest() {
        Map<String, Object> fieldMap = new HashMap<String, Object>();
        fieldMap.put("name", "customer100");
        fieldMap.put("contact", "John");
        fieldMap.put("telephone", "13512345678");
        boolean result = customerService.createCustomer(fieldMap);
        Assert.assertTrue(result);
    }
    @Test
    public void updateCustomerTest() {
        long id = 1;
        Map<String, Object> fieldMap = new HashMap<String, Object>();
        fieldMap.put("contact", "Eric");
        boolean result = customerService.updateCustomer(1, fieldMap);
        Assert.assertTrue(result);
    }
    @Test
    public void deleteCustomerTest() {
        long id = 1;
        boolean result = customerService.deleteCustomer(id);
        Assert.assertTrue(result);
    }
}
