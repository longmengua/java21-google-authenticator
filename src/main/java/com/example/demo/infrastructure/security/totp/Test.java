package com.example.demo.infrastructure.security.totp;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

import static com.example.demo.infrastructure.security.totp.QRCodeGenerator.generateQRCode;

public class Test {

    public static void main(String[] args) throws Exception {
        // 你可以切換 testCase1 / testCase2 來試
        testCase1();
        // testCase2();
    }

    // 動態產生 secret，產 URL 與 QR，並本地驗證
    private static void testCase1() throws Exception {
        String secret = TOTP.generateSecretBase32(); // Base32（A–Z,2–7），不含 '='
        System.out.println("Secret: " + secret);

        // 用 Builder 產生正確 otpauth URL
        String url = QRCodeGenerator.buildTotpUrl("WaltorApp", "waltor.huang@gmail.com", secret);
        System.out.println("Scan this QR with Google Authenticator: " + url);

        // 產出 QR Code 圖檔（≥320 推薦）
        generateQRCode(url, "qrcode.png", 320, 320);
        System.out.println("QRCode 已產生在 qrcode.png");

        // 模擬驗證（本機 TOTP 與 verify）
        String code = TOTP.generateTOTP(secret, System.currentTimeMillis() / 1000 / 30);
        System.out.println("Current TOTP: " + code);
        boolean ok = TOTP.verify(secret, code);
        System.out.println("Verify result: " + ok);
    }

    // 使用固定 secret 測試：確認 GA 可掃（可換成你資料庫裡的 secret）
    private static void testCase2() throws Exception {
        String secret = "JBSWY3DPEHPK3PXP"; // Base32 範例
        String url = QRCodeGenerator.buildTotpUrl("MyApp", "user@example.com", secret);
        System.out.println("Scan this QR with Google Authenticator: " + url);

        generateQRCode(url, "qrcode_static.png", 320, 320);
        System.out.println("QRCode 已產生在 qrcode_static.png");
    }
}
