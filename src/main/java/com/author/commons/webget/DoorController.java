package com.author.commons.webget;

import java.text.MessageFormat;
import java.util.Iterator;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.author.commons.beans.DataBean;
import com.author.commons.beans.HeadBean;
import com.author.commons.utils.Constants;
import com.author.commons.utils.enums.Rc;
import com.author.commons.utils.redis.RedisImpl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: TODO
 * @Author: YNa
 * @Date: 2020/7/21 15:37
 * @Version: #1.0 Copyright ? 2020
 */
@Slf4j
@RestController
@RequestMapping("/door")
@SuppressWarnings("all")
public class DoorController {
	@Autowired
	private RedisImpl cacheService;

	@RequestMapping(value = "/requestAuthor")
	public void requestAuthor(@RequestBody DataBean record) {
		if (StrUtil.isNotEmpty(record.getAppID())) {

			String openUrl = MessageFormat.format(Constants.AUTHOR_URL, record.getAppID(),
					MessageFormat.format(record.getAuthorCodeRedirectUri(), record.getAppID()), record.getState());
			if (StrUtil.isEmpty(record.getCmd())) {
				record.setCmd(Constants.DEFAULT_CMD);
			}
			String runCmd = record.getCmd() + StrUtil.TAB + openUrl;
			Runtime run = Runtime.getRuntime();
			try {
				run.exec(runCmd);
			} catch (Throwable ex) {
				log.error("打开浏览器异常:" + ex.getMessage());
			}
		}
	}

	@RequestMapping(value = "/headInit", method = RequestMethod.POST)
	public String headInit(@RequestBody @Valid HeadBean record, BindingResult results) {
		if (results.hasErrors()) {
			return results.getFieldError().getDefaultMessage();
		}
		cacheService.redisHandle(
				MessageFormat.format(Constants.redis.account_data, Rc.quid + StrUtil.COLON + record.getAppID()),
				record.getQuID(), Constants.redis.redis1str, 0);
		cacheService.redisHandle(
				MessageFormat.format(Constants.redis.account_data, Rc.qticket + StrUtil.COLON + record.getAppID()),
				record.getQticket(), Constants.redis.redis1str, 0);
		return "sucess";
	}

	public static void main(String[] args) {
		try {
			String quid = "eb1c7f4a0a56d512c4283ce780450527", qticket = "50ade5eeda226c344665e4ac3e23579f",
					appID = "1110495258";
			StringBuffer cookies = new StringBuffer();
			cookies.append("quid=").append(quid).append(";");
			cookies.append("qticket=").append(qticket).append(";");

			JSONObject params = new JSONObject();
			params.set("appid", appID);

			String generalSituationUri = "https://q.qq.com/pb/GetGeneralSituation";
			String response = HttpRequest.post(generalSituationUri).header("cookie", cookies.toString())
					.body(params.toString()).execute().body();

			Object data = JSONUtil.parseObj(response).get("data");
			JSONArray generalSituations = JSONUtil.parseArray(JSONUtil.parseObj(data).get("generalSituation"));
			Iterator<Object> results = generalSituations.iterator();
			/* 访问人数 */
			String situation5type = "5";
			do {
				JSONObject result = (JSONObject) results.next();
				if (StrUtil.equalsIgnoreCase(StrUtil.toString(result.get("dataType")), situation5type)) {
					/* 访问人数 */
					String persons = StrUtil.toString(result.get("number"));
				}
			} while (results.hasNext());

			String retentionDataUri = "https://q.qq.com/pb/GetRetentionTrend";
			/* 活跃留存 */
			String calculate2type = "2";
		} catch (Throwable ex) {
			log.error("二维码解析异常:{}", ex.getMessage());
		}
	}
}
