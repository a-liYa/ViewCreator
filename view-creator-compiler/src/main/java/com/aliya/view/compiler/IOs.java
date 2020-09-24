package com.aliya.view.compiler;

import java.io.Closeable;
import java.io.IOException;

/**
 * IOs
 *
 * @author a_liYa
 * @date 2020/9/24 09:57.
 */
final class IOs {

    static void close(Closeable... closeables) {
        if (closeables == null) return;

        for (Closeable closeable : closeables) {
            if (closeable != null)
                try {
                    closeable.close();
                } catch (IOException e) {
                    // Ignore it.
                }
        }
    }
}
