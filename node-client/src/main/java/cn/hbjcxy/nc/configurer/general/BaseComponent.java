package cn.hbjcxy.nc.configurer.general;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseComponent {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ObjectMapper objectMapper;


}
