package com.thoughtworks.ebikecharger;


import static com.thoughtworks.ebikecharger.Constants.HOUR_AS_MILLIS;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Charger implements Runnable {

  private static final long FULL_CHARGE_TIME = 8; // as hours

  // 插入电源的时间
  private final AtomicLong pluggedInTime = new AtomicLong();

  private final AtomicBoolean isPlugged = new AtomicBoolean(false);

  private final Object lock = new Object();

  public void plugIn() {
    synchronized (lock) {
      isPlugged.set(true);
      pluggedInTime.set(System.currentTimeMillis());
      sendPlugInEvent();
    }
  }

  public void plugOut() {
    if (isPlugged()) {
      synchronized (lock) {
        isPlugged.set(false);
        sendPlugOutEvent();
      }
    }
  }


  @Override
  public void run() {
    while (true) {
      if (isPlugged()) {
        synchronized (lock) {
          try (Socket socket = new Socket(InetAddress.getByName(Constants.INET_ADDRESS), Constants.PORT)) {
            try (OutputStream outputStream = socket.getOutputStream()) {
              try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
                String energyKnostJson = JSON.toJSONString(generateEnergyKnots(System.currentTimeMillis(), pluggedInTime.get()));
                RequestBody requestBody = RequestBody.post("/charger/energyKnots",energyKnostJson);
                objectOutputStream.writeObject(requestBody);
                objectOutputStream.flush();
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
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
    try (Socket socket = new Socket(InetAddress.getByName(Constants.INET_ADDRESS), Constants.PORT)) {
      try (OutputStream outputStream = socket.getOutputStream()) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
          System.out.println("[Charger日志][充电器]：检测到电源插入");

          RequestBody requestBody = RequestBody.post("/charger/status", "plugIn");

          objectOutputStream.writeObject(requestBody);
          objectOutputStream.flush();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendPlugOutEvent() {
    try (Socket socket = new Socket(InetAddress.getByName(Constants.INET_ADDRESS), Constants.PORT)) {
      try (OutputStream outputStream = socket.getOutputStream()) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
          System.out.println("[Charger日志][充电器]：检测到电源拔出");

          RequestBody requestBody = RequestBody.post("/charger/status", "plugOut");

          objectOutputStream.writeObject(requestBody);
          objectOutputStream.flush();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isPlugged() {
    return isPlugged.get();
  }

}