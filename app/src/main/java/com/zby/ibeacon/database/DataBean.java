package com.zby.ibeacon.database;

import com.zby.ibeacon.utils.ExcelUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author zhuj 2018/4/8 上午11:42.
 */
@Data
public class DataBean {

  String id;
  public long    time;
  public String  mac;
  public String  name;
  public boolean isSave;
  int nowStep;
  int saveStep;

  public List<String> getStringList() {
    return Arrays.asList(new String[] { mac, String.valueOf(nowStep - saveStep), String.valueOf(saveStep), String.valueOf(nowStep), ExcelUtil.mSdf.format(new Date(time))});
  }
}
