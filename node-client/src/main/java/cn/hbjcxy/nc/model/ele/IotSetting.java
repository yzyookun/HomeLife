package cn.hbjcxy.nc.model.ele;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class IotSetting implements Serializable {

    private String model;
    private String uuid;
    private Boolean power;
    private Integer value;

}
