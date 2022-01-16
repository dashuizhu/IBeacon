package com.zby.ibeacon.database;

import com.zby.corelib.DeviceBean;
import com.zby.corelib.LogUtils;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author zhuj 2018/4/8 下午5:47.
 */
public class DataDao extends RealmObject {

    private final static String TAG = DataDao.class.getSimpleName();

    private final static String COLUMN_MAC     = "mac";
    private final static String COLUMN_IS_SAVE = "isSave";

    @PrimaryKey private String  id;
    private             String  name;
    private             long    time;
    private             String  mac;
    private             int     nowStep;
    private             int     saveStep;
    private             boolean isSave;

    public static void saveOrUpdate(final DeviceBean deviceBean, final int nowStep) {
        Realm realm = Realm.getDefaultInstance();
        DataDao dao = realm.where(DataDao.class)
                .equalTo(COLUMN_MAC, deviceBean.getMac())
                .equalTo(COLUMN_IS_SAVE, false)
                .findFirst();
        if (dao == null) {

            Number number = realm.where(DataDao.class)
                    .equalTo(COLUMN_MAC, deviceBean.getMac())
                    .equalTo(COLUMN_IS_SAVE, true)
                    .max("nowStep");

            dao = new DataDao();
            dao.id = UUID.randomUUID().toString();
            dao.name = deviceBean.getName();
            dao.time = System.currentTimeMillis();
            dao.mac = deviceBean.getMac();
            if (number == null) {
                dao.saveStep = 0;
            } else {
                dao.saveStep = number.intValue();
            }
            dao.nowStep = nowStep;
            dao.isSave = false;
        } else {
            dao.time = System.currentTimeMillis();
            dao.nowStep = nowStep;
        }
        realm.copyToRealmOrUpdate(dao);
        LogUtils.writeLogToFile(TAG, deviceBean.getMac() + " save step " + dao.saveStep + " - "+ nowStep);
    }

    public static void saveClean(final String mac) {
        Realm realm = Realm.getDefaultInstance();
        DataDao dao = realm.where(DataDao.class)
                .equalTo(COLUMN_MAC, mac)
                .equalTo(COLUMN_IS_SAVE, false)
                .findFirst();
        if (dao != null) {
            dao.isSave = true;
            realm.copyToRealmOrUpdate(dao);
            LogUtils.writeLogToFile(TAG, mac + " saved! ");
        }
    }

    private static DataDao castDao(DataBean bean) {
        DataDao dao = new DataDao();
        dao.name = bean.getName();
        dao.time = bean.getTime();
        dao.mac = bean.getMac();
        dao.isSave = bean.isSave();
        dao.nowStep = bean.getNowStep();
        dao.saveStep = bean.getSaveStep();
        dao.id = bean.getId();
        return dao;
    }

    public DataBean castBean() {
        DataBean bean = new DataBean();
        bean.setId(this.id);
        bean.setMac(this.mac);
        bean.setTime(this.time);
        bean.setName(this.name);
        bean.setSave(this.isSave);
        bean.setSaveStep(this.saveStep);
        bean.setNowStep(this.nowStep);
        return bean;
    }

    /**
     * 查询所有记录,倒序查
     */
    public static List<DataBean> queryList(String mac) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<DataDao> results =
                realm.where(DataDao.class).equalTo(COLUMN_MAC, mac).findAllSorted("time", Sort.DESCENDING);
        realm.commitTransaction();
        List<DataBean> list = new ArrayList<>();
        if (results == null || !results.isValid()) {
            return list;
        }
        //因为是倒序查询的， add(0
        for (DataDao dao : results) {
            list.add(dao.castBean());
        }
        return list;
    }

    /**
     * 查询所有记录,倒序查
     */
    public static List<DataBean> queryList() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<DataDao> results =
                realm.where(DataDao.class).findAllSorted("time",Sort.DESCENDING);
        List<DataBean> list = new ArrayList<>();
        if (results == null || !results.isValid()) {
            return list;
        }
        //因为是倒序查询的， add(0
        for (DataDao dao : results) {
            list.add(dao.castBean());
        }
        return list;
    }

    public static DataBean queryNowData(String mac) {
      Realm realm = Realm.getDefaultInstance();
      realm.beginTransaction();
      DataDao dao = realm.where(DataDao.class)
              .equalTo(COLUMN_MAC, mac)
              .equalTo(COLUMN_IS_SAVE, false)
              .findFirst();
      if (dao == null) {
         DataBean dataBean = new DataBean();
         return dataBean;
      }
      return dao.castBean();
    }
}
