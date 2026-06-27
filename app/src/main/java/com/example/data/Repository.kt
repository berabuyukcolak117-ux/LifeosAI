package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifeos_settings")

class LifeRepository(
    private val context: Context,
    private val lifeDao: LifeDao
) {
    // --- Room Database APIs ---

    val allTasks: Flow<List<Task>> = lifeDao.getAllTasks()
    
    fun getTasksForDate(date: String): Flow<List<Task>> = lifeDao.getTasksForDate(date)

    suspend fun insertTask(task: Task) = lifeDao.insertTask(task)

    suspend fun deleteTask(task: Task) = lifeDao.deleteTask(task)

    suspend fun updateTaskCompletion(id: Int, isCompleted: Boolean) = lifeDao.updateTaskCompletion(id, isCompleted)


    val allTransactions: Flow<List<Transaction>> = lifeDao.getAllTransactions()

    suspend fun insertTransaction(transaction: Transaction) = lifeDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = lifeDao.deleteTransaction(transaction)


    fun getProgressForDate(date: String): Flow<DailyProgress?> = lifeDao.getProgressForDate(date)

    suspend fun getProgressForDateSync(date: String): DailyProgress? = lifeDao.getProgressForDateSync(date)

    suspend fun insertProgress(progress: DailyProgress) = lifeDao.insertProgress(progress)

    val allProgress: Flow<List<DailyProgress>> = lifeDao.getAllProgress()


    // --- Datastore Preferences Keys ---
    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_BIRTH_YEAR = intPreferencesKey("birth_year")
        private val KEY_HEIGHT = floatPreferencesKey("height")
        private val KEY_WEIGHT = floatPreferencesKey("weight")
        private val KEY_PRIMARY_GOAL = stringPreferencesKey("primary_goal")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_PROFILE_PHOTO = stringPreferencesKey("profile_photo")
        private val KEY_STREAK_COUNT = intPreferencesKey("streak_count")
        private val KEY_XP_POINTS = intPreferencesKey("xp_points")
        private val KEY_LEVEL = intPreferencesKey("level")
        private val KEY_WATER_GOAL = intPreferencesKey("water_goal")
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_CLOUD_SYNC_STATUS = stringPreferencesKey("cloud_sync_status")
    }

    // --- Datastore Preference Flow Observers ---

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOGGED_IN] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Bera"
    }

    val birthYear: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIRTH_YEAR] ?: 2000
    }

    val height: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_HEIGHT] ?: 180f
    }

    val weight: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_WEIGHT] ?: 75f
    }

    val primaryGoal: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRIMARY_GOAL] ?: "Kas yapmak"
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val profilePhoto: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_PROFILE_PHOTO]
    }

    val streakCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_STREAK_COUNT] ?: 3
    }

    val xpPoints: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_XP_POINTS] ?: 120
    }

    val level: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_LEVEL] ?: 1
    }

    val waterGoalMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_WATER_GOAL] ?: 2500
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PREMIUM] ?: false
    }

    val cloudSyncStatus: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLOUD_SYNC_STATUS] ?: "Offline Cache"
    }

    // --- Preferences Mutators ---

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED_IN] = loggedIn
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
        }
    }

    suspend fun setBirthYear(year: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIRTH_YEAR] = year
        }
    }

    suspend fun setHeight(h: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HEIGHT] = h
        }
    }

    suspend fun setWeight(w: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEIGHT] = w
        }
    }

    suspend fun setPrimaryGoal(goal: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRIMARY_GOAL] = goal
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setProfilePhoto(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri != null) {
                prefs[KEY_PROFILE_PHOTO] = uri
            } else {
                prefs.remove(KEY_PROFILE_PHOTO)
            }
        }
    }

    suspend fun incrementXp(amount: Int) {
        context.dataStore.edit { prefs ->
            val currentXp = prefs[KEY_XP_POINTS] ?: 120
            val newXp = currentXp + amount
            prefs[KEY_XP_POINTS] = newXp
            
            val currentLvl = prefs[KEY_LEVEL] ?: 1
            // Simple level system: 100 XP per level
            val targetLvl = (newXp / 100) + 1
            if (targetLvl > currentLvl) {
                prefs[KEY_LEVEL] = targetLvl
            }
        }
    }

    suspend fun incrementStreak() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_STREAK_COUNT] ?: 3
            prefs[KEY_STREAK_COUNT] = current + 1
        }
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = premium
        }
    }

    suspend fun setCloudSyncStatus(status: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOUD_SYNC_STATUS] = status
        }
    }
}
