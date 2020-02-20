package cn.hbjcxy.ns.configurer.third;

import cn.hbjcxy.ns.configurer.general.BaseComponent;
import cn.hbjcxy.ns.configurer.tools.AesUtils;
import cn.hbjcxy.ns.configurer.tools.HashUtils;
import org.springframework.stereotype.Component;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class AesComponent extends BaseComponent {

    public String seedBase64Key(String keySeed) throws NoSuchAlgorithmException {
        return HashUtils.base64(AesUtils.seedKey(keySeed));
    }

    public String encodeWithBase64Key(String content, String base64Key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        return HashUtils.base64(AesUtils.encode(content.getBytes(), HashUtils.base64(base64Key)));
    }

    public String decodeWithBase64Key(String content, String base64Key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        return new String(AesUtils.decode(HashUtils.base64(content), HashUtils.base64(base64Key)));
    }

    public String seedHexKey(String keySeed) throws NoSuchAlgorithmException {
        return HashUtils.hex(AesUtils.seedKey(keySeed));
    }

    public String encodeWithHexKey(String content, String hexKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        return HashUtils.hex(AesUtils.encode(content.getBytes(), HashUtils.hex(hexKey)));
    }

    public String decodeWithHexKey(String content, String hexKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        return new String(AesUtils.decode(HashUtils.hex(content), HashUtils.hex(hexKey)));
    }

}
