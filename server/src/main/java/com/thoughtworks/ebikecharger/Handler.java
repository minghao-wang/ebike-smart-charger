package com.thoughtworks.ebikecharger;

import static com.thoughtworks.ebikecharger.Constants.HOUR_AS_MILLIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class Handler implements Runnable{

  private final Socket accept;

  private final Object lock = new Object();
  private static boolean electricityStatus = false;
  private static String borrower = "";

  public Handler(Socket accept) {
    this.accept = accept;
  }

  @Override
  public void run() {
    try (InputStream inputStream = accept.getInputStream()) {
      try (OutputStream outputStream = accept.getOutputStream()) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
          Message message = (Message) objectInputStream.readObject();
          String flag = message.getFlag();
          if ("checkBike".equals(flag)) {
            checkBikeStatus(new ObjectOutputStream(outputStream));
            Thread.sleep(3 * HOUR_AS_MILLIS);
          } else if ("energyKnots".equals(flag)) {
            EnergyMessage energyMessage = (EnergyMessage) message;
            receiveEnergyKnots(energyMessage.getEnergyKnots());
          } else if ("plugIn".equals(flag)) {
            PlugInAndUserMessage plugInAndUserMessage = (PlugInAndUserMessage) message;
            receivePlugEvent(plugInAndUserMessage.getPlugIn());
          } else if ("reportBorrower".equals(flag)) {
            PlugInAndUserMessage plugInAndUserMessage = (PlugInAndUserMessage) message;
            receiveBorrower(plugInAndUserMessage.getUsername());
          }
        }
      } catch (ClassNotFoundException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void receivePlugEvent(boolean plugIn) {
    electricityStatus = plugIn;
    if (electricityStatus) {
      System.out.println("[Server日志][电动车]：进入充电状态");
    } else {
      System.out.println("[Server日志][电动车]：解除充电状态");
    }
  }

  private void receiveEnergyKnots(List<Integer> energyKnots) {
    if (electricityStatus) {
      System.out.println("[Server日志][电动车]当前的充电功率曲线为:" + energyKnots.toString());
    }
  }

  private void checkBikeStatus(ObjectOutputStream objectOutputStream) throws IOException {
    StringBuilder res = new StringBuilder();
    if (electricityStatus) {
      res.append("电动车正在充电");
    } else {
      res.append("电动车未处于充电状态");
    }
    res.append(",");
    synchronized (lock) {
      if (borrower.length() == 0) {
        res.append("目前电动车处于闲置状态");
      } else {
        res.append(String.format("目前%s正在使用电动车", borrower));
      }
    }
    objectOutputStream.writeObject(res.toString());
  }

  public void receiveBorrower(String username) {
    synchronized (lock) {
      System.out.printf("已经上报%s\n", username);
      borrower = username;
    }
  }
}
