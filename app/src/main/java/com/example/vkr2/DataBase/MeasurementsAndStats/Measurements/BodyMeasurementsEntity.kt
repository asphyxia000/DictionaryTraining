package com.example.vkr2.DataBase.Measurements

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity (tableName = "BodyMeasurements")
data class BodyMeasurementsEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val neck: Int, // Обхват шеи
    val shoulders: Int,  // Плеч
    val chest: Int,   // Грудь
    val waist: Int, // Талия
    val pelvis: Int, //Таз
    val bedroLeft:Int, //Бедро
    val ikriLeft:Int,//Икры
    val begroRight:Int,
    val ikriRight:Int,
    val forearmsLeft: Int,
    val forearmsRight: Int,
    val bicepsLeft: Int,
    val bicepsRight: Int,
    val tricepsLeft: Int,
    val tricepsRight: Int
    )