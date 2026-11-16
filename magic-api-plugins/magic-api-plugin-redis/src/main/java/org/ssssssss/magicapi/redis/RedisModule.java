package org.ssssssss.magicapi.redis;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPipelineException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ReflectionUtils;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.functions.DynamicMethod;
import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * redis模块
 *
 * @author mxd
 */
@MagicModule("redis")
public class RedisModule implements DynamicMethod {

	private static final Object UNHANDLED = new Object();

	private static final int SCAN_LIMIT = 50;

	private final StringRedisTemplate redisTemplate;

	private final RedisTemplate<?, ?> objectTemplate;

	private final boolean isRedisson;

	public RedisModule(RedisConnectionFactory connectionFactory) {
		this(connectionFactory, null);
	}

	public RedisModule(RedisConnectionFactory connectionFactory, RedisTemplate<?, ?> objectTemplate) {
		this.redisTemplate = new StringRedisTemplate(connectionFactory);
		this.objectTemplate = objectTemplate;
		this.isRedisson = Objects.equals("org.redisson.spring.data.connection.RedissonConnectionFactory", this.redisTemplate.getConnectionFactory().getClass().getName());
	}

	/**
	 * 序列化
	 */
	private byte[] serializer(Object value) {
		if (value == null || value instanceof String) {
			return redisTemplate.getStringSerializer().serialize((String) value);
		}
		return serializer(value.toString());
	}

	private Object serializerForRedisson(Object value){
		if(value == null || JavaReflection.isPrimitiveAssignableFrom(value.getClass(), value.getClass())){
			return value;
		}
		return serializer(value.toString());
	}

	/**
	 * 反序列化
	 */
	@SuppressWarnings("unchecked")
	private Object deserialize(Object value) {
		if (value != null) {
			if (value instanceof byte[]) {
				return this.redisTemplate.getStringSerializer().deserialize((byte[]) value);
			}
			if (value instanceof List) {
				List<Object> valueList = (List<Object>) value;
				List<Object> resultList = new ArrayList<>(valueList.size());
				for (Object val : valueList) {
					resultList.add(deserialize(val));
				}
				return resultList;
			}
			if (value instanceof Map) {
				Map<Object, Object> map = (Map<Object, Object>) value;
				LinkedHashMap<Object, Object> newMap = new LinkedHashMap<>(map.size());
				map.forEach((key, val) -> newMap.put(deserialize(key), deserialize(val)));
				return newMap;
			}
		}
		return value;
	}

