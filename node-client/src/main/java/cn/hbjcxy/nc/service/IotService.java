package cn.hbjcxy.nc.service;

import cn.hbjcxy.nc.configurer.general.BaseService;
import cn.hbjcxy.nc.configurer.tools.RandomUtils;
import cn.hbjcxy.nc.model.ele.*;
import cn.hbjcxy.nc.model.logic.IotPins;
import cn.hbjcxy.nc.tools.ComputerMonitorUtils;
import cn.hbjcxy.nc.tools.NetUtils;
import com.pi4j.io.gpio.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@EnableScheduling
public class IotService extends BaseService {

    private final IotModel iotModel;
    private final IotPins iotPins;

    public IotService() {
        final GpioController gpio = GpioFactory.getInstance();
        //
        iotModel = new IotModel();
        iotPins = new IotPins();
        // 温湿度测量仪
        iotModel.setDhts(new ArrayList<>());
        {
            String uuid = UUID.randomUUID().toString();
            Dht dht = new Dht();
            dht.setUuid(uuid);
            dht.setPower(Boolean.TRUE);
            dht.setName("温湿度测量仪");
            dht.setTemperature(new BigDecimal("27.00"));
            dht.setHumidity(new BigDecimal("45.00"));
            iotModel.getDhts().add(dht);
        }

        iotModel.setLights(new ArrayList<>());
        iotPins.setLightPins(new HashMap<>());
        {
            // 增加卧室的灯
            String uuid = UUID.randomUUID().toString();
            Light badRoomLight = new Light();
            badRoomLight.setUuid(uuid);
            badRoomLight.setName("卧室");
            badRoomLight.setPower(Boolean.FALSE);
            iotModel.getLights().add(badRoomLight);
            iotPins.getLightPins().put(
                    uuid, gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.LOW)
            );
            // 增加客厅的灯
            uuid = UUID.randomUUID().toString();
            Light livingRoomLight = new Light();
            livingRoomLight.setUuid(uuid);
            livingRoomLight.setName("客厅");
            livingRoomLight.setPower(Boolean.FALSE);
            iotModel.getLights().add(livingRoomLight);
            iotPins.getLightPins().put(
                    uuid, gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW)
            );
        }
        iotModel.setFans(new ArrayList<>());
        iotPins.setFanPins(new HashMap<>());
        {
            // 增加风扇
            String uuid = UUID.randomUUID().toString();
            Fan fan = new Fan();
            fan.setUuid(uuid);
            fan.setName("风扇");
            fan.setPower(Boolean.FALSE);
            fan.setLevel(1);
            iotModel.getFans().add(fan);
            iotPins.getFanPins().put(
                    uuid, gpio.provisionPwmOutputPin(RaspiPin.GPIO_01, 0)
            );
        }
        iotModel.setAcs(new ArrayList<>());
        {
            // 增加空调
            Ac ac = new Ac();
            ac.setUuid(UUID.randomUUID().toString());
            ac.setName("空调");
            ac.setPower(Boolean.FALSE);
            ac.setTarget(26);
            iotModel.getAcs().add(ac);
        }
        iotModel.setChip(new Chip());
        iotModel.getChip().setRams(new LinkedList<>());
        IntStream.range(0, 8).forEach(i -> iotModel.getChip().getRams().add(new BigDecimal(0)));
        iotModel.setPcs(new ArrayList<>());
        {
            //
            Pc pc = new Pc();
            pc.setUuid(UUID.randomUUID().toString());
            pc.setName("书房");
            pc.setPower(Boolean.FALSE);
            pc.setMac("283F6946FFFF");
            iotModel.getPcs().add(pc);
            //
            Pc work = new Pc();
            work.setUuid(UUID.randomUUID().toString());
            work.setName("办公室");
            work.setPower(Boolean.FALSE);
            work.setMac("283F6946FFFF");
            iotModel.getPcs().add(work);
            //
            Pc bad = new Pc();
            bad.setUuid(UUID.randomUUID().toString());
            bad.setName("卧室");
            bad.setPower(Boolean.FALSE);
            bad.setMac("283F6946FFFF");
            iotModel.getPcs().add(bad);
        }
    }

    private IotModel copyModel() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(iotModel);
            }
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                    return (IotModel) ois.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized IotModel status() {
        return copyModel();
    }

    public synchronized IotModel setting(IotSetting setting) {
        // 灯
        if ("LIGHT".equals(setting.getModel())) {
            if (setting.getUuid() != null && setting.getPower() != null)
                iotModel.getLights().stream()
                        .filter(light -> light.getUuid().equals(setting.getUuid()))
                        .forEach(light -> light.setPower(setting.getPower()));
            GpioPinDigitalOutput pin = iotPins.getLightPins().get(setting.getUuid());
            if (pin != null)
                pin.setState(setting.getPower().booleanValue() ? PinState.HIGH : PinState.LOW);
        }
        // 风扇
        else if ("FAN".equals(setting.getModel())) {
            if (setting.getUuid() != null && setting.getPower() != null && setting.getValue() != null)
                iotModel.getFans().stream()
                        .filter(fan -> fan.getUuid().equals(setting.getUuid()))
                        .forEach(fan -> {
                            fan.setPower(setting.getPower());
                            fan.setLevel(setting.getValue());
                        });
            GpioPinPwmOutput pin = iotPins.getFanPins().get(setting.getUuid());
            if (pin != null) {
                if (setting.getPower()) {
                    switch (setting.getValue()) {
                        case 1:
                            pin.setPwmRange(100);
                            break;
                        case 2:
                            pin.setPwmRange(300);
                            break;
                        case 3:
                            pin.setPwmRange(700);
                            break;
                        case 4:
                            pin.setPwmRange(1200);
                            break;
                        default:
                            pin.setPwmRange(0);
                            break;
                    }
                } else {
                    pin.setPwmRange(0);
                }
            }
        }
        // 空调
        else if ("AC".equals(setting.getModel())) {
            if (setting.getUuid() != null && setting.getPower() != null && setting.getValue() != null)
                iotModel.getAcs().stream()
                        .filter(ac -> ac.getUuid().equals(setting.getUuid()))
                        .forEach(ac -> {
                            ac.setPower(setting.getPower());
                            ac.setTarget(setting.getValue());
                        });
        }
        // 远程唤醒
        else if ("WOL".equals(setting.getModel())) {
            if (setting.getUuid() != null)
                iotModel.getPcs().stream()
                        .filter(pc -> pc.getUuid().equals(setting.getUuid()))
                        .map(Pc::getMac)
                        .forEach(NetUtils::wol);
        }
        return copyModel();
    }

    public synchronized void changeStatus(Runnable runnable) {
        runnable.run();
    }

    @Scheduled(fixedDelay = 5000)
    public synchronized void dhtScheduled() {
        Dht dht = iotModel.getDhts().get(0);
        changeStatus(() -> {
            dht.setTemperature(RandomUtils.range(new BigDecimal("26.00"), new BigDecimal("28.00")));
            dht.setHumidity(RandomUtils.range(new BigDecimal("40.00"), new BigDecimal("43.00")));
        });
    }

    @Scheduled(fixedDelay = 5000)
    public synchronized void memScheduled() {
        final BigDecimal mem = BigDecimal.valueOf(ComputerMonitorUtils.getMemUsage());
        changeStatus(() -> {
            iotModel.getChip().getRams().removeFirst();
            iotModel.getChip().getRams().add(mem);
        });
    }

}
