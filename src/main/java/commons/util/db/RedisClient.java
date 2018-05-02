package commons.util.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 *Author:WangXin69
 *Date:2018年5月2日
 *
 */
public class RedisClient {
	private String ip = null;
	private int port = -1;
	private JedisPool pool = null;
	public int tryTimes = 2;
	private JedisPoolConfig config = new JedisPoolConfig();
	private static final ObjectSerializer serializer = ObjectSerializer.getInstance();
	private static final ListSerializer listSerializer = ListSerializer.getInstance();

	public static RedisClient create(String ip, int port) {
		return new RedisClient(ip, port);
	}

	public static RedisClient create(String configFilePath) throws Exception {
		PropertiesWrapper ppWrapper = new PropertiesWrapper(configFilePath);
		// IP地址
		String ip = ppWrapper.getString("ip");
		// 端口号: 默认6379
		int port = ppWrapper.getInt("port", 6379);

		JedisPoolConfig config = new JedisPoolConfig();
		// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,
		// 默认-1
		config.setMaxWaitMillis(ppWrapper.getLong("max_wait", 10 * 1000));

		// 最大连接数, 默认100个
		config.setMaxTotal(ppWrapper.getInt("max_total", 500));
		// 最大空闲连接数, 默认10个
		config.setMaxIdle(ppWrapper.getInt("max_idle", 100));
		// 最小空闲连接数, 默认0
		config.setMinIdle(0);

		// 在空闲时检查有效性, 默认false
		config.setTestWhileIdle(true);
		// 在获取连接的时候检查有效性, 默认false
		config.setTestOnBorrow(true);

		// 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
		config.setMinEvictableIdleTimeMillis(60 * 1000);
		// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
		config.setTimeBetweenEvictionRunsMillis(30 * 1000);
		// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
		config.setNumTestsPerEvictionRun(-1);

		return new RedisClient(ip, port, config);
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 */
	private RedisClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
		config.setMaxWaitMillis(10 * 1000);
		config.setMaxTotal(500);
		config.setMaxIdle(100);
		config.setTestWhileIdle(true);
		config.setTestOnBorrow(true);
		config.setMinEvictableIdleTimeMillis(60 * 1000);
		config.setTimeBetweenEvictionRunsMillis(30 * 1000);
		config.setNumTestsPerEvictionRun(-1);
		pool = new JedisPool(config, ip, port);
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 */
	private RedisClient(String ip, int port, JedisPoolConfig config) {
		this.ip = ip;
		this.port = port;
		this.config = config;
		this.pool = new JedisPool(config, ip, port);
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 * @param maxTotal
	 *            :Pool最大容积
	 * @param maxIdel
	 *            :最大空闲 连接个数
	 * @param minIdle
	 *            :最小空闲 连接个数
	 * @param isLifo
	 *            :是否使用Lifo数据结构存储 否则FIFO
	 * @param maxWaitMillis
	 *            :最大等待时间毫秒
	 * @param isUseJmx
	 *            :是否使用Jmx
	 * @param numTestsPerEvictionRun
	 *            :每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
	 * @param timeBetweenEvictionRunsMillis
	 *            :逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
	 * @param isTestOnBorrow
	 *            取连接时是否 检查链接
	 * @param isTestWhileIdle
	 *            连接空闲时是否 检查链接
	 */
	private RedisClient(String ip, int port, int maxTotal, int maxIdel, int minIdle, boolean isLifo, long maxWaitMillis,
			boolean isUseJmx, int numTestsPerEvictionRun, long timeBetweenEvictionRunsMillis, boolean isTestOnBorrow,
			boolean isTestWhileIdle) {
		this.ip = ip;
		this.port = port;
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdel);
		config.setMinIdle(minIdle);
		config.setLifo(isLifo);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setJmxEnabled(isUseJmx);
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		config.setTestOnBorrow(isTestOnBorrow);
		config.setTestWhileIdle(isTestWhileIdle);
		pool = new JedisPool(config, ip, port);
	}

	public void restart() {
		pool = new JedisPool(config, ip, port);
	}

	public void returnResource(Jedis redis) {
		if (redis != null) {
			redis.close();
		}
	}

	public String getTest(String key) {
		Jedis jedis = pool.getResource();
		String vlaue = jedis.get(key);
		returnResource(jedis);
		return vlaue;
	}

	public String get(String key) throws Exception {
		Exception exception = null;
		boolean isFail = false;
		String value = null;
		Jedis jedis = null;
		for (int i = 0; i < tryTimes; i++) {
			try {
				jedis = pool.getResource();
				value = jedis.get(key);
				isFail = false;
			} catch (Exception e) {
				pool.returnBrokenResource(jedis);
				isFail = true;
				exception = e;
			} finally {
				returnResource(jedis);
			}
			if (!isFail)
				break;
		}
		if (isFail) {
			throw exception;
		}
		return value;
	}

	/**
	 * 取对象
	 * 
	 * @param key
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> clz) throws Exception {
		if (key == null || key.isEmpty() || clz == null) {
			return null;
		}
		return (T) getObject(key, clz, new ValueDecoder() {
			@SuppressWarnings("hiding")
			@Override
			public <T> Object decode(byte[] value, Class<T> clz) throws Exception {
				return serializer.deSerialize(value, clz);
			}
		});
	}

	/**
	 * 取对象
	 * 
	 * @param key
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String key, Class<T> clz) throws Exception {
		if (key == null || key.isEmpty() || clz == null) {
			return null;
		}
		return (List<T>) getObject(key, clz, new ValueDecoder() {
			@SuppressWarnings("hiding")
			@Override
			public <T> Object decode(byte[] value, Class<T> clz) throws Exception {
				return listSerializer.deSerialize(value, clz);
			}

		});
	}

	public <T> Object getObject(String key, Class<T> clz, ValueDecoder valueDecoder) throws Exception {
		Exception exception = null;
		boolean isFail = false;
		Object value = null;
		Jedis jedis = null;
		for (int i = 0; i < tryTimes; i++) {
			try {
				jedis = pool.getResource();
				byte[] byteValue = jedis.get(key.getBytes());
				if (byteValue != null && byteValue.length > 0) {
					value = valueDecoder.decode(byteValue, clz);
				}
				isFail = false;
			} catch (Exception e) {
				pool.returnBrokenResource(jedis);
				isFail = true;
				exception = e;
			} finally {
				returnResource(jedis);
			}
			if (!isFail)
				break;
		}
		if (isFail) {
			throw exception;
		}
		return value;
	}

	private interface ValueDecoder {

		public <T> Object decode(byte[] value, Class<T> clz) throws Exception;
	}

	/**
	 * 给定一个模糊匹配的key，例如NCP*，即得到一组匹配的keyset，用于批量取得key
	 * 
	 * @param String
	 *            key
	 * @return Set<String>
	 * @throws Exception
	 */
	public Set<String> getKeySet(String key) throws Exception {
		Exception exception = null;
		boolean isFail = false;
		Set<String> value = null;
		Jedis jedis = null;
		for (int i = 0; i < tryTimes; i++) {
			try {
				jedis = pool.getResource();
				value = jedis.keys(key);
				isFail = false;
			} catch (Exception e) {
				pool.returnBrokenResource(jedis);
				isFail = true;
				exception = e;
			} finally {
				returnResource(jedis);
			}
			if (!isFail)
				break;
		}
		if (isFail) {
			throw exception;
		}
		return value;
	}
	/**
	 * 删除key
	 * @param key
	 */
	public void deleteKey(String key){
		Jedis jedis = pool.getResource();
		if(null!=jedis){
			Set<String> set = jedis.keys(key);
			Iterator<String> iterator = set.iterator();
			while(iterator.hasNext()){
				String k = iterator.next();
				jedis.del(k);
			}
		}
	}
	public void set(String key, String value) throws Exception {
		Exception exception = null;
		boolean isFail = false;
		Jedis jedis = null;
		for (int i = 0; i < tryTimes; i++) {
			try {
				jedis = pool.getResource();
				value = jedis.set(key, value);
				isFail = false;
			} catch (Exception e) {
				pool.returnBrokenResource(jedis);
				isFail = true;
				exception = e;
			} finally {
				returnResource(jedis);
			}
			if (!isFail)
				break;
		}
		if (isFail) {
			throw exception;
		}
	}

	public void setex(String key, String value, int seconds) throws Exception {
		Exception exception = null;
		boolean isFail = false;
		Jedis jedis = null;
		for (int i = 0; i < tryTimes; i++) {
			try {
				jedis = pool.getResource();
				value = jedis.setex(key, seconds, value);
				isFail = false;
			} catch (Exception e) {
				pool.returnBrokenResource(jedis);
				isFail = true;
				exception = e;
			} finally {
				returnResource(jedis);
			}
			if (!isFail)
				break;
		}
		if (isFail) {
			throw exception;
		}
	}

	public void del(String key) {
		del(key.getBytes(Charset.forName("UTF-8")));
	}

	public void del(byte[] key) {
		Jedis jedis = pool.getResource();
		jedis.del(key);
		returnResource(jedis);
	}

	@Deprecated
	public <T> void set(String key, T value, Class<T> clz) throws Exception {
		if (key == null || key.isEmpty() || value == null || clz == null) {
			return;
		}
		setex(key.getBytes(), serializer.serialize(value), 0);
	}

	/**
	 * 设置对象类型值
	 * 
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public <T> void set(String key, T value) throws Exception {
		if (key == null || key.isEmpty() || value == null) {
			return;
		}
		setex(key.getBytes(), serializer.serialize(value), 0);
	}

	/**
	 * 设置对象类型值
	 * 
	 * @param key
	 * @param value
	 * @param seconds
	 *            设置过期时间,单位s,0表示不过期
	 * @throws Exception
	 */
	public <T> void set(String key, T value, int seconds) throws Exception {
		if (key == null || key.isEmpty() || value == null) {
			return;
		}
		setex(key.getBytes(), serializer.serialize(value), seconds);
	}

	/**
	 * 设置list类型值
	 * 
	 * @param key
	 * @param values
	 * @throws Exception
	 */
	public <T> void setList(String key, List<T> values) throws Exception {
		setex(key.getBytes(), listSerializer.serialize(values), 0);
	}

	/**
	 * 设置list类型值
	 * 
	 * @param key
	 * @param values
	 * @throws Exception
	 */
	public <T> void setList(String key, List<T> values, int seconds) throws Exception {
		setex(key.getBytes(), listSerializer.serialize(values), seconds);
	}

	/**
	 * 设置值
	 * 
	 * @param key
	 * @param vlaue
	 * @param seconds
	 *            过期时间,单位秒,传入0则不过期.
	 * @throws Exception
	 */
	public void setex(byte[] key, byte[] vlaue, int seconds) throws Exception {
		Jedis jedis = null;
		Exception exception = null;
		boolean isFail = false;
		for (int i = 0; i < tryTimes; i++) {
			try {
				jedis = pool.getResource();
				// 设置成功返回OK
				if (seconds > 0) {
					jedis.setex(key, seconds, vlaue);
				} else {
					jedis.set(key, vlaue);
				}
				isFail = false;
			} catch (Exception e) {
				e.printStackTrace();
				isFail = true;
				exception = e;
			} finally {
				returnResource(jedis);
			}
			if (!isFail) {
				break;
			}
		}
		if (isFail) {
			throw exception;
		}
	}
	
	public boolean exists(String key) throws Exception {
		Jedis jedis = null;
		try {
			jedis = get();
			return jedis.exists(key);
		} catch (Exception e) {
			throw e;
		} finally {
			returnResource(jedis);
		}
	}
	
	public boolean exists(byte[] key) throws Exception {
		Jedis jedis = null;
		try {
			jedis = get();
			return jedis.exists(key);
		} catch (Exception e) {
			throw e;
		} finally {
			returnResource(jedis);
		}
	}

	public Jedis get() {
		return pool.getResource();
	}

	/**
	 * 
	 * @param isBlockWhenExhausted
	 */
	public void setWhenExHaustedAction(boolean isBlockWhenExhausted) {
		// false : 抛出异常，
		// true : 阻塞，直到有可用链接资源
		config.setBlockWhenExhausted(isBlockWhenExhausted);
	}

	/**
	 * 
	 * @param maxWaitMillis
	 */
	public void setMaxWait(long maxWaitMillis) {
		// 最大等待毫秒数
		config.setMaxWaitMillis(maxWaitMillis);
	}

	/**
	 * 
	 * @param configParam
	 */
	public void setMaxIdle(int configParam) {
		// 最大空闲对象个数 <Jedis>
		config.setMaxIdle(configParam);
	}

	/**
	 * 
	 * @param timeBetweenEvictionRunsMillis
	 *            扫描时间间隔 毫秒
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		// 扫描时间间隔 毫秒
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

	/**
	 * 
	 * @param softMinEvictableIdleTimeMillis
	 *            空闲丢弃策略之一 当空闲时间大于该值后丢弃该链接 <Jedis>
	 */
	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
		// 空闲丢弃策略之一 当空闲时间大于该值后丢弃该链接 <Jedis>
		config.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
	}

