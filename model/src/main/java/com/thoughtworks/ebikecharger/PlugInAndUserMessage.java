package com.thoughtworks.ebikecharger;

import java.io.Serializable;

public class PlugInAndUserMessage extends Message implements Serializable {

  private String username;

  private Boolean plugIn;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Boolean getPlugIn() {
    return plugIn;
  }

  public void setPlugIn(Boolean plugIn) {
    this.plugIn = plugIn;
  }

  @Override
  public String toString() {
    return "PlugInAndUserMessage{" +
        "username='" + username + '\'' +
        ", plugIn=" + plugIn +
        '}';
  }
}
