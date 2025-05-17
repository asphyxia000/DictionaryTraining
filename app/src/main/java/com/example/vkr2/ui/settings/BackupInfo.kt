package com.example.vkr2.ui.settings

data class BackupInfo(
    val filename: String,
    val sizeInBytes: Long,
    val createdAt: Long,
    val trainingCount:Int
)
