package com.thoughtworks.ebikecharger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class MobileApp {

  private final String username;


  public MobileApp(String username) {
    this.username = username;
  }

  public void checkBike() {
    synchronized (MobileApp.class) {
      try (Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 9090)) {
        try (InputStream inputStream = socket.getInputStream()) {
          try (OutputStream outputStream = socket.getOutputStream()) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
              Message message = new Message();
              message.setFlag("checkBike");
              objectOutputStream.writeObject(message);
              objectOutputStream.flush();
              try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                String threadName = Thread.currentThread().getName();
                String s = (String) objectInputStream.readObject();
                System.out.printf("[%s]%s正在检查电动车状态：%s\n", threadName, username, s);
              }
            }
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void reportBorrower() {
    synchronized (MobileApp.class) {
      try (Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 9090)) {
        try (OutputStream outputStream = socket.getOutputStream()) {
          try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            PlugInAndUserMessage plugInAndUserMessage = new PlugInAndUserMessage();
            plugInAndUserMessage.setFlag("reportBorrower");
            plugInAndUserMessage.setUsername(username);
            objectOutputStream.writeObject(plugInAndUserMessage);
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
}