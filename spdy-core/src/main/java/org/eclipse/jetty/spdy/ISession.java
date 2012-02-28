/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jetty.spdy;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.spdy.api.DataInfo;
import org.eclipse.jetty.spdy.api.Handler;
import org.eclipse.jetty.spdy.api.Session;
import org.eclipse.jetty.spdy.frames.ControlFrame;

public interface ISession extends Session
{
    public <C> void control(IStream stream, ControlFrame frame, long timeout, TimeUnit unit, Handler<C> handler, C context);

    public <C> void data(IStream stream, DataInfo dataInfo, long timeout, TimeUnit unit, Handler<C> handler, C context);

    public void execute(Runnable task);

    public int getWindowSize();

    public interface Controller<T>
    {
        public int write(ByteBuffer buffer, Handler<T> handler, T context);

        public void close(boolean onlyOutput);
    }
}
