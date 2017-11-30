package com.sgsl.foodsee.cloud.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * @author liuyo on 17.9.30.
 */
@Slf4j
public class AccessLogReceiver {

    private static final String QUEUE_NAME = "zuulAccessQueue";
    static final String MONGODB_TABLE_NAME = "t_sys_access_log";
    static final int DEFAULT_EXPIRE_SECOND = 604800;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MongoTemplate mongoTemplate;

    @Autowired
    public AccessLogReceiver(MongoTemplate mongoTemplate) {
        mongoTemplate.indexOps(MONGODB_TABLE_NAME).ensureIndex(new Index().on("accessTime", Sort.Direction.DESC).expire(DEFAULT_EXPIRE_SECOND));
        this.mongoTemplate = mongoTemplate;
    }

    @RabbitHandler
    @RabbitListener(queues = QUEUE_NAME)
    public void accessLog(String message) {
        try {
            AccessLogBean accessLog = objectMapper.readValue(message, AccessLogBean.class);
            mongoTemplate.insert(accessLog, MONGODB_TABLE_NAME);
            log.debug("saved access log: " + message);
        } catch (Exception e) {
            log.error("save access log - [" + message + "] error: " + e.getMessage(), e);
        }
    }
}