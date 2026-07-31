package ucu.retojulio2026.talent.auth;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

final class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        try (InputStream inputStream = request.getInputStream()) {
            this.cachedBody = inputStream.readAllBytes();
        }
    }

    byte[] getCachedBody() {
        return cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {

        private final InputStream delegate;

        private CachedBodyServletInputStream(byte[] cachedBody) {
            this.delegate = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            try {
                return delegate.available() == 0;
            } catch (IOException ignored) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }
    }
}
