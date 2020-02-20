package cn.hbjcxy.nc.service;


import cn.hbjcxy.nc.configurer.general.BaseComponent;
import cn.hbjcxy.nc.configurer.third.AesComponent;
import cn.hbjcxy.nc.model.IotMessage;
import cn.hbjcxy.nc.model.IotMessageContent;
import cn.hbjcxy.nc.model.ele.IotModel;
import cn.hbjcxy.nc.model.ele.IotSetting;
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

@Service
public class MessageListener extends BaseComponent {

    private final CallbackConnection conn;

    public MessageListener(@Value("${mqtt.host}") String mqttHost,
                           @Value("${mqtt.port}") Integer mqttPort,
                           @Value("${mqtt.username}") String mqttUsername,
                           @Value("${mqtt.password}") String mqttPassword,
                           @Value("${mqtt.client-id}") String mqttClientId,
                           @Value("${mqtt.client-secret}") String mqttClientSecret,
                           @Value("${mqtt.server-id}") String mqttServerId) throws URISyntaxException {
        Listener listener = new Listener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onPublish(UTF8Buffer utf8Buffer, Buffer buffer, Runnable runnable) {
                runnable.run();
                MessageListener.this.onPublish(buffer.toByteArray(), mqttServerId, mqttClientId, mqttClientSecret);
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        };

        Callback<Void> connectCallback = new Callback<>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("CONNECT SUCCESS");
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("CONNECT FAILURE");
            }
        };

        Callback<byte[]> subscribeCallback = new Callback<>() {
            @Override
            public void onSuccess(byte[] bytes) {
                System.out.println("SUBSCRIBE SUCCESS");
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("SUBSCRIBE FAILURE");
                throwable.printStackTrace();
            }
        };

        MQTT mqtt = new MQTT();
        mqtt.setHost(mqttHost, mqttPort);
        mqtt.setClientId(mqttClientId);
        if (StringUtils.hasText(mqttUsername))
            mqtt.setUserName(mqttUsername);
        if (StringUtils.hasText(mqttPassword))
            mqtt.setPassword(mqttPassword);
        CallbackConnection conn = mqtt.callbackConnection();
        conn.listener(listener);
        conn.connect(connectCallback);
        conn.subscribe(new Topic[]{new Topic(mqttClientId, QoS.AT_LEAST_ONCE)}, subscribeCallback);

        this.conn = conn;
    }

    private Callback c = new Callback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(Throwable throwable) {

        }
    };

    public void publish(String serverId, byte[] message) {
        conn.publish(serverId, message, QoS.AT_LEAST_ONCE, false, c);
    }


    @Autowired
    private AesComponent aesComponent;

    @Autowired
    private IotService iotService;

    public void onPublish(byte[] message, String serverId, String clientId, String secret) {
        try {
            logger.info("======================= 收到消息 =======================");
            logger.info("  MESSAGE : {}", new String(message));
            IotMessage iotMessage = objectMapper.readValue(message, IotMessage.class);
            if (!StringUtils.hasText(iotMessage.getClientId())) return;
            if (!StringUtils.hasText(iotMessage.getContent())) return;
            String contentString = aesComponent.decodeWithBase64Key(iotMessage.getContent(), secret);
            logger.info("  CONTENT : {}", contentString);
            IotMessageContent content = objectMapper.readValue(contentString, IotMessageContent.class);
            // 查询状态
            if ("STATUS".equals(content.getAction())) {
                IotModel iotModel = iotService.status();
                response(serverId, clientId, secret, content.getTraceId(), iotModel);
            }
            // 设置
            else if ("SETTING".equals(content.getAction())) {
                IotSetting setting = objectMapper.readValue(content.getData(), IotSetting.class);
                IotModel iotModel = iotService.setting(setting);
                response(serverId, clientId, secret, content.getTraceId(), iotModel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void response(String serverId, String clientId, String secret, String traceId, IotModel iotModel) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        IotMessageContent responseContent = new IotMessageContent();
        responseContent.setTraceId(traceId);
        responseContent.setTimestamp(System.currentTimeMillis());
        responseContent.setAction("RESPONSE");
        responseContent.setData(objectMapper.writeValueAsString(iotModel));
        IotMessage responseMessage = new IotMessage();
        responseMessage.setClientId(clientId);
        responseMessage.setContent(aesComponent.encodeWithBase64Key(objectMapper.writeValueAsString(responseContent), secret));
        publish(serverId, objectMapper.writeValueAsBytes(responseMessage));
    }
}
