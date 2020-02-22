package com.udacity.gradle.fitnessapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    List<UserProfile> loadAllTasks();

    @Query("SELECT gender FROM users ORDER BY id DESC LIMIT 1")
    String loadGender();

    @Query("SELECT goal FROM users ORDER BY id DESC LIMIT 1")
    int loadGoal();

    @Query("SELECT weight FROM users ORDER BY id DESC LIMIT 1")
    int loadWeight();

    @Query("SELECT height FROM users ORDER BY id DESC LIMIT 1")
    int loadHeight();

    @Insert
    void insertUser(UserProfile userProfile);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUser(UserProfile userProfile);

    @Delete
    void deleteUser(UserProfile userProfile);
}
