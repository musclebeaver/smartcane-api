package com.smartcane.api.security.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM 유틸 (키 관리는 KMS/환경변수/SecretManager 권장)
 * - encryptToBase64(plaintext, key) -> "base64(iv|ciphertext|tag)"
 * - decryptFromBase64(token, key)  -> plaintext
 */
public final class CryptoUtil {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;       // 96-bit
    private static final int TAG_BITS = 128;    // 16 bytes

    private CryptoUtil(){}

    public static String encryptToBase64(byte[] plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[IV_LEN];
            new SecureRandom().nextBytes(iv);

            Cipher c = Cipher.getInstance(TRANSFORMATION);
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipher = c.doFinal(plaintext);

            // 패킹: iv | cipher (tag 포함)
            byte[] out = new byte[iv.length + cipher.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipher, 0, out, iv.length, cipher.length);

            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM encrypt 실패", e);
        }
    }

    public static byte[] decryptFromBase64(String token, SecretKey key) {
        try {
            byte[] pack = Base64.getDecoder().decode(token);
            if (pack.length <= IV_LEN) throw new IllegalArgumentException("잘못된 입력");

            byte[] iv = new byte[IV_LEN];
            byte[] cipher = new byte[pack.length - IV_LEN];
            System.arraycopy(pack, 0, iv, 0, IV_LEN);
            System.arraycopy(pack, IV_LEN, cipher, 0, cipher.length);

            Cipher c = Cipher.getInstance(TRANSFORMATION);
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return c.doFinal(cipher);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM decrypt 실패", e);
        }
    }
}
