package com.author.commons.service.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.author.commons.beans.games.OaAdvStat;
import com.author.commons.beans.games.OaAdvStatDTO;
import com.author.commons.beans.games.StatParamsDTO;
import com.author.commons.dao.games.OaAdvStatMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
@Service
public class OaAdvStatServiceImpl extends ServiceImpl<OaAdvStatMapper, OaAdvStat> implements OaAdvStatService {
	@Autowired
	private OaAdvStatMapper statMapper;

	@Override
	public int exists(StatParamsDTO record) {
		return statMapper.exists(record);
	}

	@Override
	public OaAdvStatDTO persist(StatParamsDTO record) {
		return statMapper.advertStatPersist(record);
	}
}
