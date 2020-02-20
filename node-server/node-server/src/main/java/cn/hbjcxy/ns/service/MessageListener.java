package cn.hbjcxy.ns.service;

import cn.hbjcxy.ns.configurer.general.BaseComponent;
import cn.hbjcxy.ns.configurer.third.AesComponent;
import cn.hbjcxy.ns.entity.Node;
import cn.hbjcxy.ns.model.IotMessage;
import cn.hbjcxy.ns.model.IotMessageContent;
import cn.hbjcxy.ns.service.cache.NodeCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class MessageListener extends BaseComponent {

    @Autowired
    private NodeCacheService nodeService;

    @Autowired
    private AesComponent aesComponent;

    @Autowired
    private ResponseService responseService;

    private final String mqttHost;
    private final Integer mqttPort;
    private final String mqttUsername;
    private final String mqttPassword;
    private final String mqttServerId;

    public MessageListener(@Value("${mqtt.host}") String mqttHost,
                           @Value("${mqtt.port}") Integer mqttPort,
                           @Value("${mqtt.username}") String mqttUsername,
                           @Value("${mqtt.password}") String mqttPassword,
                           @Value("${mqtt.server-id}") String mqttServerId) throws URISyntaxException {

        this.mqttHost = mqttHost;
        this.mqttPort = mqttPort;
        this.mqttUsername = mqttUsername;
        this.mqttPassword = mqttPassword;
        this.mqttServerId = mqttServerId;

        initListener();
        initMqtt();
    }

    private Listener listener;
    private Callback<Void> connectCallback;
    private Callback<byte[]> subscribeCallback;
    private Callback publishCallback;
    private CallbackConnection conn;

    private void initListener() {
        this.listener = new Listener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onPublish(UTF8Buffer utf8Buffer, Buffer buffer, Runnable runnable) {
                runnable.run();
                MessageListener.this.onPublish(buffer.toByteArray());
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        };
        this.connectCallback = new Callback<>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger.info("CONNECT SUCCESS");
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.info("CONNECT FAILURE");
            }
        };
        this.subscribeCallback = new Callback<>() {
            @Override
            public void onSuccess(byte[] bytes) {
                logger.info("SUBSCRIBE SUCCESS");
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.info("SUBSCRIBE FAILURE", throwable);
            }
        };
        this.publishCallback = new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        };
    }

    private void initMqtt() throws URISyntaxException {
        MQTT mqtt = new MQTT();
        mqtt.setHost(mqttHost, mqttPort);
        mqtt.setClientId(mqttServerId);
        if (StringUtils.hasText(mqttUsername))
            mqtt.setUserName(mqttUsername);
        if (StringUtils.hasText(mqttPassword))
            mqtt.setPassword(mqttPassword);
        this.conn = mqtt.callbackConnection();
        conn.listener(listener);
        conn.connect(connectCallback);
        conn.subscribe(new Topic[]{new Topic(mqttServerId, QoS.AT_LEAST_ONCE)}, subscribeCallback);
    }

    public void onPublish(byte[] message) {
        try {
            logger.info("======================= 收到消息 =======================");
            logger.info("{}", new String(message));
            // 判断消息是否有效
            IotMessage iotMessage = objectMapper.readValue(message, IotMessage.class);
            if (!StringUtils.hasText(iotMessage.getClientId())) return;
            if (!StringUtils.hasText(iotMessage.getContent())) return;
            Node node = nodeService.findByClientId(iotMessage.getClientId());
            if (node == null) return;
            // 消息解密还原
            String contentString = aesComponent.decodeWithBase64Key(iotMessage.getContent(), node.getSecret());
            logger.info("========= 内容解密 =========");
            logger.info("{}", contentString);
            IotMessageContent iotMessageContent = objectMapper.readValue(contentString, IotMessageContent.class);
            if ("RESPONSE".equals(iotMessageContent.getAction())) {
                responseService.putResponse(iotMessageContent.getTraceId(), iotMessageContent.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String clientId, byte[] message) {
        conn.publish(clientId, message, QoS.AT_LEAST_ONCE, false, publishCallback);
    }

    private String pm(String clientId, String action, String data) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        Node node = nodeService.findByClientId(clientId);
        if (node == null) throw new RuntimeException();
        String traceId = UUID.randomUUID().toString();
        IotMessageContent responseContent = new IotMessageContent();
        responseContent.setTraceId(traceId);
        responseContent.setTimestamp(System.currentTimeMillis());
        responseContent.setAction(action);
        responseContent.setData(data);
        IotMessage responseMessage = new IotMessage();
        responseMessage.setClientId(mqttServerId);
        responseMessage.setContent(aesComponent.encodeWithBase64Key(objectMapper.writeValueAsString(responseContent), node.getSecret()));
        publish(clientId, objectMapper.writeValueAsBytes(responseMessage));
        return traceId;
    }

    public String status(String clientId) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return pm(clientId, "STATUS", "{}");
    }

    public String setting(String clientId, String setting) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        return pm(clientId, "SETTING", setting);
    }
}
