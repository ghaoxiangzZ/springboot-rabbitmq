package com.sxw.springbootproducer.producer;

import com.alibaba.fastjson.JSON;
import com.sxw.entity.Order;
import com.sxw.springbootproducer.constant.Constants;
import com.sxw.springbootproducer.mapper.BrokerMessageLogMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class RabbitOrderSender implements RabbitTemplate.ConfirmCallback{

    // 自动注入RabbitTemplate模板类
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BrokerMessageLogMapper brokerMessageLogMapper;

    // comfirm模式：异步监听broker，根据回调做业务逻辑判断
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("消息唯一标识："+correlationData);
        System.out.println("确认结果："+ack);
        System.out.println("失败原因："+cause);
        String messageId = correlationData.getId();
        if(ack){
            // 如果confirm返回成功 则进行更新
            System.err.println("已成功发送RabbitMQ,正在执行更新状态操作");
            brokerMessageLogMapper.changeBrokerMessageLogStatus(messageId, Constants.ORDER_SEND_SUCCESS, new Date());
        } else {
            // 失败则进行具体的后续操作:重试 或者补偿等手段
            System.err.println("异常处理..." + cause);
        }
    }

    /**
     * 复杂调用rabbitmq（包含异步监听broker回调）
     */
    public void sendOrder(Order order) throws Exception {
        // 通过实现 ConfirmCallback 接口，消息发送到 Broker 后触发回调，确认消息是否到达 Broker 服务器，也就是只确认是否正确到达 Exchange 中
        rabbitTemplate.setConfirmCallback(this);
        // 消息唯一ID(一般设置为业务唯一主键)
        CorrelationData correlationData = new CorrelationData(order.getMessageId());
        rabbitTemplate.convertAndSend("order-exchange", "order.ABC", JSON.toJSONString(order), correlationData);
    }
}
