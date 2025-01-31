package com.redislabs.university.RU102J.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.redislabs.university.RU102J.api.Coordinate;
import com.redislabs.university.RU102J.api.GeoQuery;
import com.redislabs.university.RU102J.api.Site;

import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SiteGeoDaoRedisImpl implements SiteGeoDao {
	final static private Double capacityThreshold = 0.2;
	private final JedisPool jedisPool;

	public SiteGeoDaoRedisImpl(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public Site findById(long id) {
		try (Jedis jedis = jedisPool.getResource()) {
			Map<String, String> fields =
				jedis.hgetAll(RedisSchema.getSiteHashKey(id));
			if (fields == null || fields.isEmpty()) {
				return null;
			}
			return new Site(fields);
		}
	}

	@Override
	public Set<Site> findAll() {
		try (Jedis jedis = jedisPool.getResource()) {
			Set<String> keys = jedis.zrange(RedisSchema.getSiteGeoKey(), 0, -1);
			Set<Site> sites = new HashSet<>(keys.size());
			for (String key : keys) {
				Map<String, String> site = jedis.hgetAll(key);
				if (!site.isEmpty()) {
					sites.add(new Site(site));
				}
			}
			return sites;
		}
	}

	@Override
	public Set<Site> findByGeo(GeoQuery query) {
		if (query.onlyExcessCapacity()) {
			return findSitesByGeoWithCapacity(query);
		} else {
			return findSitesByGeo(query);
		}
	}

	// Challenge #5
	private Set<Site> findSitesByGeoWithCapacity(GeoQuery query) {
		return Collections.emptySet();
	}
	// Comment out the above, and uncomment what's below
	//    private Set<Site> findSitesByGeoWithCapacity(GeoQuery query) {
	//        Set<Site> results = new HashSet<>();
	//        Coordinate coord = query.getCoordinate();
	//        Double radius = query.getRadius();
	//        GeoUnit radiusUnit = query.getRadiusUnit();
	//
	//         try (Jedis jedis = jedisPool.getResource()) {
	//             // START Challenge #5
	//             // TODO: Challenge #5: Get the sites matching the geo query, store them
	//             // in List<GeoRadiusResponse> radiusResponses;
	//             // END Challenge #5
	//
	//             Set<Site> sites = radiusResponses.stream()
	//                     .map(response -> jedis.hgetAll(response.getMemberByString()))
	//                     .filter(Objects::nonNull)
	//                     .map(Site::new).collect(Collectors.toSet());
	//
	//             // START Challenge #5
	//             Pipeline pipeline = jedis.pipelined();
	//             Map<Long, Response<Double>> scores = new HashMap<>(sites.size());
	//             // TODO: Challenge #5: Add the code that populates the scores HashMap...
	//             // END Challenge #5
	//
	//             for (Site site : sites) {
	//                 if (scores.get(site.getId()).get() >= capacityThreshold) {
	//                     results.add(site);
	//                 }
	//             }
	//         }
	//
	//         return results;
	//    }

	private Set<Site> findSitesByGeo(GeoQuery query) {
		Coordinate coord = query.getCoordinate();
		Double radius = query.getRadius();
		GeoUnit radiusUnit = query.getRadiusUnit();

		try (Jedis jedis = jedisPool.getResource()) {
			List<GeoRadiusResponse> radiusResponses =
				jedis.georadius(RedisSchema.getSiteGeoKey(), coord.getLng(),
					coord.getLat(), radius, radiusUnit);

			return radiusResponses.stream()
				.map(response -> jedis.hgetAll(response.getMemberByString()))
				.filter(Objects::nonNull)
				.map(Site::new).collect(Collectors.toSet());
		}
	}

	@Override
	public void insert(Site site) {
		try (Jedis jedis = jedisPool.getResource()) {
			String key = RedisSchema.getSiteHashKey(site.getId());
			jedis.hmset(key, site.toMap());

			if (site.getCoordinate() == null) {
				throw new IllegalArgumentException("Coordinate required for Geo " +
					"insert.");
			}
			Double longitude = site.getCoordinate().getGeoCoordinate().getLongitude();
			Double latitude = site.getCoordinate().getGeoCoordinate().getLatitude();
			jedis.geoadd(RedisSchema.getSiteGeoKey(), longitude, latitude,
				key);
		}
	}
}
