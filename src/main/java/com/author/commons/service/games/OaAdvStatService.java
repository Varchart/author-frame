package com.author.commons.service.games;

import com.author.commons.beans.games.OaAdvStat;
import com.author.commons.beans.games.OaAdvStatDTO;
import com.author.commons.beans.games.StatParamsDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
public interface OaAdvStatService extends IService<OaAdvStat> {
	OaAdvStatDTO persist(StatParamsDTO record);

	int exists(StatParamsDTO record);
}
