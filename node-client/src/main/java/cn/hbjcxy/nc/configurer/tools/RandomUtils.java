package cn.hbjcxy.nc.configurer.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RandomUtils {

    public static BigDecimal range(BigDecimal min, BigDecimal max) {
        return min.add(new BigDecimal(Math.random() * (max.subtract(min).doubleValue()))).setScale(2, RoundingMode.HALF_UP);
    }

}
