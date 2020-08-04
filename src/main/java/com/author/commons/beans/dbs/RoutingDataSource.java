package com.author.commons.beans.dbs;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

import com.author.commons.utils.dbs.DBContextHolder;

public class RoutingDataSource extends AbstractRoutingDataSource {

	@Nullable
	@Override
	protected Object determineCurrentLookupKey() {
		return DBContextHolder.getDBSource();
	}
}
