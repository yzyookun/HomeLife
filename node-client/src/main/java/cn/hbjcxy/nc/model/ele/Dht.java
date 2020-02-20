package cn.hbjcxy.nc.model.ele;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Dht extends Ele {

    private BigDecimal temperature;
    private BigDecimal humidity;

}
