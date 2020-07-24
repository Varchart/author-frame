package com.author.commons.utils;

import lombok.Data;

@Data
public class Result<T> {
  private int code;
  private String msg;
  private T result;

  public Result(int code) {
    this.code = code;
  }

  public Result(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public Result(int code, String msg, T result) {
    this.code = code;
    this.msg = msg;
    this.result = result;
  }
}
