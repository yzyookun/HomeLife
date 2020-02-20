package cn.hbjcxy.ns.entity;

import cn.hbjcxy.ns.configurer.datasource.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node extends BaseEntity {

    private String clientId;
    private String secret;

}
