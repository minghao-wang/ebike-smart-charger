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
          // ??????get /charger/status???????????????
          boolean plugIn = "plugIn".equals(requestBody.getParams());
          receivePlugEvent(plugIn);
        } else if (split[2].equals("energyKnots")) {
          // ??????post /charger/energyKnots???????????????
          List<Integer> energyKnots = JSON.parseArray(requestBody.getParams(), Integer.class);
          receiveEnergyKnots(energyKnots);
        }
      } else if (split[1].equals("bike")) {
        if (method.equals(Constants.GET_METHOD)) {
          // ??????get /bike/status???????????????
          checkBikeStatus(new ObjectOutputStream(outputStream));
          Thread.sleep(3 * HOUR_AS_MILLIS);
        } else if (method.equals(Constants.POST_METHOD)) {
          // ??????post /bike/status???????????????
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
        System.out.println("[Server??????][?????????]?????????????????????");
      } else {
        System.out.println("[Server??????][?????????]?????????????????????");
      }
      electricityReadWriteLock.readLock().unlock();
    }

    private void receiveEnergyKnots(List<Integer> energyKnots) {
      electricityReadWriteLock.readLock().lock();
      if (electricityStatus) {
        System.out.println("[Server??????][?????????]??????????????????????????????:" + energyKnots.toString());
      }
      electricityReadWriteLock.readLock().unlock();
    }

    private void checkBikeStatus(ObjectOutputStream objectOutputStream) throws IOException {
      StringBuilder bikeStatus = new StringBuilder();
      electricityReadWriteLock.readLock().lock();
      if (electricityStatus) {
        bikeStatus.append("?????????????????????");
      } else {
        bikeStatus.append("??????????????????????????????");
      }
      electricityReadWriteLock.readLock().unlock();
      bikeStatus.append(",");
      borrowerReadWriteLock.readLock().lock();
      if (borrower.length() == 0) {
        bikeStatus.append("?????????????????????????????????");
      } else {
        bikeStatus.append(String.format("??????%s?????????????????????", borrower));
      }
      borrowerReadWriteLock.readLock().unlock();
      objectOutputStream.writeObject(bikeStatus.toString());
    }

    public void receiveBorrower(String username) {
      borrowerReadWriteLock.writeLock().lock();
      System.out.printf("????????????%s\n", username);
      borrower = username;
      borrowerReadWriteLock.writeLock().unlock();
    }
  }
}
