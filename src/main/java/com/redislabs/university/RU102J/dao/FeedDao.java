package com.redislabs.university.RU102J.dao;

import java.util.List;

import com.redislabs.university.RU102J.api.MeterReading;

public interface FeedDao {
	void insert(MeterReading meterReading);

	List<MeterReading> getRecentGlobal(int limit);

	List<MeterReading> getRecentForSite(long siteId, int limit);
}
