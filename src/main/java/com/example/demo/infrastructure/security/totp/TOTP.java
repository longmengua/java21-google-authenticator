package com.example.demo.infrastructure.security.totp;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class TOTP {

    /**
     * 產生隨機的 Base32 Secret 金鑰
     *
     * - 使用 SecureRandom 產生 160-bit (20 bytes) 隨機位元組
     * - 轉換為 Base32 字串（A–Z 與 2–7）
     * - 移除 '=' padding 與空白
     * - 轉為大寫，方便 Google Authenticator 掃描
     *
     * @return Base32 編碼的金鑰字串
     */
    static String generateSecretBase32() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits 是 TOTP 常見建議長度
        random.nextBytes(bytes);

        Base32 base32 = new Base32();
        return base32.encodeToString(bytes)
                .replace("=", "")      // 移除 padding
                .replaceAll("\\s+", "") // 移除空白
                .toUpperCase();         // 全部轉大寫
    }

    /**
     * 依照 TOTP 演算法產生 6 位數一次性密碼
     *
     * @param secret    Base32 金鑰字串
     * @param timeIndex 時間索引（通常為：System.currentTimeMillis() / 1000 / 30）
     * @return 六位數字組成的 TOTP
     */
    static String generateTOTP(String secret, long timeIndex) throws Exception {
        Base32 base32 = new Base32();
        byte[] key = base32.decode(secret);

        // 1. 把 timeIndex 轉成 8 bytes 陣列（Big-endian）
        byte[] data = new byte[8];
        long value = timeIndex;
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        // 2. 使用 HMAC-SHA1 計算雜湊
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        // 3. 動態截取 (Dynamic Truncation)
        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        // 4. 取餘數，限制為 6 位數
        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    /**
     * 驗證使用者輸入的 OTP 是否正確
     *
     * - 允許時間誤差 ±1 個區間（30 秒），避免使用者裝置與伺服器時鐘略有不同
     *
     * @param secret Base32 金鑰字串
     * @param code   使用者輸入的六位數 OTP
     * @return true 如果驗證通過；false 如果錯誤
     */
    static boolean verify(String secret, String code) throws Exception {
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        // 檢查當前時間區間、前一區間、下一區間
        for (long i = -1; i <= 1; i++) {
            String totp = generateTOTP(secret, timeIndex + i);
            if (totp.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
