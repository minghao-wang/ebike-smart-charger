package com.thoughtworks.ebikecharger;

public class MainWorld {

  public static final long HOUR_AS_MILLIS = 100;

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
      li.checkBike();
    }).start();
    new Thread(() -> {
      try {
        Thread.sleep((long) (0.5 * HOUR_AS_MILLIS));
      } catch (InterruptedException ignore) {
      }
      System.out.println("半小时后，小刘结束了晨练，检查电动车状态");
      liu.checkBike();
    }).start();
    System.out.println("环境将在30个小时后关闭");
    Thread.sleep(30 * HOUR_AS_MILLIS);
    System.exit(0);
  }

}