package com.example.vkr2.DataBase.Measurements

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity (tableName = "BodyMeasurements")
data class BodyMeasurementsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val neck: Int? = null,
    val shoulders: Int? = null,
    val chest: Int? = null,
    val waist: Int? = null,
    val pelvis: Int? = null,
    val bedroLeft: Int? = null,
    val ikriLeft: Int? = null,
    val begroRight: Int? = null,
    val ikriRight: Int? = null,
    val forearmsLeft: Int? = null,
    val forearmsRight: Int? = null,
    val bicepsLeft: Int? = null,
    val bicepsRight: Int? = null,
    val tricepsLeft: Int? = null,
    val tricepsRight: Int? = null
)
