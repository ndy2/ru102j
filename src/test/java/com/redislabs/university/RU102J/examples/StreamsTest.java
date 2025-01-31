package com.redislabs.university.RU102J.examples;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redislabs.university.RU102J.HostPort;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.StreamEntry;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.Transaction;

public class StreamsTest {
	private Jedis jedis;
	private String streamKey;
	private final Long numberOfSolarSites = 300L;
	private final Long measurementsPerHour = 60L;
	private final Long hoursPerDay = 24L;
	private final Long maxDays = 14L;
	private Map<String, String> entry1;
	private Map<String, String> entry2;

	@Before
	public void setUp() {
		this.jedis = new Jedis(HostPort.getRedisHost(), HostPort.getRedisPort());

		if (HostPort.getRedisPassword().length() > 0) {
			this.jedis.auth(HostPort.getRedisPassword());
		}

		this.streamKey = "test:stream";
		this.entry1 = new HashMap<>();
		entry1.put("siteId", "1");
		this.entry2 = new HashMap<>();
		entry2.put("siteId", "2");
	}

	@After
	public void tearDown() {
		jedis.del(streamKey);
		jedis.close();
	}

	@Test
	public void testStream() {
		Long maxStreamEntries =
			numberOfSolarSites * measurementsPerHour * hoursPerDay * maxDays;

		Map<String, String> entry = new HashMap<>();
		entry.put("siteId", "1");
		entry.put("tempC", "18.0");

		StreamEntryID id = jedis.xadd(streamKey, StreamEntryID.NEW_ENTRY, entry,
			maxStreamEntries, true);
		List<StreamEntry> results = jedis.xrevrange(streamKey, null, null, 1);

		assertThat(results.size(), is(1));
		assertThat(results.get(0).getID(), is(id));
		assertThat(results.get(0).getFields(), is(entry));
	}

	@Test
	public void testStreamWithPipeline() {
		Pipeline p = jedis.pipelined();
		Response<StreamEntryID> id1 = p.xadd(streamKey, StreamEntryID.NEW_ENTRY, entry1);
		Response<StreamEntryID> id2 = p.xadd(streamKey, StreamEntryID.NEW_ENTRY, entry2);
		Response<List<StreamEntry>> results = p.xrange(streamKey, null, null, 2);
		p.sync();

		List<StreamEntry> entries = results.get();
		assertThat(entries.size(), is(2));
		assertThat(entries.get(0).getID(), is(id1.get()));
		assertThat(entries.get(1).getID(), is(id2.get()));
	}

	@Test
	public void testStreamWithTransaction() {
		Transaction t = jedis.multi();
		t.xadd(streamKey, StreamEntryID.NEW_ENTRY, entry1);
		t.xadd(streamKey, StreamEntryID.NEW_ENTRY, entry2);
		Response<List<StreamEntry>> results = t.xrange(streamKey, null, null, 2);
		t.exec();
		assertThat(results.get().size(), is(2));
	}
}