	/**
	 * 执行命令
	 *
	 * @param methodName 命令名称
	 * @param parameters 命令参数
	 */
	@Override
	public Object execute(String methodName, List<Object> parameters) {
		Object highLevel = readWithTemplate(methodName, parameters);
		if (highLevel != UNHANDLED) {
			return highLevel;
		}
		if (isRedisson) {
			throw new IllegalArgumentException("Redis 只开放查询：scan、get、hget、hgetall、hkeys、lrange、llen、smembers、scard、zrange、type、ttl、exists");
		}
		return this.redisTemplate.execute(connection -> {
			Object result;
			if(isRedisson){
				result = executeForRedisson(((DefaultStringRedisConnection) connection).getDelegate(), methodName, parameters);
			} else {
				byte[][] params = new byte[parameters.size()][];
				for (int i = 0; i < params.length; i++) {
					params[i] = serializer(parameters.get(i));
				}
				result = connection.execute(methodName, params);
			}
			return deserialize(result);
		}, isRedisson || this.redisTemplate.isExposeConnection());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Object readWithTemplate(String methodName, List<Object> parameters) {
		if (objectTemplate == null) {
			return UNHANDLED;
		}
		RedisTemplate template = objectTemplate;
		String command = methodName == null ? "" : methodName.toLowerCase(Locale.ROOT);
		switch (command) {
			case "get":
				return template.opsForValue().get(text(parameters, 0));
			case "hget":
				return template.opsForHash().get(text(parameters, 0), text(parameters, 1));
			case "hgetall":
				return template.opsForHash().entries(text(parameters, 0));
			case "hkeys":
				return template.opsForHash().keys(text(parameters, 0));
			case "lrange":
				return template.opsForList().range(text(parameters, 0), number(parameters, 1, 0), number(parameters, 2, 20));
			case "llen":
				return template.opsForList().size(text(parameters, 0));
			case "smembers":
				return template.opsForSet().members(text(parameters, 0));
			case "scard":
				return template.opsForSet().size(text(parameters, 0));
			case "zrange":
				return template.opsForZSet().range(text(parameters, 0), number(parameters, 1, 0), number(parameters, 2, 20));
			case "type":
				DataType type = template.type(text(parameters, 0));
				return type == null ? "none" : type.code();
			case "ttl":
				return template.getExpire(text(parameters, 0));
			case "exists":
				return template.hasKey(text(parameters, 0));
			case "scan":
			case "keys":
				return scan(parameters == null || parameters.isEmpty() ? "*" : text(parameters, 0));
			default:
				return UNHANDLED;
		}
	}

	private List<String> scan(String pattern) {
		String match = pattern == null || pattern.trim().isEmpty() ? "*" : pattern;
		ScanOptions options = ScanOptions.scanOptions().match(match).count(SCAN_LIMIT).build();
		List<String> keys = new ArrayList<>();
		Object cursor = null;
		try {
			Method method = objectTemplate.getClass().getMethod("scan", ScanOptions.class);
			cursor = method.invoke(objectTemplate, options);
			Method hasNext = cursor.getClass().getMethod("hasNext");
			Method next = cursor.getClass().getMethod("next");
			while ((Boolean) hasNext.invoke(cursor) && keys.size() < SCAN_LIMIT) {
				keys.add(String.valueOf(next.invoke(cursor)));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Redis scan 失败: " + e.getMessage(), e);
		} finally {
			closeCursor(cursor);
		}
		return keys;
	}

	private static void closeCursor(Object cursor) {
		if (cursor == null) {
			return;
		}
		try {
			cursor.getClass().getMethod("close").invoke(cursor);
		} catch (Exception ignored) {
			// 旧版本游标没有 close
		}
	}

	private static String text(List<Object> parameters, int index) {
		if (parameters == null || parameters.size() <= index || parameters.get(index) == null) {
			throw new IllegalArgumentException("缺少第 " + (index + 1) + " 个参数");
		}
		return String.valueOf(parameters.get(index));
	}

	private static long number(List<Object> parameters, int index, long defaultValue) {
		if (parameters == null || parameters.size() <= index || parameters.get(index) == null) {
			return defaultValue;
		}
		Object value = parameters.get(index);
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return Long.parseLong(String.valueOf(value));
	}

	private Object executeForRedisson(RedisConnection connection, String command, List<Object> parameters) {
		Method[] methods = connection.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equalsIgnoreCase(command) && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == parameters.size()) {
				try {
					Object ret = this.execute(connection, method, parameters);
					if (ret instanceof String) {
						return ((String) ret).getBytes();
					}
					return ret;
				} catch (IllegalArgumentException e) {
					if (connection.isPipelined()) {
						throw new RedisPipelineException(e);
					}

					throw new InvalidDataAccessApiUsageException(e.getMessage(), e);
				}
			}
		}
		throw new UnsupportedOperationException();
	}
	private Object execute(RedisConnection connection,Method method, List<Object> parameters){
		if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] == byte[][].class) {
			return ReflectionUtils.invokeMethod(method, connection, parameters.stream().map(this::serializer).toArray(byte[][]::new));
		} else if (parameters.size() == 0){
			return ReflectionUtils.invokeMethod(method, connection);
		}
		return ReflectionUtils.invokeMethod(method, connection, parameters.stream().map(this::serializerForRedisson).toArray());
	}

}
