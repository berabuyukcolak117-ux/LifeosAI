package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LifeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = LifeRepository(application, db.lifeDao())

    // --- Active Application Routes ---
    val isOnboardingCompleted = repository.isOnboardingCompleted.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val isLoggedIn = repository.isLoggedIn.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val userName = repository.userName.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Bera"
    )
    val birthYear = repository.birthYear.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 2000
    )
    val height = repository.height.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 180f
    )
    val weight = repository.weight.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 75f
    )
    val primaryGoal = repository.primaryGoal.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Kas yapmak"
    )
    val isNotificationsEnabled = repository.isNotificationsEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val profilePhoto = repository.profilePhoto.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val streakCount = repository.streakCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 3
    )
    val xpPoints = repository.xpPoints.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 120
    )
    val level = repository.level.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1
    )
    val isPremium = repository.isPremium.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val cloudSyncStatus = repository.cloudSyncStatus.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Offline Cache"
    )

    // --- Date Context ---
    private val _selectedDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // --- Database Flows ---
    val currentDayTasks: Flow<List<Task>> = _selectedDate.flatMapLatest { date ->
        repository.getTasksForDate(date)
    }

    val allTasks: StateFlow<List<Task>> = repository.allTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val currentDayProgress: StateFlow<DailyProgress?> = _selectedDate.flatMapLatest { date ->
        repository.getProgressForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProgress: Flow<List<DailyProgress>> = repository.allProgress


    // --- UI/Wizard Wizard States ---
    private val _wizardStep = MutableStateFlow(1)
    val wizardStep = _wizardStep.asStateFlow()

    val wizardName = MutableStateFlow("Bera")
    val wizardBirthYear = MutableStateFlow(2000)
    val wizardHeight = MutableStateFlow(180f)
    val wizardWeight = MutableStateFlow(75f)
    val wizardGoal = MutableStateFlow("Kas yapmak")
    val wizardNotifications = MutableStateFlow(true)
    val wizardPhoto = MutableStateFlow<String?>(null)

    // --- AI Coach Chat States ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Merhaba! Ben senin kişisel AI Yaşam Koçun. Bugün seni daha disiplinli ve üretken yapmak için buradayım. Bana her şeyi sorabilirsin!",
                isUser = false
            )
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking = _isAiThinking.asStateFlow()

    // --- Onboarding Completion Trigger ---
    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

    // --- Login Actions ---
    fun performLogin(email: String) {
        viewModelScope.launch {
            repository.setLoggedIn(true)
            // Trigger automatic sync
            repository.setCloudSyncStatus("Synced")
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            repository.setLoggedIn(false)
            repository.setOnboardingCompleted(false)
            _wizardStep.value = 1
        }
    }

    // --- Wizard Actions ---
    fun advanceWizard() {
        if (_wizardStep.value < 8) {
            _wizardStep.value += 1
        } else {
            // Save wizard results to Datastore
            viewModelScope.launch {
                repository.setUserName(wizardName.value)
                repository.setBirthYear(wizardBirthYear.value)
                repository.setHeight(wizardHeight.value)
                repository.setWeight(wizardWeight.value)
                repository.setPrimaryGoal(wizardGoal.value)
                repository.setNotificationsEnabled(wizardNotifications.value)
                repository.setProfilePhoto(wizardPhoto.value)
                
                // Initialize daily progress in Database for today
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val existing = repository.getProgressForDateSync(today)
                if (existing == null) {
                    repository.insertProgress(
                        DailyProgress(
                            date = today,
                            stepsCount = 4320,
                            caloriesBurned = 180,
                            waterMl = 750,
                            focusMinutes = 25,
                            sleepMinutes = 420,
                            completedHabitsJson = "Su,Vitamin"
                        )
                    )
                }
                repository.setLoggedIn(true)
            }
        }
    }

    fun setWizardGoalChoice(goal: String) {
        wizardGoal.value = goal
    }

    fun toggleWizardNotification(enabled: Boolean) {
        wizardNotifications.value = enabled
    }

    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // --- Task Actions ---
    fun addTask(title: String, desc: String, category: String, priority: String) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = desc,
                date = _selectedDate.value,
                category = category,
                priority = priority
            )
            repository.insertTask(task)
            repository.incrementXp(15) // Gain XP for adding a plan
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val updated = !task.isCompleted
            repository.updateTaskCompletion(task.id, updated)
            if (updated) {
                repository.incrementXp(30) // Double XP for task completion!
                repository.incrementStreak()
            }
        }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Finance Actions ---
    fun addTransaction(amount: Double, isIncome: Boolean, category: String, desc: String) {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                isIncome = isIncome,
                category = category,
                description = desc,
                date = _selectedDate.value
            )
            repository.insertTransaction(transaction)
            repository.incrementXp(10)
        }
    }

    fun removeTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // --- Fitness / Health Actions ---
    fun addWater(ml: Int) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val current = repository.getProgressForDateSync(today) ?: DailyProgress(date = today)
            val updated = current.copy(waterMl = current.waterMl + ml)
            repository.insertProgress(updated)
            repository.incrementXp(5)
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val current = repository.getProgressForDateSync(today) ?: DailyProgress(date = today)
            val updated = current.copy(
                stepsCount = current.stepsCount + steps,
                caloriesBurned = current.caloriesBurned + (steps * 0.04).toInt()
            )
            repository.insertProgress(updated)
            repository.incrementXp(10)
        }
    }

    fun addFocusMinutes(minutes: Int) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val current = repository.getProgressForDateSync(today) ?: DailyProgress(date = today)
            val updated = current.copy(focusMinutes = current.focusMinutes + minutes)
            repository.insertProgress(updated)
            repository.incrementXp(minutes) // 1 XP per minute of focus!
        }
    }

    fun toggleHabit(habitName: String) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val current = repository.getProgressForDateSync(today) ?: DailyProgress(date = today)
            val habitsList = current.completedHabitsJson.split(",").filter { it.isNotEmpty() }.toMutableList()
            if (habitsList.contains(habitName)) {
                habitsList.remove(habitName)
            } else {
                habitsList.add(habitName)
                repository.incrementXp(20) // Gain XP on completing habit
            }
            val updated = current.copy(completedHabitsJson = habitsList.joinToString(","))
            repository.insertProgress(updated)
        }
    }

    fun logWorkout(workoutType: String) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val current = repository.getProgressForDateSync(today) ?: DailyProgress(date = today)
            val updated = current.copy(activeWorkoutType = workoutType)
            repository.insertProgress(updated)
            repository.incrementXp(50) // High XP for active workouts!
        }
    }

    // --- Chat Coach actions ---
    fun sendChatMessage(text: String, imageBase64: String? = null) {
        if (text.isBlank() && imageBase64 == null) return
        
        val userMsg = ChatMessage(text = text, isUser = true, imageUrl = imageBase64)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            // Build conversation payload
            val apiHistory = _chatMessages.value.dropLast(1).map { msg ->
                Content(parts = listOf(Part(text = msg.text)))
            }

            val coachResponse = GeminiHelper.generateCoachResponse(
                userName = userName.value,
                userGoal = primaryGoal.value,
                conversationHistory = apiHistory,
                latestPrompt = text
            )

            _chatMessages.value = _chatMessages.value + ChatMessage(text = coachResponse, isUser = false)
            _isAiThinking.value = false
        }
    }

    fun togglePremiumStatus() {
        viewModelScope.launch {
            val current = isPremium.value
            repository.setPremium(!current)
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.setCloudSyncStatus("Syncing...")
            kotlinx.coroutines.delay(2000)
            repository.setCloudSyncStatus("Synced")
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)

class LifeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LifeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LifeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
