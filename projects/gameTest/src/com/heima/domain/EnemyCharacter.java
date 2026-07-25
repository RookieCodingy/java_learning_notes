package com.heima.domain;

public class EnemyCharacter extends Character {
    public String skill;
    public boolean defending;

    public EnemyCharacter() {
        super();
    }
    public EnemyCharacter(String name, int HP, int attack, int defense, String skill) {
        super(name, HP, attack, defense);
        this.skill = skill;
    }

    @Override
    public void takeDamage(int damage) {
        //如果处于防御姿态，收到伤害减半
        if(defending){
            damage=damage/2>1?damage/2:1;
            System.out.println("防御姿态生效了！");
            defending=false;
        }
        super.takeDamage(damage);
    }
}
