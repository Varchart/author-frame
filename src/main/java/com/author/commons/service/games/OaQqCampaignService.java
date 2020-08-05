package com.author.commons.service.games;

import com.author.commons.beans.games.OaQqCampaign;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
public interface OaQqCampaignService extends IService<OaQqCampaign> {
	int insertData(OaQqCampaign record);
}
