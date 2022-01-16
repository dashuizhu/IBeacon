package com.zby.ibeacon.database;

import lombok.Data;

/**
 * @author zhuj 2018/4/8 上午11:42.
 */
@Data
public class DataBean {

  String id;
  private long    time;
  private String  mac;
  private String  name;
  private boolean isSave;
  int nowStep;
  int saveStep;
}
