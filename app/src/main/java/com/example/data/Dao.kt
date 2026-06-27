package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeDao {

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY date DESC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY id DESC")
    fun getTasksForDate(date: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskCompletion(id: Int, isCompleted: Boolean)


    // --- Transactions (Finance) ---
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)


    // --- Daily Progress (Fitness, Habits, Metrics) ---
    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    fun getProgressForDate(date: String): Flow<DailyProgress?>

    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    suspend fun getProgressForDateSync(date: String): DailyProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: DailyProgress)

    @Query("SELECT * FROM daily_progress ORDER BY date DESC")
    fun getAllProgress(): Flow<List<DailyProgress>>
}
