package cn.hbjcxy.ns.configurer.third;

import cn.hbjcxy.ns.configurer.general.BaseComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class RestComponent extends BaseComponent {

    @Autowired
    private RestTemplate restTemplate;

    public <T> T getRequest(String url, Class<T> responseType) throws IOException {
        logger.info("================== REST API BEGIN ==================");
        logger.info("   METHOD : {}", "GET");
        logger.info("      URL : {}", url);
        String response = restTemplate.getForObject(url, String.class);
        logger.info(" RESPONSE : {}", response);
        logger.info("================== REST API END ==================");
        return String.class.equals(responseType) ? (T) response : objectMapper.readValue(response, responseType);
    }

    private <T> T postRequest(String url, Object params, MediaType mediaType, Class<T> responseType) throws IOException {
        logger.info("================== REST API BEGIN ==================");
        logger.info("   METHOD : {}", "POST");
        logger.info("      URL : {}", url);
        logger.info("  REQUEST : {}", params);
        logger.info("     TYPE : {}", mediaType);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(mediaType);
        HttpEntity entity = new HttpEntity(params, requestHeaders);
        String response = restTemplate.postForObject(url, entity, String.class);
        logger.info(" RESPONSE : {}", response);
        logger.info("================== REST API END ==================");
        return String.class.equals(responseType) ? (T) response : objectMapper.readValue(response, responseType);
    }

    public <T> T postJson(String url, Object params, Class<T> responseType) throws IOException {
        return postRequest(url, params, MediaType.APPLICATION_JSON_UTF8, responseType);
    }

    public <T> T postFormData(String url, Object params, Class<T> responseType) throws IOException {
        return postRequest(url, params, MediaType.MULTIPART_FORM_DATA, responseType);
    }


    /**
     * FUCK PHP Software Engineer
     **/

    public String killBom(String content) {
        return content != null && content.startsWith("\ufeff") ? content.substring(1) : content;
    }

    public <T> T getRequestKillBom(String url, Class<T> responseType) throws IOException {
        String kb = killBom(getRequest(url, String.class));
        return String.class.equals(responseType) ? (T) kb : objectMapper.readValue(kb, responseType);
    }

}
