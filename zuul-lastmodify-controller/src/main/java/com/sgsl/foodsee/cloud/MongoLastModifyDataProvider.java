package com.sgsl.foodsee.cloud;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List; /**
 * Created by Administrator .
 * create_time: 2017/11/14 0014
 * modify_time: 2017/11/14 0014
 */
public class MongoLastModifyDataProvider implements LastModifyDataProvider{
    private final MongoTemplate mongoTemplate;

    public MongoLastModifyDataProvider(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<LastModifyData> findLastModifyDatas(List<String> tableNameKeys) {
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(tableNameKeys)), LastModifyData.class);
    }
}
