package com.author.commons.utils;

import cn.hutool.core.text.UnicodeUtil;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/7/21 15:38
 * @Version: #1.0 Copyright ? 2020
 */
public final class Constants {
  public final static char ask = '\u002a';
  public final static char split = '\u003b';
  public final static char equals = '\u003d';
  public final static String million = "1000000";
  public final static String AUTHOR_URL = "https://developers.e.qq.com/oauth/authorize?client_id={0}&redirect_uri={1}&state={2}";
  public final static String DEFAULT_CMD = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";

  public enum log {
    NOT_LOGIN("-101185006", UnicodeUtil.toString("\\u767b\\u5f55\\u6001\\u6821\\u9a8c\\u5931\\u8d25"));

    private final String c;
    private final String m;

    log(String c, String m) {
      this.c = c;
      this.m = m;
    }

    public String c() {
      return c;
    }

    public String m() {
      return m;
    }
  }

  public final class redis {
    public final static int redis1str = 1;
    public final static int redis2set = 2;
    public final static int redis3hash = 3;
    public final static int redis4list = 4;
    public final static int redis5zset = 5;
    public final static String account_data = "game:account:data:{0}";
  }

  public final class uri {
    public final static String generalSituationUri = "https://q.qq.com/pb/GetGeneralSituation";
    public final static String retentionDataUri = "https://q.qq.com/pb/GetRetentionTrend";
    public final static String operationDataUri = "https://q.qq.com/pb/GetOperationData";
    public final static String referUvsUri = "https://q.qq.com/pb/GetReferUvs";
    public final static String adDataDailyUri = "https://q.qq.com/pb/GetAdDataDaily";
  }

  public enum gs {
    generalSituation, dataType, number;
  }

  public enum rd {
    retentionDatas, calculate_type, real_time_begin, real_time_end, date_type, data_value;
  }

  public enum od {
    retainDatas, accessDatas, ftimeBegin, ftimeEnd, uvTime;
  }

  public enum ru {
    referUvs, ftimeBegin, ftimeEnd, order_type, time_tag, refer, uv;
  }

  public enum ad {
    AdDataDailyList, adType, ftimeBegin, ftimeEnd, needSubPosData, revenue, channelType;
  }
}
