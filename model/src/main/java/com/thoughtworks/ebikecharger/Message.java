package com.thoughtworks.ebikecharger;

import java.io.Serializable;

public class Message implements Serializable {

  private String flag;

  public String getFlag() {
    return flag;
  }

  public void setFlag(String flag) {
    this.flag = flag;
  }
}
