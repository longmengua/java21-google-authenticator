package com.example.demo.infrastructure.security.totp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QRCodeGenerator {
    /**
     * 清理並驗證 Base32：去空白、轉大寫、去 '='、僅允許 A–Z/2–7
     */
    static String sanitizeBase32Secret(String raw) {
        String s = raw.replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT)
                .replace("=", "");
        if (!s.matches("[A-Z2-7]+")) {
            throw new IllegalArgumentException("Secret 必須為 Base32 (A–Z,2–7)，不可包含其他字元或符號");
        }
        return s;
    }

    /**
     * 產生「可被 Google Authenticator 掃描」的 otpauth URL
     * 例：otpauth://totp/MyApp:user%40example.com?secret=JBSWY3DPEHPK3PXP&issuer=MyApp&algorithm=SHA1&digits=6&period=30
     */
    static String buildTotpUrl(String issuer, String account, String secretBase32) {
        String secret = sanitizeBase32Secret(secretBase32);

        // label 放在 path：Issuer:Account（兩者皆需 URL encode；@ 會被轉成 %40）
        String label = url(issuer) + ":" + url(account);
        String iss = url(issuer);

        return "otpauth://totp/" + label
                + "?secret=" + secret
                + "&issuer=" + iss
                + "&algorithm=SHA1"
                + "&digits=6"
                + "&period=30";
    }

    static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * 產生 QR 圖檔（PNG）
     * 建議寬高 >= 320，MARGIN=1，ERROR_CORRECTION=M 增強掃描穩定度
     */
    static void generateQRCode(String text, String filePath, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
    }
}
