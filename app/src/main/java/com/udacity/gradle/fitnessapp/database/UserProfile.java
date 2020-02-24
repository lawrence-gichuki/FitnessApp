package com.udacity.gradle.fitnessapp.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "users")
public class UserProfile {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String gender;
    private int goal;
    private int weight;
    private int height;
    private Date updatedAt;

    @Ignore
    public UserProfile(String gender, int goal, int weight, int height, Date updatedAt) {
        this.gender = gender;
        this.goal = goal;
        this.weight = weight;
        this.height = height;
        this.updatedAt = updatedAt;
    }

    public UserProfile(int id, String gender, int goal, int weight, int height, Date updatedAt) {
        this.id = id;
        this.gender = gender;
        this.goal = goal;
        this.weight = weight;
        this.height = height;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getGender() {
        return gender;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public int getGoal() {
        return goal;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

}