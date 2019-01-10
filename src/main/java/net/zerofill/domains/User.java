package net.zerofill.domains;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User implements Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String discordUser;

    private int health;
    private int skill;

    public User() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDiscordUser() {
        return discordUser;
    }

    public void setDiscordUser(String discordUser) {
        this.discordUser = discordUser;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }
}
