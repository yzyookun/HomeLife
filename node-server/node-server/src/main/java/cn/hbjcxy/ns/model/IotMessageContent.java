package cn.hbjcxy.ns.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class IotMessageContent implements Serializable {

    private String traceId;
    private Long timestamp;
    private String action;
    private String data;

}
