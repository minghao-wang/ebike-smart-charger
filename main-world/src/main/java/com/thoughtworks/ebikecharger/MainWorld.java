package com.thoughtworks.ebikecharger;

import static com.thoughtworks.ebikecharger.Constants.HOUR_AS_MILLIS;

public class MainWorld {


  public static void main(String[] args) throws InterruptedException {
    Server server = new Server();
    new Thread(server).start();
    Charger charger = new Charger();
    new Thread(charger).start();
    charger.plugIn();
    System.out.println("环境初始化完成，电动车开始充电");
    Thread.sleep(9 * HOUR_AS_MILLIS);
    System.out.println("9小时后，电动车完成充电");
    MobileApp zhang = new MobileApp("小张");
    MobileApp li = new MobileApp("小李");
    MobileApp liu = new MobileApp("小刘");
    new Thread(() -> {
      System.out.println("小张骑走了电动车");
      charger.plugOut();
      zhang.reportBorrower();
    }).start();
    new Thread(() -> {
      try {
        Thread.sleep((long) (1.5 * HOUR_AS_MILLIS));
      } catch (InterruptedException ignore) {
      }
      System.out.println("一个半小时后，小李起床检查电动车状态");
      long l = System.currentTimeMillis();
      li.checkBike();
      if (System.currentTimeMillis() - l > 150) {
        System.out.println("小李：我等到花都谢了");
      }
    }).start();
    new Thread(() -> {
      try {
        Thread.sleep((long) (1.5 * HOUR_AS_MILLIS));
      } catch (InterruptedException ignore) {
      }
      System.out.println("一个半小时后，小刘结束了晨练，检查电动车状态");
      long l = System.currentTimeMillis();
      liu.checkBike();
      if (System.currentTimeMillis() - l > 150) {
        System.out.println("小刘：我等到花都谢了");
      }
    }).start();
    System.out.println("环境将在30个小时后关闭");
    Thread.sleep(30 * HOUR_AS_MILLIS);
    System.exit(0);
  }

}