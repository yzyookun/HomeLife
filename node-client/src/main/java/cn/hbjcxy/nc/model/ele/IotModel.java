package cn.hbjcxy.nc.model.ele;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class IotModel implements Serializable {

    private Boolean node = true;

    private List<Dht> dhts;
    private List<Light> lights;
    private List<Fan> fans;
    private List<Ac> acs;
    private List<Pc> pcs;

    private Chip chip;

}
