package commons.util.reflection;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import commons.util.entity.Persion;
import commons.util.reflection.ReflectionUtil;

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
			Object invoke = ReflectionUtil.invoke("wx.euler.util.entity.Persion", "calculate", 1,2);
			System.out.println(invoke);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSetValue() throws IllegalArgumentException, IllegalAccessException {
		Persion persion = new Persion();
		ReflectionUtil.setValue(persion, "name", "老王");
		System.out.println(persion);
	}

	@Test
	public void testGetSuperClassGenricType() {
		Class<?> class1 = ReflectionUtil.getSuperClassGenricType(Persion.class, 0);
		System.out.println(class1);
	}

}
