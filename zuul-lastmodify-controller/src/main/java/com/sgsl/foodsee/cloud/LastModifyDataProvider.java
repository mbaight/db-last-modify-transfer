package com.sgsl.foodsee.cloud;

import java.util.List;

/**
 * Created by Administrator .
 * create_time: 2017/11/14 0014
 * modify_time: 2017/11/14 0014
 */
public interface LastModifyDataProvider {
    List<LastModifyData> findLastModifyDatas(List<String> tableNameKeys);
}
