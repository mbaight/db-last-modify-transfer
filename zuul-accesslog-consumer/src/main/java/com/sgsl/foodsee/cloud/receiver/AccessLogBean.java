package com.sgsl.foodsee.cloud.receiver;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author liuyo on 17.9.30.
 */
@Data
@ToString
@Document(collection = AccessLogReceiver.MONGODB_TABLE_NAME)
public class AccessLogBean implements Serializable {
    private String clientIp;
    private String userAgent;
    private String accessUri;
    private Map<String, Object> parameters;
    private Integer responseStatus;
    private String method;
    @Indexed(expireAfterSeconds = AccessLogReceiver.DEFAULT_EXPIRE_SECOND)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", locale = "zh", timezone = "GMT+8")
    private Date accessTime;
}
