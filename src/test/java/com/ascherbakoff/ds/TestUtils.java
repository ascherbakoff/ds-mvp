package com.ascherbakoff.ds;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestUtils {
    public static void fill(ByteBuffer buffer, byte b) {
        if (buffer.hasArray())
            Arrays.fill(buffer.array(), buffer.position(), buffer.remaining(), b);
        else {
            int position = buffer.position();
            int length = buffer.remaining();

            for (int i = position; i < length; i++)
                buffer.put(b);

            buffer.flip();
        }
    }

    public static boolean match(ByteBuffer buffer, byte b) {
        int position = buffer.position();
        int length = buffer.remaining();

        for (int i = position; i < length; i++)
            if (buffer.get(i) != b)
                return false;

        return true;
    }
}
