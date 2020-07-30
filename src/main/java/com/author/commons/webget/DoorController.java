package com.author.commons.webget;

import java.text.MessageFormat;

import javax.annotation.Resource;
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
import com.author.commons.utils.Result;
import com.author.commons.utils.caches.RedisImpl;
import com.author.commons.utils.enums.Rc;
import com.author.commons.utils.quartzs.RunHandle;
import com.author.commons.utils.resps.Response;

import cn.hutool.core.util.StrUtil;
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

	@Resource
	private RunHandle handleService;

	@RequestMapping(value = "/requestAuthor")
	public void requestAuthor(@RequestBody DataBean record) {
		if (StrUtil.isNotEmpty(record.getAppID())) {

			String openUrl = MessageFormat.format(Constants.AUTHOR_URL, record.getAppID(), MessageFormat.format(record.getAuthorCodeRedirectUri(), record.getAppID()), record.getState());
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
	public Result headInit(@RequestBody @Valid HeadBean record, BindingResult results) {
		if (results.hasErrors()) {
			return Response.genFailResult(results.getFieldError().getDefaultMessage());
		}
		cacheService.redisHandle(MessageFormat.format(Constants.redis.account_data, Rc.quid + StrUtil.COLON + record.getAppID()), record.getQuID(), Constants.redis.redis1str, 0);
		cacheService.redisHandle(MessageFormat.format(Constants.redis.account_data, Rc.qticket + StrUtil.COLON + record.getAppID()), record.getQticket(), Constants.redis.redis1str, 0);
		return Response.genSuccessResult();
	}

	@RequestMapping(value = "/headDel", method = RequestMethod.POST)
	public Result headDel(@RequestBody @Valid HeadBean record, BindingResult results) {
		if (results.hasErrors()) {
			return Response.genFailResult(results.getFieldError().getDefaultMessage());
		}
		cacheService.redisHandle(MessageFormat.format(Constants.redis.account_data, Rc.quid + StrUtil.COLON + record.getAppID()), record.getQuID(), Constants.redis.redis1str);
		cacheService.redisHandle(MessageFormat.format(Constants.redis.account_data, Rc.qticket + StrUtil.COLON + record.getAppID()), record.getQticket(), Constants.redis.redis1str);
		return Response.genSuccessResult();
	}

	@RequestMapping(value = "/handleData", method = RequestMethod.POST)
	public void handleData() {
		handleService.run1data();
	}
}
