package com.redislabs.university.RU102J.examples;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redislabs.university.RU102J.HostPort;

import redis.clients.jedis.Jedis;

public class UpdateIfLowestScriptTest {

	private Jedis jedis;

	@Before
	public void setUp() {
		this.jedis = new Jedis(HostPort.getRedisHost(), HostPort.getRedisPort());

		if (HostPort.getRedisPassword().length() > 0) {
			this.jedis.auth(HostPort.getRedisPassword());
		}
	}

	@After
	public void tearDown() {
		jedis.del("testLua");
		jedis.close();
	}

	@Test
	public void updateIfLowest() {
		jedis.set("testLua", "100");
		UpdateIfLowestScript script = new UpdateIfLowestScript(jedis);

		boolean result = script.updateIfLowest("testLua", 50);
		assertThat(result, is(true));
		assertThat(jedis.get("testLua"), is("50"));
	}

	@Test
	public void updateIfLowestUnchanged() {
		jedis.set("testLua", "100");
		UpdateIfLowestScript script = new UpdateIfLowestScript(jedis);

		boolean result = script.updateIfLowest("testLua", 200);
		assertThat(result, is(false));
		assertThat(jedis.get("testLua"), is("100"));
	}

	@Test
	public void updateIfLowestWithNoKey() {
		jedis.del("testLua");
		UpdateIfLowestScript script = new UpdateIfLowestScript(jedis);

		boolean result = script.updateIfLowest("testLua", 200);
		assertThat(result, is(true));
		assertThat(jedis.get("testLua"), is("200"));
	}
}
