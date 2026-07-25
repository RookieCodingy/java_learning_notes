package com.heima.ui;

import com.heima.domain.User;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Login {
    //这个方法表示登录注册的主界面
    public void start(){
        System.out.println("游戏界面打开了~");
        ArrayList<User> users=new ArrayList<>();
        while (true) {
            System.out.println("       欢迎来到xxx格斗游戏      ");
            System.out.println("请选择操作：1.登录 2.注册 3。退出");

            Scanner sc=new Scanner(System.in);
            String choose=sc.next();

            switch (choose) {
                case "1":
                    login(users);
                    break;
                case "2":
                    register(users);
                    break;
                case "3":
                    exit();
                    break;
                    default:
                        System.out.println("输入有误，请重新选择");
            }
        }
    }

    //登录的操作
    public void login(ArrayList<User> users){
        System.out.println("用户选择了登录");

        //1.键盘录入用户名
        Scanner sc=new Scanner(System.in);
        System.out.println("请输入用户名：");
        String username=sc.next();

        //2.判断用户名是否存在
        boolean isExist = true;
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (!user.getUsername().equals(username)){
                isExist = false;
                break;
            }
        }
        if (!isExist) {
            System.out.println("用户名不存在，请先注册");
            return;
        }
        //3.判断用户是否被禁用
        int index=findIndex(users,username);
        User u=users.get(index);
        if(!u.isStatus()){
            System.out.println("用户已禁用，请联系管理员");
            return;
        }
        //4.让用户录入验证码和密码
        for (int i=0;i<3;i++) {
            System.out.println("请输入密码：");
            String password=sc.next();

            while (true) {
                //先生成一个正确的验证码
                String rightCode=getCode();
                System.out.println("正确的验证码是："+rightCode);


                System.out.println("请输入验证码：");
                String code=sc.next();

                if(rightCode.equalsIgnoreCase(code)){
                    System.out.println("验证码正确");
                    break;
                }else{
                    System.out.println("验证码错误");
                    continue;
                }
            }

            if(password.equals(u.getPassword())){
                System.out.println("登录成功！");
                Fighting fg=new Fighting();
                fg.gameStart(username);
                break;
            }else{
                if(i==2){
                    u.setStatus(false);
                    System.out.println("密码错误，次数已耗尽，用户已禁用，请联系管理员");
                }else{
                    System.out.println("密码错误，请重新输入，还剩下"+(2-i)+"次机会");
                }
            }
        }
    }
    //注册的操作
    public void register(ArrayList<User> users){
        System.out.println("用户选择了注册");
        //1.创建user对象
        User u=new User();
        //2.键盘录入用户名
        Scanner sc=new Scanner(System.in);
        while (true) {
            System.out.println("请输入用户名：");
            String username=sc.next();
            //校验用户名是否符合要求
            //检验用户名长度
            if (!checkLen(3,16,username)){
                System.out.println("用户名长度不符合要求，长度应在3-16之间，请重新输入");
                continue;
            }
            //检验用户名格式
            if (!checkUsername(username)){
                System.out.println("用户名格式不符合要求，请重新输入");
                continue;
            }
            //检验用户名是否已存在
            boolean isExist = false;
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                if (user.getUsername().equals(username)){
                    isExist = true;
                    break;
                }
            }
            if (isExist) {
                System.out.println("用户名已存在，请重新输入");
                continue;
            }
            u.setUsername(username);
            break;
        }

        while (true) {
            System.out.println("请输入密码：");
            String password1=sc.next();
            System.out.println("请输入密码：");
            String password2=sc.next();
            //校验密码是否符合要求
            if (!checkLen(3,8,password1)){
                System.out.println("密码长度不符合要求，长度应在3-8之间，请重新输入");
                continue;
            }
            //只能是字母加数字的组合
            if (!checkPassword(password1)){
                System.out.println("密码格式不符合要求，请重新输入");
                continue;
            }
            //两次输入的密码一致
            if (!password1.equals(password2)){
                System.out.println("两次输入的密码不一致，请重新输入");
                continue;
            }
            u.setPassword(password1);
            break;
        }
        //4.将user对象添加到集合当中
        users.add(u);
        //5.提示成功
        System.out.println("用户"+u.getUsername()+"注册成功！");
    }
    //退出的操作
    public void exit(){
        System.out.println("用户选择了退出");
        System.exit(0); // 退出程序
    }
    //判断字符串长度是否符合要求




    public int[] getCount(String userInfo){
        int charCount=0;
        int intCount=0;
        int otherCount=0;
        for (int i = 0; i < userInfo.length(); i++) {
            char c = userInfo.charAt(i);
            if (Character.isLetter(c)){
                charCount++;
            }else if (Character.isDigit(c)){
                intCount++;
            }else {
                otherCount++;
            }
        }
        return new int[]{charCount,intCount,otherCount};
    }
    public boolean checkLen(int minLen,int maxLen,String str){
        return str.length()>=minLen&&str.length()<=maxLen;
    }
    public boolean checkUsername(String username){
        int[] arr = getCount(username);
        return arr[0]>0&&arr[1]>=0&&arr[2]==0;
    }
    public boolean checkPassword(String password){
        int[] arr = getCount(password);
        return arr[0]>0&&arr[1]>0&&arr[2]==0;
    }
    //寻找集合中username的索引
    public int findIndex(ArrayList<User> users,String username){
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUsername().equals(username)){
                return i;
            }
        }
        return -1;
    }
    //生成验证码
    public static String getCode(){
    /*
    长度为5
    由四位大写或小写字母和一味数字组成，同一个字母可以重复
    数字可以出现在任意位置
     */
        ArrayList<Character> code = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            code.add((char)('a' + i));
            code.add((char)('A' + i));
        }

        StringBuilder sb = new StringBuilder();
        Random r=new Random();
        for (int i = 0; i < 4; i++) {
            int index=r.nextInt(code.size());
            char c=code.get(index);
            sb.append(c);
        }

        sb.append(r.nextInt(10));

        int change=r.nextInt(sb.length());
        char temp=sb.charAt(change);
        sb.setCharAt(change, sb.charAt(sb.length()-1));
        sb.setCharAt(sb.length()-1, temp);
        return sb.toString();
    }
}

