/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package net.daporkchop.porkbot.util.mcpinger.pcping;

import lombok.experimental.UtilityClass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@UtilityClass
public class MinecraftPingUtil {
    public final int PROTOCOL_VERSION = 315;
    public final int STATUS_HANDSHAKE = 1;

    public final byte PACKET_HANDSHAKE = 0x00;
    public final byte PACKET_STATUSREQUEST = 0x00;
    public final byte PACKET_PING = 0x01;

    public void io(final boolean b, final String m) throws IOException {
        if (b) {
            throw new IOException(m);
        }
    }

    /**
     * @author thinkofdeath
     * See: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
     */
    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();

            i |= (k & 0x7F) << j++ * 7;

            if (j > 5)
                throw new RuntimeException("VarInt too big");

            if ((k & 0x80) != 128)
                break;
        }

        return i;
    }

    /**
     * @throws IOException
     * @author thinkofdeath
     * See: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
     */
    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }
}
