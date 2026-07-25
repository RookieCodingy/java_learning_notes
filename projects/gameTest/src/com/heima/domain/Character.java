package com.heima.domain;

public class Character {
    public String name;
    public int HP;
    public int maxHP;
    public int attack;
    public int defense;

    public Character() {
    }

    public Character(String name, int HP, int attack, int defense) {
        this.name = name;
        this.HP = HP;
        this.maxHP = HP;
        this.attack = attack;
        this.defense = defense;
    }

    //1.判断人物是否存活
    public boolean isAlive() {
        return this.HP > 0;
    }
    //2.恢复血量
    public void heal(int amount) {
        HP+=amount;
        if (HP > maxHP) {
            HP = maxHP;
        }
    }
    //3.受到伤害
    public void takeDamage(int damage){
        HP-=damage;
        if (HP < 0) {
            HP = 0;
        }
    }
    //4.展示人物属性
    public String show(){
        return "名称：" + name + " 血量：" + HP + " 攻击：" + attack + " 防御：" + defense;
    }
}
