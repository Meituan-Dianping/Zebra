package com.dianping.zebra.dao;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import com.dianping.zebra.dao.annotation.TargetMethod;

public class AsyncMapperProxy<T> implements InvocationHandler, Serializable {

	private static final long serialVersionUID = -2910124058827357833L;

	private T mapper;

	public AsyncMapperProxy(T mapper) {
		this.mapper = mapper;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (isCallbackMethod(method, args)) {
			// 优先找是否有annotation
			Method _method = getAnnotationMethod(method);

			if (_method == null) {
				// 如果没有找到annotation的方法，再找同名少一个参数的重载方法
				_method = getOverrideMethod(method, args);
			}

			Object[] newArgs = new Object[args.length - 1];
			int i = 0;

			AsyncDaoCallback callback = null;
			for (Object arg : args) {
				if (arg != null) {
					if (!AsyncDaoCallback.class.isAssignableFrom(arg.getClass())) {
						newArgs[i++] = arg;
					} else {
						callback = (AsyncDaoCallback) arg;
					}
				} else {
					newArgs[i++] = arg;
				}
			}

			AsyncMapperExecutor.executeRunnable(mapper, _method, newArgs, callback);
			return null;
		} else if (Future.class.isAssignableFrom(method.getReturnType())) {
			Method _method = getAnnotationMethod(method);

			if (_method != null) {
				return AsyncMapperExecutor.submitCallback(mapper, _method, args);
			}

			throw new AsyncDaoException("Cannot find any target method for future method[" + method.getName() + "]");
		} else {
			return method.invoke(mapper, args);
		}
	}

	private Method getOverrideMethod(Method method, Object[] args) throws NoSuchMethodException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?>[] newParameterTypes = new Class<?>[args.length - 1];

		int i = 0;
		for (Class<?> clazz : parameterTypes) {
			if (!AsyncDaoCallback.class.isAssignableFrom(clazz)) {
				newParameterTypes[i++] = clazz;
			}
		}

		return mapper.getClass().getDeclaredMethod(method.getName(), newParameterTypes);
	}

	private Method getAnnotationMethod(Method method) {
		TargetMethod targetMethod = method.getAnnotation(TargetMethod.class);

		if (targetMethod != null) {
			Method[] methods = mapper.getClass().getMethods();

			for (Method _method : methods) {
				if (_method.getName().equalsIgnoreCase(targetMethod.name())) {
					Class<?>[] parameterTypes = _method.getParameterTypes();

					boolean isCallableMethod = false;
					for (Class<?> parameterType : parameterTypes) {
						if (AsyncDaoCallback.class.isAssignableFrom(parameterType)) {
							isCallableMethod = true;
							break;
						}
					}

					if (!isCallableMethod) {
						return _method;
					}
				}
			}
		}

		return null;
	}

	private boolean isCallbackMethod(Method method, Object[] args) {
		boolean hasAsyncCallback = false;

		if (args == null) {
			return hasAsyncCallback;
		}

		int count = 0;
		for (Object arg : args) {
			if (arg != null) {
				if (AsyncDaoCallback.class.isAssignableFrom(arg.getClass())) {
					hasAsyncCallback = true;
					count++;
				}
			}
		}

		if (hasAsyncCallback && count > 1) {
			throw new AsyncDaoException("Method[" + method.getName() + "] has more than one callback method!");
		}

		return hasAsyncCallback;
	}
}
