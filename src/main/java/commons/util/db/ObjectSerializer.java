package commons.util.db;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 *
 *Author:WangXin69
 *Date:2018年5月2日
 *
 */
public class ObjectSerializer {
	private ObjectSerializer() {
	}

	private static ObjectSerializer instance = null;

	public static ObjectSerializer getInstance() {
		if (null == instance) {
			synchronized (ObjectSerializer.class) {
				if (null == instance) {
					instance = new ObjectSerializer();
				}
			}
		}
		return instance;
	}

	public <T> byte[] serialize(T t) throws SerializerException {
		if (null == t) {
			throw new NullPointerException("Can not serialize null");
		}
		byte[] value = null;
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			@SuppressWarnings("unchecked")
			Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(t.getClass());
			value = ProtostuffIOUtil.toByteArray(t, schema, buffer);
		} finally {
			buffer.clear();
		}
		return value;
	}

	public <T> T deSerialize(byte[] value, Class<T> cls) {
		if (null == value || null == cls) {
			throw new NullPointerException("要反序列化的byte数据为空");
		}
		T instance = null;
		try {
			instance = cls.newInstance();
			Schema<T> schema = RuntimeSchema.getSchema(cls);
			ProtostuffIOUtil.mergeFrom(value, instance, schema);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
}


