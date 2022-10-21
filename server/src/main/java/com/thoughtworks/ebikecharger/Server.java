package com.thoughtworks.ebikecharger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

  ServerSocket serverSocket = new ServerSocket(9090);
  public Server() throws IOException {
  }

  @Override
  public void run() {
    while(true) {
      Socket accept;
      try {
        accept = serverSocket.accept();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      new Thread(new Handler(accept)).start();
    }
  }
}
