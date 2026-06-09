package com.library.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.BlockingQueue;

/**
 * Wraps a real JDBC {@link Connection} using a dynamic proxy so that calling
 * {@code close()} does not actually close the physical connection — instead it
 * returns the connection to the pool for reuse. All other method calls are
 * delegated to the real connection unchanged.
 */
final class PooledConnectionProxy implements InvocationHandler {

    private final Connection realConnection;
    private final BlockingQueue<Connection> pool;

    private PooledConnectionProxy(Connection realConnection, BlockingQueue<Connection> pool) {
        this.realConnection = realConnection;
        this.pool = pool;
    }

    /**
     * Creates a proxy connection bound to the given pool.
     *
     * @param realConnection the underlying physical connection
     * @param pool           the pool to return the connection to on close
     * @return a proxy {@link Connection}
     */
    static Connection wrap(Connection realConnection, BlockingQueue<Connection> pool) {
        return (Connection) Proxy.newProxyInstance(
                PooledConnectionProxy.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionProxy(realConnection, pool));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("close".equals(method.getName())) {
            // Return to the pool instead of closing the physical connection.
            pool.offer((Connection) proxy);
            return null;
        }
        return method.invoke(realConnection, args);
    }
}