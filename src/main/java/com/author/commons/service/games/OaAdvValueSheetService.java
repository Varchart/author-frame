package com.author.commons.service.games;

import com.author.commons.beans.games.OaAdvValueSheet;
import com.author.commons.beans.games.OaAdvValueSheetDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yn
 * @since 2020-07-24
 */
public interface OaAdvValueSheetService extends IService<OaAdvValueSheet> {
	OaAdvValueSheetDTO queryData(OaAdvValueSheetDTO record);
}
