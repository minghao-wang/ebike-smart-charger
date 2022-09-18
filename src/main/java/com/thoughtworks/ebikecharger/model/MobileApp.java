package com.thoughtworks.ebikecharger.model;

public class MobileApp {

  private final String username;

  public MobileApp(String username) {
    this.username = username;
  }

  public void checkBike(Server server) {
    String threadName = Thread.currentThread().getName();
    System.out.printf("[%s]%s正在检查电动车状态：%s\n", threadName, username, server.checkBikeStatus());
  }

}
