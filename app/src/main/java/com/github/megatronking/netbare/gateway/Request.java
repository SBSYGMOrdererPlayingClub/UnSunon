/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare.gateway;

import com.github.megatronking.netbare.tunnel.Tunnel;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A client requester, it connects to the remote server tunnel directly. We can send packet to the
 * remote server using {@link #process(ByteBuffer)}.
 *
 * @author Megatron King
 * @since 2018-11-05 22:18
 */
public class Request extends SessionTunnelFlow {

    private Tunnel mTunnel;
    private Tunnel mResponseTunnel;

    public Request() {
    }

    public Request(Tunnel tunnel) {
        this.mTunnel = tunnel;
    }

    /**
     * Set the response tunnel (proxy tunnel) so that the injector can send
     * fake responses back to the client directly.
     */
    public void setResponseTunnel(Tunnel responseTunnel) {
        this.mResponseTunnel = responseTunnel;
    }

    /**
     * Send a raw byte buffer back to the client through the proxy tunnel.
     * Use this to inject fake HTTP responses when the remote server is unreachable.
     */
    public void respond(ByteBuffer buffer) throws IOException {
        if (mResponseTunnel != null) {
            mResponseTunnel.write(buffer);
        }
    }

    @Override
    public void process(ByteBuffer buffer) throws IOException {
        if (mTunnel != null) {
            mTunnel.write(buffer);
        }
    }

}
