package com.example.demo.infrastructure.security.totp;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static com.example.demo.infrastructure.security.totp.QRCodeGenerator.generateQRCode;

public class Test {

    // 記住 secret，後面驗證會用到
    private static String secretGlobal;

    public static void main(String[] args) throws Exception {
        generateAndVerifyTotpFlow();

        // 模擬使用者輸入一次性驗證碼
        verifyWithUserInput();
    }

    /**
     * 建立 TOTP Secret、生成 otpauth URL，
     * 非同步產出 QR Code，並等待完成後再進行下一步
     */
    private static void generateAndVerifyTotpFlow() throws Exception {
        secretGlobal = TOTP.generateSecretBase32(); // Base32（A–Z,2–7），不含 '='
        System.out.println("Secret: " + secretGlobal);

        // 用 Builder 產生正確 otpauth URL
        String url = QRCodeGenerator.buildTotpUrl("WaltorApp", "waltor.huang@gmail.com", secretGlobal);
        System.out.println("Scan this QR with Google Authenticator: " + url);

        // 非同步產生 QR Code 圖片
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                generateQRCode(url, "qrcode.png", 320, 320);
                System.out.println("QRCode 已產生在 qrcode.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 等待圖片產生完成再繼續
        future.join();

        // 本地模擬驗證
        String code = TOTP.generateTOTP(secretGlobal, System.currentTimeMillis() / 1000 / 30);
        System.out.println("Current TOTP: " + code);
        boolean ok = TOTP.verify(secretGlobal, code);
        System.out.println("Verify result: " + ok);
    }

    /**
     * 從命令列讀取使用者輸入的 6 位數驗證碼並驗證
     */
    private static void verifyWithUserInput() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("請輸入 Google Authenticator 顯示的 6 位數驗證碼: ");
        String userCode = scanner.nextLine().trim();

        boolean result = TOTP.verify(secretGlobal, userCode);
        if (result) {
            System.out.println("✅ 驗證成功！");
        } else {
            System.out.println("❌ 驗證失敗，請確認輸入的代碼或時間是否同步。");
        }
    }
}
