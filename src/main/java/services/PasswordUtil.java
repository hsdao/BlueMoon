package services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Tiện ích băm mật khẩu (salted SHA-256).
 *
 * <p>Định dạng lưu trong DB: {@code <saltHex>:<hashHex>} với
 * {@code hashHex = SHA-256(saltHex + plainPassword)}.</p>
 *
 * <p>Lý do thay cho việc lưu mật khẩu thô:
 * tránh lộ mật khẩu người dùng nếu DB bị rò rỉ.</p>
 *
 * <p>{@link #verify(String, String)} vẫn chấp nhận dữ liệu cũ (mật khẩu thô,
 * không có dấu ':') để không vỡ các tài khoản đã tạo trước khi nâng cấp.</p>
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 16;

    private PasswordUtil() {}

    /** Băm mật khẩu thô thành chuỗi {@code salt:hash} để lưu DB. */
    public static String hash(String plain) {
        String saltHex = toHex(randomSalt());
        return saltHex + ":" + sha256Hex(saltHex + plain);
    }

    /**
     * Kiểm tra mật khẩu thô có khớp với giá trị đã lưu hay không.
     *
     * @param stored giá trị trong DB (dạng {@code salt:hash}, hoặc mật khẩu thô cũ)
     * @param plain  mật khẩu người dùng nhập
     */
    public static boolean verify(String stored, String plain) {
        if (stored == null || plain == null) return false;

        int sep = stored.indexOf(':');
        if (sep < 0) {
            // Dữ liệu cũ chưa băm -> so sánh thô (tương thích ngược)
            return stored.equals(plain);
        }

        String saltHex = stored.substring(0, sep);
        String hashHex = stored.substring(sep + 1);
        return constantTimeEquals(hashHex, sha256Hex(saltHex + plain));
    }

    /** True nếu giá trị lưu đã ở dạng băm (có salt). Dùng để quyết định nâng cấp. */
    public static boolean isHashed(String stored) {
        return stored != null && stored.indexOf(':') >= 0;
    }

    // ---------------------------------------------------------------

    private static byte[] randomSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có trong JDK chuẩn -> không bao giờ xảy ra
            throw new IllegalStateException("Không tìm thấy thuật toán SHA-256", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /** So sánh chuỗi hex theo thời gian hằng số để chống timing attack. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
