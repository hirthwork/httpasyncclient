/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.nio.conn;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.protocol.HttpContext;

/**
 * Represents a manager of persistent client connections.
 * <p/>
 * The purpose of an HTTP connection manager is to serve as a factory for new
 * HTTP connections, manage persistent connections and synchronize access to
 * persistent connections making sure that only one thread of execution can
 * have access to a connection at a time.
 * <p/>
 * Implementations of this interface must be thread-safe. Access to shared
 * data must be synchronized as methods of this interface may be executed
 * from multiple threads.
 */
public interface NHttpClientConnectionManager {

    /**
     * Returns a {@link Future} for a {@link NHttpClientConnection}.
     */
    Future<NHttpClientConnection> requestConnection(
            HttpRoute route, Object state,
            long connectTimeout, TimeUnit timeUnit,
            FutureCallback<NHttpClientConnection> callback);

    /**
     * Releases a connection for use by others.
     * You may optionally specify how long the connection is valid
     * to be reused.  Values <= 0 are considered to be valid forever.
     * If the connection is not marked as reusable, the connection will
     * not be reused regardless of the valid duration.
     *
     * If the connection has been released before,
     * the call will be ignored.
     *
     * @param conn      the connection to release
     * @param validDuration the duration of time this connection is valid for reuse
     * @param timeUnit the unit of time validDuration is measured in
     *
     * @see #closeExpiredConnections()
     */
    void releaseConnection(
            NHttpClientConnection conn, Object newState, long validDuration, TimeUnit timeUnit);

    Future<NHttpClientConnection> connect(
            NHttpClientConnection conn,
            HttpRoute route,
            int connectTimeout,
            FutureCallback<NHttpClientConnection> callback);

    void initialize(
            NHttpClientConnection conn,
            HttpRoute route,
            HttpContext context) throws IOException;

    void upgrade(
            NHttpClientConnection conn,
            HttpRoute route,
            HttpContext context) throws IOException;

    void routeComplete(
            NHttpClientConnection conn,
            HttpRoute route,
            HttpContext context);

    boolean isRouteComplete(NHttpClientConnection conn);

    /**
     * Closes idle connections in the pool.
     * Open connections in the pool that have not been used for the
     * timespan given by the argument will be closed.
     * Currently allocated connections are not subject to this method.
     * Times will be checked with milliseconds precision
     *
     * All expired connections will also be closed.
     *
     * @param idletime  the idle time of connections to be closed
     * @param tunit     the unit for the <code>idletime</code>
     *
     * @see #closeExpiredConnections()
     */
    void closeIdleConnections(long idletime, TimeUnit tunit);

    /**
     * Closes all expired connections in the pool.
     * Open connections in the pool that have not been used for
     * the timespan defined when the connection was released will be closed.
     * Currently allocated connections are not subject to this method.
     * Times will be checked with milliseconds precision.
     */
    void closeExpiredConnections();

    void execute(IOEventDispatch eventDispatch) throws IOException;

    /**
     * Shuts down this connection manager and releases allocated resources.
     * This includes closing all connections, whether they are currently
     * used or not.
     */
    void shutdown() throws IOException;

}