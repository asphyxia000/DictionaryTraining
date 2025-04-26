package com.example.vkr2.DataBase.Trainings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "Trainings")
data class TrainingsEntity (
    @PrimaryKey(autoGenerate = true)
    val trainingId: Int = 0,
    val date: LocalDate,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var name: String,
    var comment: String
)