	/**
	 * 
	 * @param isLifo
	 *            true为栈 ,false为队列
	 */
	public void setLifo(boolean isLifo) {
		// true为栈 ,false为队列
		config.setLifo(isLifo);
	}

	/**
	 * 
	 * @param isTestOnBorrow
	 *            是否检查获得链接的有效性 true是 false否
	 */
	public void setTestOnBorrow(boolean isTestOnBorrow) {
		// 是否检查获得链接的有效性 true是 false否
		config.setTestOnBorrow(isTestOnBorrow);
	}

	/**
	 * 
	 * @param istestWhileIdle
	 *            是否检查空闲连接的有效性 true是 false否
	 */
	public void setTestWhileIdle(boolean istestWhileIdle) {
		// 是否检查空闲连接的有效性 true是 false否
		config.setTestWhileIdle(istestWhileIdle);
	}

	public static class PropertiesWrapper {

		private Properties pro = null;

		public PropertiesWrapper(String path) throws Exception {
			pro = loadProperty(path);
		}

		public PropertiesWrapper(InputStream inputStream) {
			pro = new Properties();
			try {
				pro.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public String getString(String key) throws Exception {
			try {
				return pro.getProperty(key);
			} catch (Exception e) {
				throw new Exception("key:" + key);
			}
		}

		public int getInt(String key) throws Exception {
			try {
				return Integer.parseInt(pro.getProperty(key));
			} catch (Exception e) {
				throw new Exception("key:" + key);
			}
		}

		public int getInt(String key, int defaultValue) throws Exception {
			String value = pro.getProperty(key);
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				return defaultValue;
			}
		}

		public long getLong(String key, long defaultValue) throws Exception {
			String value = pro.getProperty(key);
			try {
				return Long.parseLong(value);
			} catch (Exception e) {
				return defaultValue;
			}
		}

		public float getFloat(String key) throws Exception {
			try {
				return Float.parseFloat(pro.getProperty(key));
			} catch (Exception e) {
				throw new Exception("key:" + key);
			}
		}

		public boolean getBoolean(String key) throws Exception {
			try {
				return Boolean.parseBoolean(pro.getProperty(key));
			} catch (Exception e) {
				throw new Exception("key:" + key);
			}
		}

		public Set<Object> getAllKey() {
			return pro.keySet();
		}

		public Collection<Object> getAllValue() {
			return pro.values();
		}

		public Map<String, Object> getAllKeyValue() {
			Map<String, Object> mapAll = new HashMap<String, Object>();
			Set<Object> keys = getAllKey();

			Iterator<Object> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next().toString();
				mapAll.put(key, pro.get(key));
			}
			return mapAll;
		}

		private Properties loadProperty(String filePath) throws Exception {
			FileInputStream fin = null;
			Properties pro = new Properties();
			try {
				fin = new FileInputStream(filePath);
				pro.load(fin);
			} catch (IOException e) {
				throw e;
			} finally {
				if (fin != null) {
					fin.close();
				}
			}
			return pro;
		}
	}

}


