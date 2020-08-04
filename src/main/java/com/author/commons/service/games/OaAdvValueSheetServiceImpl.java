package com.author.commons.service.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.author.commons.beans.games.OaAdvValueSheet;
import com.author.commons.beans.games.OaAdvValueSheetDTO;
import com.author.commons.dao.games.OaAdvValueSheetMapper;
import com.author.commons.utils.aspects.annotations.PageQry;
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
public class OaAdvValueSheetServiceImpl extends ServiceImpl<OaAdvValueSheetMapper, OaAdvValueSheet> implements OaAdvValueSheetService {
	@Autowired
	private OaAdvValueSheetMapper valueSheetMapper;

	@Override
	@PageQry
	public OaAdvValueSheetDTO queryData(OaAdvValueSheetDTO record) {
		return valueSheetMapper.queryByParams(record);
	}

}
