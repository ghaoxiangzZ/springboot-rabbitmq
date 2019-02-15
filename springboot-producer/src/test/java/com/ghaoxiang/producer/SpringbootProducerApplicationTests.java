package com.ghaoxiang.producer;

import com.ghaoxiang.entity.Order;
import com.ghaoxiang.producer.service.OrderService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootProducerApplicationTests {

    @Autowired private OrderService orderService;

    @Test
    public void testSend() throws Exception {
        for (int i = 0; i < 40; i++) {
            Order order = new Order();
            order.setId(i+1955);
            order.setName("测试订单");
            order.setMessageId(System.currentTimeMillis() + "$" + UUID.randomUUID().toString());
            orderService.createOrder(order);
        }
    }
}
