package wx.euler.util.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class ReflectionUtilTest {

	@Test
	public void testInvokeObjectStringObjectArray() {
		StringBuffer s = new StringBuffer("wangxin");
		try {
			Object invoke = ReflectionUtil.invoke(s, "toString");
			System.out.println(invoke);
		} catch (IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@Test
	public void testInvokeStringStringObjectArray() {
		try {
			Object invoke = ReflectionUtil.invoke("java.lang.System", "getProperty", "user.dir");
			System.out.println(invoke);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void test() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> forName = Class.forName("java.lang.Math");
		Method method = forName.getMethod("cos", double.class);
		
		Object invoke = method.invoke(null, 2);
		System.out.println(invoke);
	}

}
