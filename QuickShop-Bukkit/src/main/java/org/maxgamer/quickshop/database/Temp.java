//package org.maxgamer.quickshop.database;
//
//import cc.carm.lib.easysql.EasySQL;
//import cc.carm.lib.easysql.api.SQLManager;
//import cc.carm.lib.easysql.hikari.HikariConfig;
//
//public class Temp {
//    public static void a() {
//        HikariConfig hikari = YOUR_HIKARI_CONFIG;
//        SQLManager sqlManager = EasySQL.createManager(hikari);
//        sqlManager.createInsert("table_name")
//                .setColumnNames("name", "sex", "age")
//                .setParams("Alex","female",16)
//                .executeAsync();
//    }
//}
