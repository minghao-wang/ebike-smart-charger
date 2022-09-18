package com.thoughtworks.ebikecharger.model;

import static com.thoughtworks.ebikecharger.MainWorld.HOUR_AS_MILLIS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Charger implements Runnable {

  private static final long FULL_CHARGE_TIME = 8; // as hours

  private final AtomicLong pluggedInTime = new AtomicLong();

  private final AtomicReference<MobileApp> borrower = new AtomicReference<>();

  private final Server server;

  public Charger(Server server) {
    this.server = server;
  }

  public void plugIn() {
    borrower.set(null);
    pluggedInTime.set(System.currentTimeMillis());
    sendPlugInEvent();
  }

  public void plugOut(MobileApp borrower) {
    this.borrower.set(borrower);
    sendPlugOutEvent();
  }

  @Override
  public void run() {
    while (true) {
      if (isPlugged()) {
        server.receiveEnergyKnots(generateEnergyKnots(System.currentTimeMillis(), pluggedInTime.get()));
      }
      try {
        Thread.sleep(HOUR_AS_MILLIS);
      } catch (InterruptedException ignore) {
      }
    }
  }

  protected List<Integer> generateEnergyKnots(long now, long from) {
    if ((now - from) / HOUR_AS_MILLIS >= FULL_CHARGE_TIME + 1) {
      return Collections.emptyList();
    }
    List<Integer> knots = new ArrayList<>(10);
    long start = now - HOUR_AS_MILLIS;
    long slice = HOUR_AS_MILLIS / 10;
    for (int i = 0; i <= 9; i++) {
      knots.add((start + slice * i - from) / HOUR_AS_MILLIS >= FULL_CHARGE_TIME ? 0 : 10);
    }
    return knots;
  }

  private void sendPlugInEvent() {
    System.out.println("[充电器]：检测到电源插入");
    server.receivePlugEvent(true);
  }

  private void sendPlugOutEvent() {
    System.out.println("[充电器]：检测到电源拔出");
    server.receivePlugEvent(false);
  }

  private boolean isPlugged() {
    return borrower.get() == null;
  }

}
