package cn.hbjcxy.nc.model.ele;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Ele implements Serializable {
    private String uuid;
    private String name;
    private Boolean power;
}
