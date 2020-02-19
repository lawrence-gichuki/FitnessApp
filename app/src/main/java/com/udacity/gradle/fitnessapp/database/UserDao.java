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
@Query("SELECT * FROM users")
    List<UserProfile> loadAllTasks();

@Insert
    void insertUser(UserProfile userProfile);

@Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUser(UserProfile userProfile);

@Delete
    void deleteUser(UserProfile userProfile);
}
