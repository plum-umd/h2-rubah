/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.value;

import rubah.Rubah;
import rubah.UpdateRequestedException;
import rubah.io.UpdatableInputStream;
import rubah.io.UpdatableOutputStream;

import java.nio.channels.SocketChannel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.h2.engine.SessionInterface;
import org.h2.message.TraceSystem;

/**
 * The transfer class is used to send and receive Value objects.
 * It is used on both the client side, and on the server side.
 */
public class TransferNonblocking extends Transfer {
    private SocketChannel channel;
    private UpdatableInputStream uin;

    /**
     * Create a new transfer object for the specified session.
     *
     * @param session the session
     */
    public TransferNonblocking(SessionInterface session) {
        super(session);
    }

    /**
     * Set the socket this object uses.
     *
     * @param s the socket
     */
    public void setChannel(SocketChannel c) {
        channel = c;
				this.setSocket(c.socket());
    }

    /**
     * Initialize the transfer object. This method will try to open an input and
     * output stream.
     */
    public void init() throws IOException {
				uin = new UpdatableInputStream(this.channel);
        in = new DataInputStream(uin);
        out = new DataOutputStream(new UpdatableOutputStream(this.channel));
    }

    /**
     * Close the transfer object and the socket.
     */
    public void close() {
        if (channel.isOpen()) {
            try {
                out.flush();
                channel.close();
            } catch (IOException e) {
                TraceSystem.traceThrowable(e);
            } finally {
								setSocket(null);
            }
        }
    }

		public int readOperation() throws IOException {
				while (true) {
					if (Rubah.isUpdateRequested()) {
						throw new UpdateRequestedException();
					}
					try {
						this.uin.peek();
						return readInt();
					} catch (rubah.io.InterruptedException e) {
						continue;
					}
				}
		}
}
