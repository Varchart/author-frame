package com.author.commons.service.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.author.commons.beans.games.OaQqCampaign;
import com.author.commons.dao.games.OaQqCampaignMapper;
import com.author.commons.utils.aspects.annotations.Noder;
import com.author.commons.utils.enums.Ndb;
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
public class OaQqCampaignServiceImpl extends ServiceImpl<OaQqCampaignMapper, OaQqCampaign> implements OaQqCampaignService {
	@Autowired
	private OaQqCampaignMapper oaQqCampaignMapper;

	@Override
	@Noder(node = Ndb.writer)
	public int insertData(OaQqCampaign record) {
		return oaQqCampaignMapper.insertActive(record);
	}
}
