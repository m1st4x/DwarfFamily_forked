package de.ninjasoft.dwarffamily;

import java.io.IOException;
import java.io.InputStream;

public class FilteredInputStream extends InputStream {
    private final InputStream wrapped;
    
    public FilteredInputStream(InputStream wrapped) {
        this.wrapped = wrapped;
    }
    
    @Override
    public int read() throws IOException {
        int data;
        while ((data = wrapped.read()) != -1) {
            // Specifically allow valid XML characters + specifically block 0x11
            if (data != 0x11 && isValidXmlChar(data)) {
                return data;
            }
            // Skip 0x11 and other invalid chars
        }
        return -1;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int pos = off;
        int count = 0;
        while (count < len) {
            int data = read();
            if (data == -1) {
                return count == 0 ? -1 : count;
            }
            b[pos++] = (byte) data;
            count++;
        }
        return count;
    }
    
    private boolean isValidXmlChar(int codePoint) {
        // Valid XML 1.0 characters (excluding 0x11 which we handle separately)
        return (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD) ||
               (codePoint >= 0x20 && codePoint <= 0xD7FF) ||
               (codePoint >= 0xE000 && codePoint <= 0xFFFD) ||
               (codePoint >= 0x10000 && codePoint <= 0x10FFFF);
    }
}
