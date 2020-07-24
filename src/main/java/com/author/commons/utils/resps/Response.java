package com.author.commons.utils.resps;

import com.author.commons.utils.Result;
import com.author.commons.utils.enums.ResultCode;

public class Response {
  private static final String DEFAULT_SUCCESS_MSG = "SUCCESS";

  public static <T> Result<? extends T> genSuccessResult() {
    return new Result<T>(ResultCode.SUCCESS.code(), DEFAULT_SUCCESS_MSG);
  }

  public static <T> Result<T> genSuccessResult(T data) {
    return new Result<T>(ResultCode.SUCCESS.code(), DEFAULT_SUCCESS_MSG, data);
  }

  public static <T> Result<T> genFailResult(String message) {
    return new Result<T>(ResultCode.FAIL.code(), message);
  }
}
