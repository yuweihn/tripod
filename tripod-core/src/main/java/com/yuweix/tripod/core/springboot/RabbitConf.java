package com.yuweix.tripod.core.springboot;


import com.yuweix.tripod.core.SpringContext;
import com.yuweix.tripod.core.mq.rabbit.BindingSetting;
import com.yuweix.tripod.core.mq.rabbit.DefaultRabbitSender;
import com.yuweix.tripod.core.mq.rabbit.CfmCallback;
import com.yuweix.tripod.core.mq.rabbit.RabbitSender;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author yuwei
 */
public class RabbitConf {
    @ConditionalOnMissingBean(BindingSetting.class)
    @Bean
    @ConfigurationProperties(prefix = "tripod.rabbit.setting", ignoreUnknownFields = true)
    public BindingSetting bindingSetting() {
        return new BindingSetting() {
            private List<Item> itemList = new ArrayList<>();

            @Override
            public List<Item> getBindings() {
                return itemList;
            }
        };
    }

    @ConditionalOnMissingBean(name = "rabbitBinding")
    @Bean("rabbitBinding")
    public Object rabbitBinding(SpringContext springContext, BindingSetting data) {
        Object obj = new Object();
        List<BindingSetting.Item> bindings = data.getBindings();
        if (bindings == null || bindings.isEmpty()) {
            return obj;
        }
        for (int i = 0, sz = bindings.size(); i < sz; i++) {
            BindingSetting.Item item = bindings.get(i);
            Queue queue = new Queue(item.getQueue(), true);
            SpringContext.register(queue.getName(), queue);

            Exchange exchange = new DirectExchange(item.getExchange(), true, false);
            SpringContext.register(exchange.getName(), exchange);

            Binding bd = BindingBuilder.bind(queue).to(exchange).with(item.getRouteKey()).noargs();
            SpringContext.register("rabbitBinding" + i, bd);
        }
        return obj;
    }

    @ConditionalOnMissingBean(name = "rabbitJsonMessageConverter")
    @Bean("rabbitJsonMessageConverter")
    public MessageConverter rabbitJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @ConditionalOnMissingBean(name = "rabbitRetryTemplate")
    @Bean("rabbitRetryTemplate")
    public RetryTemplate rabbitRetryTemplate(@Value("${tripod.rabbit.retry.maxAttempts:3}") int maxAttempts
            , @Value("${tripod.rabbit.retry.backOffPeriod:2000}") long backOffPeriod) {
        RetryTemplate retryTemplate = new RetryTemplate();
        // 设置重试策略：重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);
        // 设置退避策略：每次重试间隔2秒
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(backOffPeriod);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    @ConditionalOnMissingBean(CfmCallback.class)
    @Bean
    public CfmCallback cfmCallback() {
        return new CfmCallback() {
            @Override
            public void call(CorrelationData correlationData, boolean ack, String cause) {
                //TODO
            }
        };
    }

    @ConditionalOnMissingBean(RabbitTemplate.class)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory
            , @Qualifier("rabbitRetryTemplate") RetryTemplate retryTemplate
            , @Qualifier("rabbitJsonMessageConverter") MessageConverter rabbitJsonMessageConverter
            , CfmCallback cfmCallback) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(rabbitJsonMessageConverter);
        template.setRetryTemplate(retryTemplate);
        template.setConfirmCallback(cfmCallback::call);
        return template;
    }

    @ConditionalOnMissingBean(RabbitSender.class)
    @Bean
    public RabbitSender rabbitSender(RabbitTemplate rabbitTemplate) {
        return new DefaultRabbitSender(rabbitTemplate);
    }
}
