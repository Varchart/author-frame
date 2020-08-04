package com.author.commons.dao.games;

import org.apache.ibatis.annotations.Mapper;

import com.author.commons.beans.games.OaAdvStat;
import com.author.commons.beans.games.OaAdvStatDTO;
import com.author.commons.beans.games.StatParamsDTO;
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
public interface OaAdvStatMapper extends BaseMapper<OaAdvStat> {
	OaAdvStatDTO advertStatPersist(StatParamsDTO record);

	int exists(StatParamsDTO record);
}
