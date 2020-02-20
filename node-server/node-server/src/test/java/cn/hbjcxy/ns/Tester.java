package cn.hbjcxy.ns;

import cn.hbjcxy.ns.configurer.third.AesComponent;
import cn.hbjcxy.ns.model.IotMessage;
import cn.hbjcxy.ns.model.IotMessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Tester {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;

    public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        IotMessageContent responseContent = new IotMessageContent();
        responseContent.setTraceId(UUID.randomUUID().toString());
        responseContent.setTimestamp(System.currentTimeMillis());
        responseContent.setAction("STATUS");
        responseContent.setData("{}");
        IotMessage responseMessage = new IotMessage();
        responseMessage.setClientId("node-server");
        responseMessage.setContent(new AesComponent().encodeWithBase64Key(new ObjectMapper().writeValueAsString(responseContent), "PWUhZ3SC0+dTldzgWYiAcg=="));
        System.out.println(new ObjectMapper().writeValueAsString(responseMessage));
    }

}
