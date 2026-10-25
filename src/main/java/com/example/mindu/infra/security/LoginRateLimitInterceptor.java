package com.example.mindu.infra.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
@Component
public class LoginRateLimitInterceptor implements HandlerInterceptor {
    private static final int LIMIT = 10;
    private static final long WINDOW_MS = 60_000;
    private static final int MAX_IPS = 5_000;
    private final LinkedHashMap<String, Window> windows = new LinkedHashMap<>(64, .75f, true);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        long now = Instant.now().toEpochMilli();
        synchronized (windows) {
            Iterator<Map.Entry<String, Window>> iterator = windows.entrySet().iterator();
            while (iterator.hasNext()) if (now >= iterator.next().getValue().startedAt + WINDOW_MS) iterator.remove();
            String ip = request.getRemoteAddr();
            Window window = windows.get(ip);
            if (window == null) {
                if (windows.size() >= MAX_IPS) {
                    String oldestIp = windows.keySet().iterator().next();
                    windows.remove(oldestIp);
                }
                window = new Window(now);
                windows.put(ip, window);
            }
            if (window.attempts >= LIMIT)
                return reject(response, Math.max(1, (window.startedAt + WINDOW_MS - now + 999) / 1000));
            window.attempts++;
            return true;
        }
    }
    private boolean reject(HttpServletResponse response, long retryAfter) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(retryAfter));
        response.setContentType("application/json");
        response.getWriter().write("{\"erro\":\"Muitas tentativas. Tente novamente em instantes.\"}");
        return false;
    }
    private static final class Window {
        private final long startedAt;
        private int attempts;
        private Window(long startedAt) { this.startedAt = startedAt; }
    }
}
