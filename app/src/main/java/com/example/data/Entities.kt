package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val date: String, // YYYY-MM-DD
    val priority: String = "Medium", // Low, Medium, High
    val category: String = "Personal", // Work, Health, Personal, Finance
    val reminderTime: String? = null,
    val isRecurring: Boolean = false
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val isIncome: Boolean,
    val category: String, // Maaş, Gıda, Eğlence, Kira, Yatırım, Fatura, Diğer
    val description: String,
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey val date: String, // YYYY-MM-DD (acts as primary key since 1 entry per day)
    val stepsCount: Int = 0,
    val caloriesBurned: Int = 0,
    val waterMl: Int = 0,
    val focusMinutes: Int = 0,
    val sleepMinutes: Int = 0,
    val activeWorkoutType: String? = null, // e.g. "Chest", "Legs", "Cardio"
    val completedHabitsJson: String = "" // comma-separated or simple list "Water,Book,Sport"
)
