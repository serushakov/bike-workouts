package io.ushakov.bike_workouts.view_models

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.db.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class WorkoutListViewModel(private val workoutRepository: WorkoutRepository) : ViewModel(){

    val allWorkouts: LiveData<List<Workout>> = workoutRepository.allWorkout

    private val _workoutsByUserId = mutableStateOf(emptyList<WorkoutSummary>())
    val workoutsByUserId: State<List<WorkoutSummary>> = _workoutsByUserId


    private val _workoutId = MutableLiveData<Long>()
    val workoutId: LiveData<Long>
        get() = _workoutId

    fun insert(workout: Workout) = viewModelScope.launch {

        val newlyCreatedWorkoutId = async(Dispatchers.IO) {
            workoutRepository.insert(workout)
        }
        _workoutId.value = newlyCreatedWorkoutId.await()
    }

    fun getWorkoutsByUserId (userId: Long) = viewModelScope.launch {
        val workoutsByUserId = async(Dispatchers.IO) {
            workoutRepository.getWorkoutsByUserId(userId)
        }
        _workoutsByUserId.value = workoutsByUserId.await()
    }
}

class WorkoutListViewModelFactory(private val workoutRepository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutListViewModel(workoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}