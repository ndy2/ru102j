package com.redislabs.university.RU102J.dao;

import java.util.Set;

import com.redislabs.university.RU102J.api.GeoQuery;
import com.redislabs.university.RU102J.api.Site;

public interface SiteGeoDao extends SiteDao {
	Set<Site> findByGeo(GeoQuery query);
}
