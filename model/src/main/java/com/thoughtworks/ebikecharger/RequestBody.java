package com.thoughtworks.ebikecharger;

import java.io.Serializable;

public class RequestBody implements Serializable {

  private final long serialVersionUUID = 1L;

  private String method;

  private String url;

  private String params;

  public static RequestBody get(String url) {
    RequestBody requestBody = new RequestBody();
    requestBody.setMethod(Constants.GET_METHOD);
    requestBody.setUrl(url);
    return requestBody;
  }

  public static RequestBody post(String url, String params) {
    RequestBody requestBody = new RequestBody();
    requestBody.setMethod(Constants.POST_METHOD);
    requestBody.setUrl(url);
    requestBody.setParams(params);
    return requestBody;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
