// Copyright (c) 2022 David Uselmann
package org.davu.opencl.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

/**
 *
 * The java.nio.Buffer hierarchy does not contain an abstract get() method
 * This class wraps the Buffer to provide a generic get. It will only work
 * for types defined in the enum BufferType. The most common use buffers are
 * defined.
 *
 * NOTE: It is not recommended to use this wrapper in a tight loop where performance is critical.
 *       It is most useful as a means to abstract setup and debugging info.
 *       In tight loops, use the actual buffers data type.
 *
 * @author davu
 *
 * @param <B> TODO need to investigate the use cases for proper definition.
 */
public class BufferWrapper<B extends Buffer> {

    /**
     * Helper mechanism to cast and return the Buffer elements.
     * @author davu
     */
    public enum BufferType {
        BYTE(ByteBuffer.class),
        CHAR(CharBuffer.class),
        SHORT(ShortBuffer.class),
        INTEGER(IntBuffer.class),
        LONG(LongBuffer.class),
        FLOAT(FloatBuffer.class),
        DOUBLE(DoubleBuffer.class),
        ;

        final Class<? extends Buffer> klass;

        BufferType(Class<? extends Buffer> klass) {
            this.klass = klass;
        }

        /**
         * Determines if the given buffer is of the enum's root data type
         * @param buffer a buffer to test
         * @return true of the given buffer extends its type
         */
        public boolean is(Buffer buffer) {
            return klass.isAssignableFrom(buffer.getClass());
        }

        /**
         * Convert from Buffer instance to enum instance
         * @param buffer the buffer to convert (or find) the supporting enum
         * @return enum that defines the given buffers data type
         */
        public static BufferType to(Buffer buffer) {
            for(BufferType type : values()) {
                if (type.is(buffer)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * The buffer instance wrapped by this helper.
     */
    final B buffer;
    /**
     * The type wrapper of the buffer instance.
     */
    final BufferType type;

    public BufferWrapper(B buffer) {
        this.buffer = buffer;
        this.type = BufferType.to(buffer);
    }

    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    public boolean isReadOnly() {
        return buffer.isReadOnly();
    }

    public boolean hasArray() {
        return buffer.hasArray();
    }

    public Object array() {
        return buffer.array();
    }

    public int arrayOffset() {
        return buffer.arrayOffset();
    }

    public boolean isDirect() {
        return buffer.isDirect();
    }

    /**
     * Since Buffer class does not have an abstract get() method,
     * it is necessary to examine the type to cast and call get from each implementation.
     *
     * @return instance of the data at the current position
     * as the wrapper class of the raw type the buffer contains.
     */
    public Object get() {
        switch(type) {
        case BYTE:
            return ((ByteBuffer)buffer).get();
        case CHAR:
            return ((CharBuffer)buffer).get();
        case DOUBLE:
            return ((DoubleBuffer)buffer).get();
        case FLOAT:
            return ((FloatBuffer)buffer).get();
        case INTEGER:
            return ((IntBuffer)buffer).get();
        case LONG:
            return ((LongBuffer)buffer).get();
        case SHORT:
            return ((ShortBuffer)buffer).get();
        default:
            return null;
        }
    }

    public static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }


}
