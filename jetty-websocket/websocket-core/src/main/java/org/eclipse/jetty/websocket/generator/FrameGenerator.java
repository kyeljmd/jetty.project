package org.eclipse.jetty.websocket.generator;

import java.nio.ByteBuffer;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.websocket.api.WebSocketSettings;
import org.eclipse.jetty.websocket.frames.BaseFrame;

public abstract class FrameGenerator<T extends BaseFrame>
{
    private final ByteBufferPool bufferPool;
    private final WebSocketSettings settings;

    protected FrameGenerator(ByteBufferPool bufferPool, WebSocketSettings settings)
    {
        this.bufferPool = bufferPool;
        this.settings = settings;
    }

    public ByteBuffer generate(T frame)
    {
        ByteBuffer framing = ByteBuffer.allocate(16);

        byte b;

        // Setup fin thru opcode
        b = 0x00;
        b |= (frame.isFin()?0x80:0x00); // 1000_0000
        b |= (frame.isRsv1()?0x40:0x00); // 0100_0000
        b |= (frame.isRsv2()?0x20:0x00); // 0010_0000 TODO: validate?
        b |= (frame.isRsv3()?0x10:0x00); // 0001_0000 TODO: validate?
        b |= (frame.getOpCode().getCode() & 0x0F);
        framing.put(b);

        // is masked
        b = 0x00;
        b |= (frame.isMasked()?0x80:0x00);

        // payload lengths
        int payloadLength = frame.getPayloadLength();
        if (payloadLength >= 0x7F)
        {
            // we have a 64 bit length
            b |= 0x7F;
            framing.put(b);

            framing.putInt(payloadLength);
        }
        else if (payloadLength >= 0x7E)
        {
            // we have a 16 bit length
            b |= 0x7E;
            framing.put(b);

            framing.putShort((short)(payloadLength & 0xFFFF));
        }
        else
        {
            // we have a 7 bit length
            b |= (payloadLength & 0x7F);
            framing.put(b);
        }

        // masking key
        if (frame.isMasked())
        {
            // TODO: figure out maskgen
            framing.put(frame.getMask());
        }

        framing.flip(); // to figure out how many bytes are used

        // now the payload itself
        int buflen = frame.getPayloadLength() + framing.remaining();
        ByteBuffer buffer = ByteBuffer.allocate(buflen);
        // TODO: figure out how to get this from a bytebuffer pool

        // TODO see if we can avoid the extra buffer here, make convention of payload
        // call back into masking check/method on this class?
        
        // generate payload        
        ByteBuffer payloadBuffer = payload(frame);
        
        // insert framing
        buffer.put(framing);
        
        // mask it if needed
        if ( frame.isMasked() )
        {
            int size = payloadBuffer.remaining();
            byte[] mask = frame.getMask();
            int m = 0;
            for (int i=0;i<size;i++)
            {
                buffer.put((byte)(payloadBuffer.get() ^ mask[m++%4]));
            }
        }
        else
        {
            buffer.put(payloadBuffer);
        }
        
        return buffer;
    }

    public abstract ByteBuffer payload(T frame);

    protected ByteBufferPool getByteBufferPool()
    {
        return bufferPool;
    }
}