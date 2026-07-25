package com.heima.ui;

import com.heima.domain.EnemyCharacter;
import com.heima.domain.HeroCharacter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Fighting {

    //启动游戏
    public void gameStart(String username){
        //1.显示标题
        System.out.println("欢迎" + username + "来到游戏！");
        //2.创建玩家角色+属性分配
        HeroCharacter player=createRole(username);
        //3.显示角色信息
        System.out.println("角色创建成功！");
        System.out.println("初始属性为："+player.show());
        System.out.println("技能列表为："+player.showSkill());
        //4.创建多个敌人的列表
        ArrayList<EnemyCharacter> enemies=new ArrayList<>();
        enemies.add(new EnemyCharacter("哥布林",80,10,5,"普通攻击"));
        enemies.add(new EnemyCharacter("幻影刺客",100,5,15,"快速攻击"));
        enemies.add(new EnemyCharacter("超级坦克",70,10,0,"防御姿态"));
        enemies.add(new EnemyCharacter("邪恶法师",60,15,10,"火球术"));
        //5.准备战斗
        int count=1;//记录第几个敌人战斗
        int wins=0;//记录胜利的敌人数量

        while(player.isAlive()){
            if(wins!=0){
                //获取到每一个敌人的信息，进行属性点增加
                for (int i = 0; i < enemies.size(); i++) {
                    EnemyCharacter c=enemies.get(i);
                    c.maxHP+=10;
                    c.HP=c.maxHP;
                    c.attack+=3;
                    c.defense+=2;
                    c.defending=false;
                }

            }
            //随机选取敌人
            Random r=new Random();
            int index=r.nextInt(enemies.size());
            EnemyCharacter enemy=enemies.get(index);
            System.out.println(enemy.show());

            //战斗开始
            System.out.println("------------------------------------------");
            System.out.println("第"+count+"个敌人："+enemy.name+"出现！");

            int round=1;
            while(player.isAlive()){
                System.out.println("---------------------第"+round+"轮---------------------");
                System.out.println(getBloodBar(player.name,player.HP,player.maxHP));
                System.out.println(getBloodBar(enemy.name,enemy.HP,enemy.maxHP));

                playerTurn(player,enemy);

                //判断敌方血量是否为0
                if(!enemy.isAlive()){
                    System.out.println(enemy.name+"已死亡！");
                    wins++;
                    break;
                }

                //敌人回合
                enemyTurn(enemy,player);

                //判断玩家血量是否为0
                if(!player.isAlive()){
                    System.out.println(player.name+"已死亡！");
                    System.out.println("你被"+enemy.name+"击败！游戏结束！");
                    break;
                }
                round++;


            }
            //战斗结束，恢复HP
            if(player.isAlive()){
                int healHP=r.nextInt(21)+20;
                player.heal(healHP);
                System.out.println("战斗结束，"+player.name+"恢复了"+healHP+"点HP！");
                System.out.println("当前胜场数为："+wins+"！");
                System.out.println("---------------------------------");
            }
            //胜利3场后获得属性提升
            if(player.isAlive()&&wins>0&&wins%3==0){
                System.out.println("你获得了属性提升！");
                player.maxHP+=30;
                player.attack+=5;
                player.defense+=3;
                System.out.println("最大生命值提升30！攻击力提升5！防御力提升3！");
                System.out.println("当前属性为："+player.show());
            }
            //询问玩家是否继续
            if (player.isAlive()) {
                System.out.println("是否继续游戏？y/n");
                Scanner sc = new Scanner(System.in);
                String choose=sc.next();
                if(choose.equalsIgnoreCase("n")){
                    System.out.println("游戏结束！");
                    break;
                }else if(choose.equalsIgnoreCase("y")){
                    count++;
                    continue;
                }else{
                    System.out.println("无效输入，默认游戏继续！");
                    count++;
                    continue;
                }
            }
        }
        //6.游戏最终结算
        System.out.println("=======================================================");
        System.out.println("游戏结束！");
        System.out.println("你的最终属性为："+player.show());
        System.out.println("你的最终胜场数为："+wins+"！");
        System.out.println("感谢游玩！");
    }

    //敌人回合
    private void enemyTurn(EnemyCharacter enemy, HeroCharacter player) {
        System.out.println("====="+enemy.name+"的回合====");
        //50%几率普通攻击，50%几率技能
        String action="普通攻击";
        //进行几率计算
        Random r=new Random();
        int num=r.nextInt(10);
        if(num<5){
            action="普通攻击";
        }else{
            action=enemy.skill;
        }

        switch (action){
            case"普通攻击":
                System.out.println(enemy.name+"使用普通攻击！");
                int damage1=calculateDamage(enemy.attack,player.defense);
                System.out.println(enemy.name+"对"+player.name+"造成了"+damage1+"点伤害！");
                player.takeDamage(damage1);
                break;
            case "快速攻击":
                System.out.println(enemy.name+"使用快速攻击！");
                int damage2=0;
                for (int i = 0; i < 2; i++) {
                    int temp=calculateDamage(enemy.attack/2,player.defense);
                    damage2+=temp;
                }
                System.out.println(enemy.name+"对"+player.name+"造成了"+damage2+"点伤害！");
                player.takeDamage(damage2);
                break;
            case "防御姿态":
                enemy.defending=true;
                System.out.println(enemy.name+"使用防御姿态！");
                break;
            case "火球术":
                System.out.println(enemy.name+"使用火球术！");
                int damage3=calculateDamage((int)(enemy.attack*1.8),player.defense);
                System.out.println(enemy.name+"对"+player.name+"造成了"+damage3+"点伤害！");
                player.takeDamage(damage3);
                break;
        }
    }


    //创建游戏角色
    //参数：用户名
    //返回值：玩家角色
    public HeroCharacter createRole(String username){
        System.out.println("正在创建角色...");
        System.out.println("角色名：" + username);

        //属性分配
        int points=20;

        //提示
        System.out.println("请分配属性(共20点)：");
        System.out.println("生命值（每点+10HP）");
        System.out.println("攻击力（每点+2Attack）");
        System.out.println("防御力（每点+1Defense）");

        Scanner sc = new Scanner(System.in);
        String[] attributes={"HP","Attack","Defense"};

        int[] values=new int [3];
        for(int i=0;i<attributes.length;i++){
            System.out.println("分配点数到"+attributes[i]+"（剩余点数："+points+"）");
            int input=sc.nextInt();

            if(input<0){
                System.out.println("无效输入！默认分配0点");
                input=0;
            }
            if(input>points){
                System.out.println("无效输入！剩余属性点全部分配到"+attributes[i]);
                input=points;
            }
            points=points-input;
            values[i]=input;
        }
        HeroCharacter player=new HeroCharacter(username,
                100+values[0]*10,
                10+values[1]*2,
                values[2]);
        player.skillList.add("普通攻击");
        player.skillList.add("强力一击");
        player.skillList.add("生命汲取");

        return player;

    }

    //打印双方血条
    public String getBloodBar(String name,int HP, int maxHP){
        int barLength=20;
        int filled=(int)(HP*1.0/maxHP*barLength);
        StringBuilder sb=new StringBuilder();
        sb.append(name).append("\t").append(": [");

        for (int i = 0; i < barLength; i++) {
            if(i<filled){
                sb.append("|");
            }else {
                sb.append("-");
            }
        }
        sb.append("]").append(HP).append("/").append(maxHP).append("HP");
        return sb.toString();
    }

    //玩家回合
    public void playerTurn(HeroCharacter player, EnemyCharacter enemy){
        System.out.println("====="+player.name+"的回合====");
        System.out.println("请选择技能：");
        System.out.println(player.showSkill());
        System.out.println("选择行动1-3");
        Scanner sc = new Scanner(System.in);
        String choose=sc.next();
        switch(choose){
            case"1":
                System.out.println(player.name+"使用普通攻击！");
                int damage=calculateDamage(player.attack,enemy.defense);
                System.out.println(player.name+"对"+enemy.name+"造成了"+damage+"点伤害！");
                enemy.takeDamage(damage);
                break;
            case"2":
                System.out.println(player.name+"使用强力一击！");
                if(player.HP>10){
                    player.takeDamage(10);
                    int damage2=calculateDamage((int)(player.attack*1.8),enemy.defense);
                    System.out.println("消耗10点hp，"+player.name+"对"+enemy.name+"造成了"+damage2+"点伤害！");
                    enemy.takeDamage(damage2);
                }else{
                    System.out.println("HP不足，无法使用强力一击！");
                }
                break;
            case"3":
                System.out.println(player.name+"使用生命汲取！");
                if(player.HP>10){
                    player.takeDamage(10);
                    Random r=new Random();
                    int heal=r.nextInt(20)+1;
                    player.heal(heal);
                    System.out.println("消耗10点hp，"+player.name+"恢复"+heal+"点hp！");
                }else{
                    System.out.println("HP不足，无法使用生命汲取！");
                }
                break;
            default:
                System.out.println("默认使用普通攻击");
        }
    }

    //计算造成的伤害
    public int calculateDamage(int attack, int defense){
        int damage=attack-defense;
        if(damage<1){
            damage=1;
        }
        return damage;
    }
}
