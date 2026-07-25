package com.heima.domain;

import java.util.ArrayList;

public class HeroCharacter extends Character {
    public ArrayList<String> skillList;

    public HeroCharacter() {
        super();
        skillList=new ArrayList<>();
    }

    public HeroCharacter(String name, int HP, int attack, int defense) {
        super(name, HP, attack, defense);
        skillList=new ArrayList<>();
    }

    public String showSkill(){
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < skillList.size(); i++) {
            sb.append(skillList.get(i));
            if(i<skillList.size()-1)
                sb.append(", ");
        }
        return sb.toString();
    }


}
