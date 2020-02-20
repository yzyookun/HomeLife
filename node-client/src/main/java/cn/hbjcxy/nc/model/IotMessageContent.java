package cn.hbjcxy.nc.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IotMessageContent implements Serializable {

    private String traceId;
    private Long timestamp;
    private String action;
    private String data;

}
