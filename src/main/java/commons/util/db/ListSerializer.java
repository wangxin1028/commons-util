package commons.util.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class ListSerializer {
	private static ListSerializer listserializer = null;

	private ListSerializer() {
	}

	public static ListSerializer getInstance() {
		if (null == listserializer) {
			synchronized (ListSerializer.class) {
				if (null == listserializer) {
					listserializer = new ListSerializer();
				}
			}
		}
		return listserializer;
	}

	public <T> byte[] serialize(List<T> values) throws SerializerException {
		if (values == null) {
			throw new NullPointerException("Can not serialize null");
		}
		byte[] byteValue = null;
		try {
			@SuppressWarnings("unchecked")
			Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(values.get(0).getClass());
			LinkedBuffer buffer = LinkedBuffer.allocate(1024 * 1024);
			ByteArrayOutputStream bos = null;
			try {
				bos = new ByteArrayOutputStream();
				ProtostuffIOUtil.writeListTo(bos, values, schema, buffer);
				byteValue = bos.toByteArray();
			} finally {
				buffer.clear();
				try {
					if (null != bos) {
						bos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return byteValue;
	}

	public <T> List<T> deSerialize(byte[] buf, Class<T> t) throws SerializerException {
		if (null == buf) {
			throw new RuntimeException("要反序列化的byte数据为空");
		}
		List<T> list = new ArrayList<>();
		try {
			Schema<T> schema = RuntimeSchema.getSchema(t);
			list = ProtostuffIOUtil.parseListFrom(new ByteArrayInputStream(buf), schema);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}


