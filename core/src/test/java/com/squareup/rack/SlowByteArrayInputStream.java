package com.squareup.rack;

import java.io.ByteArrayInputStream;

class SlowByteArrayInputStream extends ByteArrayInputStream {

    public SlowByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    @Override
    public final int read(byte[] b, int off, int len) {
        if (pos >= count) {
            return -1;
        }
        if ((pos + len) > count) {
            len = (count - pos);
        }
        if (len <= 0) {
            return 0;
        }
        // Ensure we only read two bytes per read
        len = Math.min(len, 2);
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }
}
