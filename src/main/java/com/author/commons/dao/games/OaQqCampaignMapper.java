package com.author.commons.dao.games;

import org.apache.ibatis.annotations.Mapper;

import com.author.commons.beans.games.OaQqCampaign;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
@Mapper
public interface OaQqCampaignMapper extends BaseMapper<OaQqCampaign> {
	int insertActive(OaQqCampaign record);
}
