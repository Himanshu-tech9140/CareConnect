package com.careconnect.config;

import com.careconnect.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private final String secret;
    private final long ttlSeconds;
    public JwtService(@Value("${careconnect.jwt.secret}") String secret, @Value("${careconnect.jwt.ttl-seconds:28800}") long ttlSeconds) { this.secret=secret; this.ttlSeconds=ttlSeconds; }

    public String issue(AppUser user) {
        long now = Instant.now().getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + user.username + "\",\"uid\":" + user.id + ",\"role\":\"" + user.role + "\",\"patientId\":" + (user.patientId == null ? "null" : user.patientId) + ",\"iat\":" + now + ",\"exp\":" + (now + ttlSeconds) + "}");
        String unsigned = header + "." + payload;
        return unsigned + "." + sign(unsigned);
    }

    public DemoPrincipal parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !constantTime(sign(parts[0] + "." + parts[1]), parts[2])) return null;
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String,String> values = new HashMap<>();
            for (String field : json.replace("{", "").replace("}", "").replace("\"", "").split(",")) { String[] pair=field.split(":",2); if (pair.length==2) values.put(pair[0], pair[1]); }
            long exp = Long.parseLong(values.get("exp"));
            if (exp < Instant.now().getEpochSecond()) return null;
            Long patientId = "null".equals(values.get("patientId")) ? null : Long.valueOf(values.get("patientId"));
            return new DemoPrincipal(Long.valueOf(values.get("uid")), values.get("sub"), values.get("role"), patientId);
        } catch (Exception ignored) { return null; }
    }

    private String encode(String value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    private String sign(String value) { try { Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception exception) { throw new IllegalStateException(exception); } }
    private boolean constantTime(String left, String right) { return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8)); }
}
