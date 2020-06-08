package com.lets.service.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MyLocation.class}, version = 1, exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {
    public abstract LocationDao dao();
}
