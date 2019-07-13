package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

/**
 * 监听类 用于生成网页
 */
@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            for (Long goodsId : goodsIds) {
                System.out.println("接受到消息:"+goodsId);
                boolean b = itemPageService.genItemHtml(goodsId);
                System.out.println("网页生成结果:"+b);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
