package com.author.commons.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.author.commons.beans.models.OaQqRobot;
import com.author.commons.dao.OaQqRobotMapper;
import com.author.commons.service.IOaQqRobotService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
@Service
public class OaQqRobotServiceImpl extends ServiceImpl<OaQqRobotMapper, OaQqRobot> implements IOaQqRobotService {
	@Autowired
	private OaQqRobotMapper robotMapper;

	@Override
	public boolean saveActive(OaQqRobot record) {
		return SqlHelper.retBool(robotMapper.insertActive(record));
	}
}
