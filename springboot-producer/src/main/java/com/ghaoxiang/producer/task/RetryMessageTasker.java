package com.ghaoxiang.producer.task;

import com.ghaoxiang.entity.BrokerMessageLog;
import com.ghaoxiang.entity.Order;
import com.ghaoxiang.producer.constant.Constants;
import com.ghaoxiang.producer.dao.BrokerMessageLogMapper;
import com.ghaoxiang.producer.sender.RabbitOrderSender;
import com.ghaoxiang.producer.utils.FastJsonConvertUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class RetryMessageTasker {


    @Autowired
    private RabbitOrderSender rabbitOrderSender;

    @Autowired
    private BrokerMessageLogMapper brokerMessageLogMapper;

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void reSend(){
        // pull status = 0 and timeout message
        List<BrokerMessageLog> list = brokerMessageLogMapper.query4StatusAndTimeoutMessage();
        list.forEach(messageLog -> {
            System.out.println("-----------定时任务开始-----------");
            if(messageLog.getTryCount() >= 3){
                // update fail message
                brokerMessageLogMapper.changeBrokerMessageLogStatus(messageLog.getMessageId(), Constants.ORDER_SEND_FAILURE, new Date());
            } else {
                // resend
                brokerMessageLogMapper.update4ReSend(messageLog.getMessageId(),  new Date());
                Order reSendOrder = FastJsonConvertUtil.convertJSONToObject(messageLog.getMessage(), Order.class);
                try {
                    rabbitOrderSender.sendOrder(reSendOrder);
                    System.out.println("-----------重新发送mq-----------");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("-----------异常处理-----------");
                }
            }
        });
    }
}

