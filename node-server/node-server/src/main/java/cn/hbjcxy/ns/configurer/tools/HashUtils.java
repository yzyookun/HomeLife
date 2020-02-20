package cn.hbjcxy.ns.configurer.tools;

import java.security.MessageDigest;
import java.util.Base64;

public class HashUtils {

    private static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String hex(byte[] byteArray) {
        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

    public static byte[] hex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            String subStr = hex.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    public static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64(String string) {
        return Base64.getDecoder().decode(string);
    }

    public static String md5(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(content);
            return hex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
