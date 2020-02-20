package cn.hbjcxy.nc.configurer.tools;

import cn.hbjcxy.nc.configurer.general.BaseComponent;
import org.springframework.util.StreamUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AesUtils extends BaseComponent {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;

    public static byte[] seedKey(String keySeed) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(keySeed.getBytes());
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    private static Key getKey(byte[] keyData) {
        return new SecretKeySpec(keyData, ALGORITHM);
    }

    private static Cipher getCipher(int mode, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, key);
        return cipher;
    }

    private static CipherOutputStream encrypt(OutputStream src, Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new CipherOutputStream(src, getCipher(Cipher.ENCRYPT_MODE, key));
    }

    private static CipherInputStream decrypt(InputStream src, Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return new CipherInputStream(src, getCipher(Cipher.DECRYPT_MODE, key));
    }

    private static byte[] encode(byte[] content, Key key) throws InvalidKeyException, IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (CipherOutputStream cos = encrypt(bos, key)) {
                cos.write(content);
            }
            return bos.toByteArray();
        }
    }

    public static byte[] encode(byte[] content, byte[] keyData) throws InvalidKeyException, IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        return encode(content, getKey(keyData));
    }

    private static byte[] decode(byte[] content, Key key) throws InvalidKeyException, IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(content)) {
            try (CipherInputStream cis = decrypt(bis, key)) {
                return StreamUtils.copyToByteArray(cis);
            }
        }
    }

    public static byte[] decode(byte[] content, byte[] keyData) throws InvalidKeyException, IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        return decode(content, getKey(keyData));
    }

}
