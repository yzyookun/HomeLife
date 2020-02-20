package cn.hbjcxy.ns.entity;

import cn.hbjcxy.ns.configurer.datasource.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends BaseEntity {

    private String username;
    private String password;

}
