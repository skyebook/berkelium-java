package org.berkelium.java.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.berkelium.java.api.Berkelium;
import org.berkelium.java.api.Window;

public class WindowThreadProxy implements InvocationHandler {
	private final Berkelium berkelium;
	private final Window target;
	private final Window proxy = (Window) Proxy.newProxyInstance(getClass()
			.getClassLoader(), new Class<?>[] { Window.class }, this);

	public WindowThreadProxy(Window target) {
		this.target = target;
		berkelium = target.getBerkelium();
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args)
			throws Throwable {
		String name = method.getName();
		if ("getRealWindow".equals(name)) {
			return target.getRealWindow();
		} else if ("getThreadProxyWindow".equals(name)) {
			return target.getThreadProxyWindow();
		}

		final AtomicReference<Object> ret = new AtomicReference<Object>();
		final AtomicReference<Throwable> th = new AtomicReference<Throwable>();

		berkelium.executeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					ret.set(method.invoke(target, args));
				} catch (Throwable t) {
					th.set(t);
				}
			}
		});

		Throwable t = th.get();
		if (t != null) {
			throw t;
		}

		return ret.get();
	}

	public Window getProxy() {
		return proxy;
	}
}
