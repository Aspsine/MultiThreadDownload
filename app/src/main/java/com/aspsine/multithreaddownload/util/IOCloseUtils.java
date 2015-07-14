package com.aspsine.multithreaddownload.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Aspsine on 2015/7/10.
 */
public class IOCloseUtils {

    public static final void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            synchronized (IOCloseUtils.class) {
                closeable.close();
            }
        }
    }
}
