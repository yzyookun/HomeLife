package cn.hbjcxy.ns.controller;

import cn.hbjcxy.ns.service.MessageListener;
import cn.hbjcxy.ns.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MessageListener messageListener;

    @Autowired
    private ResponseService responseService;

    @PostMapping("/status/{clientId}")
    public String status(@PathVariable String clientId) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        return responseService.getResponse(messageListener.status(clientId));
    }

    @PostMapping("/setting/{clientId}")
    public String status(@PathVariable String clientId, @RequestBody String setting) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        return responseService.getResponse(messageListener.setting(clientId, setting));
    }

}
