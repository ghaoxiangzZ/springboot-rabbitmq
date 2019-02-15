package com.ghaoxiang.producer.service;

import com.ghaoxiang.entity.BrokerMessageLog;
import com.ghaoxiang.entity.Order;
import com.ghaoxiang.producer.constant.Constants;
import com.ghaoxiang.producer.utils.FastJsonConvertUtil;
import com.ghaoxiang.producer.dao.BrokerMessageLogMapper;
import com.ghaoxiang.producer.dao.OrderMapper;
import com.ghaoxiang.producer.sender.RabbitOrderSender;
import com.ghaoxiang.producer.utils.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BrokerMessageLogMapper brokerMessageLogMapper;

    @Autowired
    private RabbitOrderSender rabbitOrderSender;

    public void createOrder(Order order) throws Exception {
        /**
         * step1 业务数据入库
         */
        // 使用当前时间当做订单创建时间（为了模拟一下简化）
        Date orderTime = new Date();
        // 插入业务数据
        orderMapper.insert(order);
        // 插入消息记录表数据
        BrokerMessageLog brokerMessageLog = new BrokerMessageLog();
        // 消息唯一ID
        brokerMessageLog.setMessageId(order.getMessageId());
        // 保存消息整体 转为JSON 格式存储入库
        brokerMessageLog.setMessage(FastJsonConvertUtil.convertObjectToJSON(order));
        // 设置消息状态为0 表示发送中
        brokerMessageLog.setStatus("0");
        // 设置消息未确认超时时间窗口为 一分钟
        brokerMessageLog.setNextRetry(DateUtils.addMinutes(orderTime, Constants.ORDER_TIMEOUT));
        brokerMessageLog.setCreateTime(new Date());
        brokerMessageLog.setUpdateTime(new Date());
        brokerMessageLogMapper.insertSelective(brokerMessageLog);
        /**
         * step2 业务数据发送rabbitmq
         */
        // 发送消息
        rabbitOrderSender.sendOrder(order);
    }

}

