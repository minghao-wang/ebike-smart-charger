package com.thoughtworks.ebikecharger;

import static com.thoughtworks.ebikecharger.Constants.HOUR_AS_MILLIS;

import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server implements Runnable {

  ServerSocket serverSocket = new ServerSocket(Constants.PORT);

  private boolean electricityStatus = false;

  private String borrower = "";

  public Server() throws IOException {
  }

  @Override
  public void run() {
    while (true) {
      Socket accept;
      try {
        accept = serverSocket.accept();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      new Thread(new Handler(accept)).start();
    }
  }

  class Handler implements Runnable {

    private final Socket acceptSocket;


    private final ReadWriteLock electricityReadWriteLock = new ReentrantReadWriteLock();

    private final ReadWriteLock borrowerReadWriteLock = new ReentrantReadWriteLock();

    public Handler(Socket acceptSocket) {
      this.acceptSocket = acceptSocket;
    }

    @Override
    public void run() {
      try (InputStream inputStream = acceptSocket.getInputStream()) {
        try (OutputStream outputStream = acceptSocket.getOutputStream()) {
          try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            handleRequest(outputStream, objectInputStream);
          }
        } catch (ClassNotFoundException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      try {
        acceptSocket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void handleRequest(OutputStream outputStream, ObjectInputStream objectInputStream)
        throws IOException, ClassNotFoundException, InterruptedException {
      RequestBody requestBody = (RequestBody) objectInputStream.readObject();
      String method = requestBody.getMethod();
      String url = requestBody.getUrl();
      String[] split = url.split("/");
      if (split[1].equals("charger")) {
        if (split[2].equals("status")) {
          // 对应get /charger/status的逻辑分支
          boolean plugIn = "plugIn".equals(requestBody.getParams());
          receivePlugEvent(plugIn);
        } else if (split[2].equals("energyKnots")) {
          // 对应post /charger/energyKnots的逻辑分支
          List<Integer> energyKnots = JSON.parseArray(requestBody.getParams(), Integer.class);
          receiveEnergyKnots(energyKnots);
        }
      } else if (split[1].equals("bike")) {
        if (method.equals(Constants.GET_METHOD)) {
          // 对应get /bike/status的逻辑分支
          checkBikeStatus(new ObjectOutputStream(outputStream));
          Thread.sleep(3 * HOUR_AS_MILLIS);
        } else if (method.equals(Constants.POST_METHOD)) {
          // 对应post /bike/status的逻辑分支
          String username = requestBody.getParams();
          receiveBorrower(username);
        }
      }
    }

    public void receivePlugEvent(boolean plugIn) {
      electricityReadWriteLock.writeLock().lock();
      electricityStatus = plugIn;
      electricityReadWriteLock.writeLock().unlock();
      electricityReadWriteLock.readLock().lock();
      if (electricityStatus) {
        System.out.println("[Server日志][电动车]：进入充电状态");
      } else {
        System.out.println("[Server日志][电动车]：解除充电状态");
      }
      electricityReadWriteLock.readLock().unlock();
    }

    private void receiveEnergyKnots(List<Integer> energyKnots) {
      electricityReadWriteLock.readLock().lock();
      if (electricityStatus) {
        System.out.println("[Server日志][电动车]当前的充电功率曲线为:" + energyKnots.toString());
      }
      electricityReadWriteLock.readLock().unlock();
    }

    private void checkBikeStatus(ObjectOutputStream objectOutputStream) throws IOException {
      StringBuilder bikeStatus = new StringBuilder();
      electricityReadWriteLock.readLock().lock();
      if (electricityStatus) {
        bikeStatus.append("电动车正在充电");
      } else {
        bikeStatus.append("电动车未处于充电状态");
      }
      electricityReadWriteLock.readLock().unlock();
      bikeStatus.append(",");
      borrowerReadWriteLock.readLock().lock();
      if (borrower.length() == 0) {
        bikeStatus.append("目前电动车处于闲置状态");
      } else {
        bikeStatus.append(String.format("目前%s正在使用电动车", borrower));
      }
      borrowerReadWriteLock.readLock().unlock();
      objectOutputStream.writeObject(bikeStatus.toString());
    }

    public void receiveBorrower(String username) {
      borrowerReadWriteLock.writeLock().lock();
      System.out.printf("已经上报%s\n", username);
      borrower = username;
      borrowerReadWriteLock.writeLock().unlock();
    }
  }
}
