package com.zby.ibeacon.database;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

/**
 * Created by zhuj on 17/1/3.
 */
public class BuddyRealm {

  private final static String TAG = BuddyRealm.class.getSimpleName();

  final static int REALM_VERSION = 1;

  /**
   * @param userId 用户ID
   * @brief 为每个帐号创建一个特有的 Realm 文件, 必须在子线程同步插入、或主线程异步插入，
   * 主线程查询
   * 子线程查询更改，用 realm.executeTransaction
   */
  static public void setDefaultRealmForUser(String userId) {
    if (userId == null || userId.isEmpty()) {
      return;
    }
    RealmConfiguration config = new RealmConfiguration.Builder().name(userId)
            .schemaVersion(REALM_VERSION)
            //.migration(new CustomRealmMigration())
            //migration 与 delete 有冲突， 会优先执行delete
            .deleteRealmIfMigrationNeeded()
            .build();
    Realm.setDefaultConfiguration(config);
    ////android debug模式下， chrome浏览器查看数据库 chrome://inspect/#devices
    //if (BuildConfig.DEBUG) {
    //com.facebook.stetho.Stetho.initialize(
    //    com.facebook.stetho.Stetho.newInitializerBuilder(AppApplication.getAppContext())
    //        .enableDumpapp(com.facebook.stetho.Stetho.defaultDumperPluginsProvider(AppApplication.getAppContext()))
    //        .enableWebKitInspector(com.uphyca.stetho_realm.RealmInspectorModulesProvider.builder(AppApplication.getAppContext())
    //            .databaseNamePattern(Pattern.compile(userId))
    //            .build())
    //        .build());
    //}
  }

  //数据库版本变迁
  static class CustomRealmMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof CustomRealmMigration;
    }

    @Override
    public int hashCode() {
      return REALM_VERSION;
    }
  }
}
