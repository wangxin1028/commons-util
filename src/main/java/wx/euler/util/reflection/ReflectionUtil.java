package wx.euler.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtil {
	/**
	 * 获取给定的类的父类的泛型
	 * @param clazz
	 * @param index
	 * @return
	 */
	public static Class<?> getSuperClassGenricType(Class<?> clazz,int index) {
		Type type = clazz.getGenericSuperclass();
		if(type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type[] types = parameterizedType.getActualTypeArguments();
			if(types==null || types.length<=index) {
				return null;
			}else {
				return (Class<?>)types[index];
			}
		}else {
			return null;
		}
	}
	/**
	 * 给指定的对象的指定的属性设置指定的值
	 * @param target
	 * @param fieldName
	 * @param parm
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setValue(Object target,String fieldName,Object parm) throws IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> clazz = target.getClass();
		Field field = getField(clazz, fieldName);
		field.setAccessible(true);
		field.set(target, parm);
	}
	/**
	 * 获取一个Class中的指定Field
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	private static Field getField(Class<?> clazz,String fieldName) {
		Field field = null;
		for(;clazz!=null;clazz = clazz.getSuperclass()) {
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (Exception e) {}
		}
		return field;
	}
	/**
	 * 参数不支持基本数据类型
	 * @param target
	 * @param methodName
	 * @param args
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static Object invoke(Object target,String methodName,Object ...args) throws IllegalArgumentException, InvocationTargetException {
		Class<? extends Object> clazz = target.getClass();
		Class<?>[] argsClazz = getArgsClazz(args);
		Method method = getMethod(clazz, methodName, argsClazz);
		try {
			return method.invoke(target, args);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private static Class<?>[] getArgsClazz(Object... args) {
		Class<?>[] argsClazz = new Class[args.length];
		for(int i = 0 ; i < args.length ; i++) {
			argsClazz[i] = args[i].getClass();
		}
		return argsClazz;
	}

	@SuppressWarnings("unused")
	private static Method getMethod(Class<?> clazz,String methodName,Class<?> ...args ) {
		Method method = null;
		for(;clazz!=null;clazz = clazz.getSuperclass()) {
			try {
				method = clazz.getDeclaredMethod(methodName, args);
			} catch (Exception e) {	e.printStackTrace();
			}
			return method;
		}
		return method;
	}
	/**
	 * 1.如果要调用的方法是费静态的,必须有无参构造方法
	 * 2.要调用的方法可以是静态的,如果是静态的可以没有无参构造方法
	 * 3.参数不支持基本数据类型
	 * @param className
	 * @param methodName
	 * @param args
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static Object invoke(String className,String methodName,Object ...args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName(className);
		Class<?>[] argsClazz = getArgsClazz(args);
		Method method = getMethod(clazz, methodName, argsClazz);
		boolean sta = isStatic(method);
		if(sta) {
			method.setAccessible(true);
			return method.invoke(null, args);
		}else {
			Object target = clazz.newInstance();
			return invoke(target, methodName, args);
		}
	}
	private static boolean isStatic(Method method) {
		int modifiers = method.getModifiers();
		return Modifier.isStatic(modifiers);
	}
}
