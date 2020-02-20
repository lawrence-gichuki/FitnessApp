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

    public void setId(int id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String description) {
        this.gender = description;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int priority) {
        this.height = priority;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int priority) {
        this.weight = priority;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int priority) {
        this.goal = priority;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
