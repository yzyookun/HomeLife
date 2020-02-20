package cn.hbjcxy.ns.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IotMessage implements Serializable {

    private String clientId;
    private String content;

}
