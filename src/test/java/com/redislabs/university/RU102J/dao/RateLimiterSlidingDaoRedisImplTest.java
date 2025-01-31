package com.redislabs.university.RU102J.dao;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.redislabs.university.RU102J.HostPort;
import com.redislabs.university.RU102J.TestKeyManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RateLimiterSlidingDaoRedisImplTest {

	private static JedisPool jedisPool;
	private static Jedis jedis;
	private static TestKeyManager keyManager;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void setUp() throws Exception {
		String password = HostPort.getRedisPassword();

		if (password.length() > 0) {
			jedisPool = new JedisPool(new JedisPoolConfig(), HostPort.getRedisHost(), HostPort.getRedisPort(), 2000,
				password);
			jedis = new Jedis(HostPort.getRedisHost(), HostPort.getRedisPort());

			jedis.auth(password);
		} else {
			jedisPool = new JedisPool(HostPort.getRedisHost(), HostPort.getRedisPort());
			jedis = new Jedis(HostPort.getRedisHost(), HostPort.getRedisPort());
		}
		keyManager = new TestKeyManager("test");
	}

	@AfterClass
	public static void tearDown() {
		jedisPool.destroy();
		jedis.close();
	}

	@After
	public void flush() {
		keyManager.deleteKeys(jedis);
	}

	@Ignore
	@Test
	public void hit() {
		int exceptionCount = 0;
		RateLimiter limiter = new RateLimiterSlidingDaoRedisImpl(jedisPool,
			100, 10);
		for (int i = 0; i < 10; i++) {
			try {
				limiter.hit("foo");
			} catch (RateLimitExceededException e) {
				exceptionCount += 1;
			}
		}

		assertThat(exceptionCount, is(0));
	}

	@Ignore
	@Test
	public void hitOutsideLimit() {
		int exceptionCount = 0;
		RateLimiter limiter = new RateLimiterSlidingDaoRedisImpl(jedisPool,
			100, 10);
		for (int i = 0; i < 12; i++) {
			try {
				limiter.hit("foo");
			} catch (RateLimitExceededException e) {
				exceptionCount += 1;
			}
		}

		assertThat(exceptionCount, is(2));
	}

	@Ignore
	@Test
	public void hitOutsideWindow() throws InterruptedException {
		int exceptionCount = 0;
		RateLimiter limiter = new RateLimiterSlidingDaoRedisImpl(jedisPool,
			100, 10);
		for (int i = 0; i < 11; i++) {
			if (i == 10) {
				// Sleep long enough for the window to expire.
				Thread.sleep(200);
			}
			try {
				limiter.hit("foo");
			} catch (RateLimitExceededException e) {
				exceptionCount += 1;
			}
		}

		assertThat(exceptionCount, is(0));
	}
}
