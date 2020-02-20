package cn.hbjcxy.nc.model.ele;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;

@Getter
@Setter
public class Chip implements Serializable {

    private LinkedList<BigDecimal> rams;

}
