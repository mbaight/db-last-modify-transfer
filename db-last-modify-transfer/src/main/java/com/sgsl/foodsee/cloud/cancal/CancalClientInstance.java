package com.sgsl.foodsee.cloud.cancal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;

import javax.validation.constraints.NotNull;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/24
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/24
 * Description:
 */

@Slf4j
public class CancalClientInstance implements ApplicationListener<ContextClosedEvent> {

    private final CancalClientProperties cancalClientProperties;
    private final CanalConnector connector;

    private final MongoTemplate mongoTemplate;

    private long emptyCount = 0;

    public CancalClientInstance(CancalClientProperties cancalClientProperties, MongoTemplate mongoTemplate) {
        this.cancalClientProperties = cancalClientProperties;
        this.mongoTemplate = mongoTemplate;

        if (cancalClientProperties.isCluster()) {
            // 基于zookeeper动态获取canal server的地址，建立链接，其中一台server发生crash，可以支持 failover
            CancalClientProperties.ClusterProperties clusterProperties = cancalClientProperties.getClusterProperties();
            Validate.notNull(clusterProperties);
            Validate.notEmpty(clusterProperties.getZkAddress());
            this.connector = CanalConnectors.newClusterConnector(
                    clusterProperties.getZkAddress(),
                    cancalClientProperties.getDstination(),
                    clusterProperties.getUserName(),
                    clusterProperties.getPassword());
        } else {
            // 创建单机canal链接
            CancalClientProperties.SimpleProperties simpleProperties = cancalClientProperties.getSimpleProperties();
            Validate.notNull(simpleProperties);
            Validate.notNull(simpleProperties.getCanalServerIp());
            Validate.notNull(simpleProperties.getCanalServerPort());

            InetSocketAddress inetSocketAddress = new InetSocketAddress(
                    simpleProperties.getCanalServerIp(),
                    simpleProperties.getCanalServerPort()
            );
            this.connector = CanalConnectors.newSingleConnector(
                    inetSocketAddress,
                    cancalClientProperties.getDstination(),
                    simpleProperties.getUserName(),
                    simpleProperties.getPassword()
            );
        }

        connector.connect();
        connector.subscribe(".*\\..*");


//        String dbConnectorHistoryKey = getDbConnectorHistoryKey();
//        Query query = new Query(Criteria.where(DbConnectorHistory.INDEX_KEY).is(dbConnectorHistoryKey));
//        DbConnectorHistory dbConnectorHistory = mongoTemplate.findOne(query, DbConnectorHistory.class);

//        if (Objects.nonNull(dbConnectorHistory)) {
//            Long lastBatchId = dbConnectorHistory.getLastBatchId();
//            if (lastBatchId > 0) {
//                connector.rollback(lastBatchId);
//            }
//        }

        log.info("started CancalClientInstance");
    }

    private String getDbConnectorHistoryKey() {
        return getClass().getName() + ".DbConnectorHistory";
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {
        if (!connector.checkValid()) {
            return;
        }

        Message message = connector.getWithoutAck(cancalClientProperties.getBatchSize()); // 获取指定数量的数据
        long batchId = message.getId();
        try {
            int size = message.getEntries().size();
            emptyCount++;
            if (batchId == -1 || size == 0) {
                log.debug("empty count : {}", emptyCount);
            } else {
                emptyCount = 0;
                if (log.isDebugEnabled()) {
                    log.debug("message[batchId={},size={}] ", batchId, size);
                    printEntry(message.getEntries());
                }

                saveLastModifyDatas(message.getEntries());

                mongoTemplate.upsert(
                        Query.query(Criteria.where(DbConnectorHistory.INDEX_KEY).is(getDbConnectorHistoryKey())),
                        Update.update(DbConnectorHistory.LASTBATCHID_KEY, batchId),
                        DbConnectorHistory.class
                );

                connector.ack(batchId); // 提交确认
            }
        } catch (Exception e) {
            connector.rollback(batchId); // 处理失败, 回滚数据
            log.error("process data failed with {}", e.getMessage(), e);
        }
    }

    private void saveLastModifyDatas(@NonNull List<CanalEntry.Entry> entries) {
        entries.forEach(entry -> {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN ||
                    entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                return;
            }

            try {
                CanalEntry.RowChange rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                CanalEntry.Header header = entry.getHeader();

                String dbName = header.getSchemaName();

                String serviceName = cancalClientProperties.findServiceNameFromDbName(dbName);
                if (StringUtils.isEmpty(serviceName)) {
                    log.warn("not save lastmodifydatas because not found service name. dbName:{}", dbName);
                    return;
                }

                String tableName = header.getTableName();
                long executeTime = header.getExecuteTime();

                for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                    Long userId = getUserIdFromColumns(rowData.getBeforeColumnsList());

                    String id = String.format("%s_%s_%s_%d",
                            serviceName,
                            dbName,
                            tableName,
                            userId
                    );
                    mongoTemplate.upsert(
                            Query.query(Criteria.where("_id").is(id)),
                            Update.update("_id", id)
                                    .set("serviceName", serviceName)
                                    .set("dbName", dbName)
                                    .set("tableName", tableName)
                                    .set("userId", userId)
                                    .set("lastUpdateTime", executeTime),
                            LastModifyData.class
                    );
                }

            } catch (Exception e) {
                throw new CanalProcessException(String.format("ERROR ## parser of eromanga-event has an error message:%s, data:%s",
                        e.getMessage(),
                        entry.toString()),
                        e);
            }
        });
    }

    private void printEntry(@NotNull List<CanalEntry.Entry> entrys) {
        if (!log.isDebugEnabled()) {
            return;
        }

        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN ||
                    entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            try {
                CanalEntry.RowChange rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                CanalEntry.EventType eventType = rowChage.getEventType();
                CanalEntry.Header header = entry.getHeader();

                log.debug("================> binlog[{}:{}] , name[{},{}] , executeTime:{}  eventType : {}",
                        header.getLogfileName(),
                        header.getLogfileOffset(),
                        header.getSchemaName(),
                        header.getTableName(),
                        header.getExecuteTime(),
                        eventType);

                for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                    if (eventType == CanalEntry.EventType.DELETE) {
                        printColumn(rowData.getBeforeColumnsList());
                    } else if (eventType == CanalEntry.EventType.INSERT) {
                        printColumn(rowData.getAfterColumnsList());
                    } else {
                        log.debug("-------> before");
                        printColumn(rowData.getBeforeColumnsList());
                        log.debug("-------> after");
                        printColumn(rowData.getAfterColumnsList());
                    }
                }
            } catch (Exception e) {
                throw new CanalProcessException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

        }
    }


    private void printColumn(@NotNull List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            String value = column.getValue();
            log.debug("{} : {}    update = {}",
                    column.getName(),
                    value,
                    column.getUpdated()
            );
        }
    }

    private Long getUserIdFromColumns(@NotNull List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            if (cancalClientProperties.getUserIdColumnNames().contains(column.getName())) {
                try {
                    return Long.valueOf(column.getValue());
                } catch (NumberFormatException e) {
                    log.error("convert userId error from column:{} ", column.getName(), e);
                }
            }
        }

        return 0L;
    }


    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        connector.unsubscribe();
        connector.disconnect();
    }
}
