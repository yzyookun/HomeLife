package cn.hbjcxy.nc.model.logic;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class IotPins {

    private Map<String, GpioPinDigitalOutput> lightPins;
    private Map<String, GpioPinPwmOutput> fanPins;

}
