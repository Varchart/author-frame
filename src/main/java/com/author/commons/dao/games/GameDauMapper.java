package com.author.commons.dao.games;

import org.apache.ibatis.annotations.Mapper;

import com.author.commons.beans.games.GameDau;
import com.author.commons.beans.games.GameDauDTO;
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
public interface GameDauMapper extends BaseMapper<GameDau> {
	int queryDau(GameDauDTO record);
}
