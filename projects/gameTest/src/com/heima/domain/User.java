package com.heima.domain;

import java.util.ArrayList;
import java.util.Random;

public class User {
    //id 用户名 密码 状态
    private String id;
    private String username;
    private String password;
    private boolean status;

    //用户无法设置id，是自动生成的，格式为heima+5个随机数
    public String creatID(){
        StringBuilder sb =new StringBuilder("heima");

        Random r=new Random();
        for (int i = 0; i < 5; i++) {
            int num=r.nextInt(10);
            sb.append(num);
        }
        return sb.toString();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public User() {
        id=creatID();

        //修改status的值
        status=true;
    }

    public User(String id, String username, String password, boolean status) {
        id=creatID();
        this.username = username;
        this.password = password;
        status=true;
    }

}
