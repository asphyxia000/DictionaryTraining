package com.example.vkr2.DataBase.MuscleGroup

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MuscleGroup")
data class MuscleGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val MuscleGroupsID:Int=0,

    val NameMuscleGroups:String
)