package com.redislabs.university.RU102J.dao;

import java.util.Set;

import com.redislabs.university.RU102J.api.Site;

public interface SiteDao {
	void insert(Site site);

	Site findById(long id);

	Set<Site> findAll();
}
