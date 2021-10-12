package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.db.entity.*
import io.ushakov.bike_workouts.db.repository.*
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.DateDifference
import io.ushakov.bike_workouts.util.getDifferenceBetweenDates
import kotlinx.coroutines.*
import java.util.*

class WorkoutDataProcessor(
    private val workoutRepository: WorkoutRepository,
    private val heartRateRepository: HeartRateRepository,
    private val locationRepository: LocationRepository,
    private val summaryRepository: SummaryRepository,
    private val durationRepository: DurationRepository,
    private val coroutineScope: CoroutineScope,
) {
    private val workoutDistanceProcessor = WorkoutDistanceProcessor()
    private val workoutCaloriesProcessor = WorkoutCaloriesProcessor()
    private var workoutUser: User? = null

    private var activeWorkout: Workout? = null
    // TODO save this to state
    private var activeDuration: Duration? = null
    private var totalWorkoutDuration: Long = 0L

    private var currentWorkoutDistance: Double? = null
    private var workoutCalories: Int = 0

    private var lastSpeed: Float? = null

    companion object {
        private var instance: WorkoutDataProcessor? = null

        fun initialize(
            workoutRepository: WorkoutRepository,
            heartRateRepository: HeartRateRepository,
            locationRepository: LocationRepository,
            summaryRepository: SummaryRepository,
            durationRepository: DurationRepository,
            coroutineScope: CoroutineScope,
        ) {
            instance = WorkoutDataProcessor(
                workoutRepository,
                heartRateRepository,
                locationRepository,
                summaryRepository,
                durationRepository,
                coroutineScope,
            )
        }

        fun getInstance(): WorkoutDataProcessor {
            return instance!!
        }
    }

    fun processHeartRate(heartRateValue: Int) {
        val workout = activeWorkout ?: return
        val timestamp = Date()
        //Write HR
        coroutineScope.launch(Dispatchers.IO) {
            heartRateRepository.insert(
                HeartRate(
                    workout.id,
                    heartRateValue,
                    timestamp
                )
            )
        }
    }

    fun processLocation(location: Location) {
        val workout = activeWorkout ?: return
        val timestamp = Date()

        var speed = location.speed

        // Ignore locations when standing in same place
        if(speed < 1 && lastSpeed == 0f) {
            return
        } else if(speed < 1 && lastSpeed != 0f) {
            lastSpeed = 0f
            speed = 0f
        }

        Log.d("DBG", "currentWorkoutDistance: $currentWorkoutDistance meters")

        // Insert location
        coroutineScope.launch(Dispatchers.IO) {

            if (currentWorkoutDistance == null) {
                val summary = summaryRepository.getSummaryForWorkout(workout.id)
                currentWorkoutDistance = summary?.distance ?: 0.0
            }


            currentWorkoutDistance =
                workoutDistanceProcessor.latestDistance(location, currentWorkoutDistance!!)

            locationRepository.locationDao.insert(
                io.ushakov.bike_workouts.db.entity.Location(
                    workout.id,
                    location.latitude,
                    location.longitude,
                    speed,
                    location.altitude.toFloat(),
                    timestamp
                )
            )
            //update Summary table
            updateSummary()
        }

    }

    fun restoreWorkout(user: User, workout: Workout, summary: Summary) {
        workoutUser = user
        activeWorkout = workout

    }

    fun createWorkout(user: User, title: String, type: Int) {
        Log.d("DBG", "Adding workout to DB, userId: ${user.id}, title: $title, type: $type")

        workoutUser = user

        coroutineScope.launch(Dispatchers.IO) {

            val workout = Workout(
                userId = user.id,
                title = title,
                type = type,
                startAt = Date()
            )

            val workoutId = workoutRepository.insert(workout)

            workout.id = workoutId

            activeWorkout = workout

            //Create Summary
            summaryRepository.insert(
                Summary(
                    workoutId = workoutId,
                    distance = currentWorkoutDistance ?: 0.0,
                    kiloCalories = workoutCalories
                ))

            //Duration will be added into DB now and we will update this duration when workout pause
            //On resume, we will create a new duration and will update on pause
            //TODO create separate suspend function
            activeDuration = Duration(0, workoutId, workout.startAt, null)
            val durationId = durationRepository.insert(activeDuration!!)
            activeDuration!!.id = durationId
            //createNewDuration()
            //Log.d("DBG", "Duration created with $durationId id")
            //Log.d("DBG", "Workout created with $workoutId id")
        }
    }

    fun pauseWorkout() {
        val workout = activeWorkout ?: return
        val onGoingDuration = activeDuration ?: return



        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, false)


            //TODO remove it after saving into state or observable whatever.
            //get active duration from DB
            /*val getDurationJob = async {
                durationRepository.getById(activeWorkout!!.id)
            }*/

            //update it
            //TODO Use following line after saving durationToUpdate into state
            //activeDuration?.stopAt = Date()
            //val durationToUpdate = getDurationJob.await()
            //durationToUpdate.stopAt = Date()
            onGoingDuration.stopAt = Date()
            activeDuration!!.stopAt = Date()
            //TODO remove async after feature is tested
            val updateDurationJob = async { durationRepository.update(onGoingDuration) }
            if ( updateDurationJob.await() > 0) {
                //Log.d("DBG", "Duration updated at pause")
            } else {
                //Log.d("DBG", "Failed to update Duration")
            }
            //calculate it after stop time is written
            //TODO fetch all duration
            // calculate time duration of all
            // pass that time duration to calculateCalories
            totalWorkoutDuration = getWorkoutTotalDuration()
            //Log.d("DBG", "Total workout duration when paused: $totalWorkoutDuration")

            calculateCalories(totalWorkoutDuration)

            Log.d("DBG", "Workout paused")
        }
    }

    fun resumeWorkout() {
        val workout = activeWorkout ?: return
        ///var newDuration = activeDuration ?: return

        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, true)

            //TODO create separate suspend function
            val newDuration = Duration(0, workout.id, Date(), null)
            val durationId = durationRepository.insert(newDuration)
            newDuration.id = durationId
            activeDuration = newDuration
            //Log.d("DBG", "Duration created for resume workout with $durationId id")
            //Log.d("DBG", "Workout resumed")
        }
    }

    fun stopWorkout() {
        val workout = activeWorkout ?: return
        val onGoingDuration = activeDuration ?: return

        CoroutineScope(Dispatchers.IO).launch {
            onGoingDuration.stopAt = Date()
            //TODO remove async after feature is tested
            val updateDurationJob = async { durationRepository.update(onGoingDuration) }
            if ( updateDurationJob.await() > 0) {
                //Log.d("DBG", "Duration updated at Stop")
            } else {
                //Log.d("DBG", "Failed to update Duration")
            }
            getWorkoutTotalDuration()
            //Log.d("DBG", "Total workout duration when stoped: $totalWorkoutDuration")
            /*val workoutDurationJob = async {
                workoutRepository.getWorkoutDurations(workout.id)
            }

            val workoutDurationList = workoutDurationJob.await()
            Log.d("DBG", "No of durations ${workoutDurationList.duration?.size}")
            var totalWorkoutDuration: Long = 0L
            workoutDurationList.duration?.forEach {
                Log.d("DBG", "durations end time ${it.stopAt}")

                totalWorkoutDuration += it.stopAt?.time?.minus(it.startAt.time)!!
            }
            Log.d("DBG", "Total work out duration ${totalWorkoutDuration / 1000}")*/


            if (totalWorkoutDuration > Constants.MINIMUM_WORKOUT_DURATION_MS) {
                //Log.d("DBG", "totalWorkoutDuration $totalWorkoutDuration")


                //Log.d("DBG", "Stopping workout")

                //CoroutineScope(Dispatchers.IO).launch {
                    workoutRepository.captureWorkoutFinishDate(workout.id, onGoingDuration.stopAt!!)
                    //calculate it after stop time is written
                    calculateCalories(totalWorkoutDuration)
                    updateSummary()
                //}
            } else {
                //Log.d("DBG", "totalWorkoutDuration $totalWorkoutDuration")

                //Log.d("DBG", "Deleting workout")
                CoroutineScope(Dispatchers.IO).launch {
                    workoutRepository.deleteById(workout.id)
                }
            }

        }
        val finishTime = Date()


        /*CoroutineScope(Dispatchers.IO).launch {

        }*/
        //TODO create new time difference from All workout differences.
/*        val timeDifference = finishTime.time - workout.startAt.time

        Log.d("DBG", "timeDifference $timeDifference")

        if (timeDifference > Constants.MINIMUM_WORKOUT_DURATION_MS) {


            Log.d("DBG", "Stopping workout")

            CoroutineScope(Dispatchers.IO).launch {
                workoutRepository.captureWorkoutFinishDate(workout.id, finishAt = finishTime)
                //calculate it after stop time is written
                calculateCalories(finishTime)
                updateSummary()
            }
        } else {
            Log.d("DBG", "Deleting workout")
            CoroutineScope(Dispatchers.IO).launch {
                workoutRepository.deleteById(workout.id)
            }
        }*/

        activeWorkout = null
        currentWorkoutDistance = 0.0
    }


    private fun updateSummary() {
        val workout = activeWorkout ?: return

        coroutineScope.launch(Dispatchers.IO) {
            //Log.d("WorkoutDataProcessor", "updating summary with distance $currentWorkoutDistance")
            val summary = summaryRepository.getSummaryForWorkout(workout.id) ?: return@launch

            summaryRepository.update(Summary(
                id = summary.id,
                workoutId = activeWorkout?.id ?: return@launch,
                distance = currentWorkoutDistance ?: 0.0,
                kiloCalories = workoutCalories
            ))
        }
    }

    private fun calculateCalories(workoutDurationInMicroSeconds: Long) {
        val workout = activeWorkout ?: return
        val user = workoutUser ?: return

        workoutCalories =
            workoutCaloriesProcessor.getCalories(user, workout, workoutDurationInMicroSeconds)
    }
    /*private fun calculateCalories(endTime: Date) {
        val workout = activeWorkout ?: return
        val user = workoutUser ?: return

        workoutCalories =
            workoutCaloriesProcessor.getCalories(user, workout, endTime)
    }*/
    //TODO later
    private suspend fun createNewDuration() {
        //durationRepository.insert(Duration(0, workoutId, workout.startAt, null))
    }

    //TODO later
    private suspend fun getWorkoutTotalDuration() = withContext(Dispatchers.IO){
        val workout = activeWorkout

        val workoutDurationJob = async {
            workout?.let { workoutRepository.getWorkoutDurations(it.id) }
        }

        val workoutDurationList = workoutDurationJob.await()
        if (workoutDurationList != null) {
            //Log.d("DBG", "No of durations ${workoutDurationList.duration?.size}")
        }
        //var totalWorkoutDuration: Long = 0L
        workoutDurationList?.duration?.forEach {
            //Log.d("DBG", "durations end time ${it.stopAt}")

            totalWorkoutDuration += it.stopAt?.time?.minus(it.startAt.time)!!
        }
        //Log.d("DBG", "Total work out duration ${totalWorkoutDuration / 1000}")

        return@withContext totalWorkoutDuration
    }

    fun calculateTime(): String{

        var diff: DateDifference? = null
        /*if (activeDuration?.stopAt == null) {
            Log.d("DBG", "activeDuration Stop null. totalWorkoutDuration $totalWorkoutDuration activeDuration?.startAt? ${activeDuration?.startAt}")
            //diff = activeDuration?.let { getDifferenceBetweenDates((totalWorkoutDuration + it.startAt.time), Date().time) }
            diff = getDifferenceBetweenDates(Date(totalWorkoutDuration + activeDuration?.startAt?.time!!), Date())
        } else {
            Log.d("DBG", "activeDuration Stop not null. totalWorkoutDuration $totalWorkoutDuration activeDuration?.startAt? ${activeDuration?.startAt}")
            //diff = activeDuration?.let { it.stopAt?.let { it1 -> getDifferenceBetweenDates((totalWorkoutDuration + it.startAt.time), it1.time) } }
            //diff = activeDuration!!.stopAt?.let { getDifferenceBetweenDates(( activeDuration!!.startAt.time), it.time) }
            diff = getDifferenceBetweenDates(Date(totalWorkoutDuration + activeDuration?.startAt?.time!!),
                activeDuration?.stopAt!!
            )
        }*/

        if (activeDuration?.stopAt == null) {
            //Log.d("DBG", "activeDuration Stop null. totalWorkoutDuration $totalWorkoutDuration activeDuration?.startAt? ${activeDuration?.startAt}")
            Log.d("DBG", "activeDuration StopAt null. totalWorkoutDuration ${totalWorkoutDuration + (Date().time - activeDuration?.startAt!!.time)}")
            //diff = activeDuration?.let { getDifferenceBetweenDates((totalWorkoutDuration + it.startAt.time), Date().time) }
            //diff = getDifferenceBetweenDates(totalWorkoutDuration + activeDuration?.startAt?.time!!, Date().time)
            diff = getDifferenceBetweenDates(totalWorkoutDuration + (Date().time - activeDuration?.startAt!!.time))
        } else {
            Log.d("DBG", "activeDuration Stop not null. totalWorkoutDuration $totalWorkoutDuration activeDuration?.startAt? ${activeDuration?.startAt}")
            //diff = activeDuration?.let { it.stopAt?.let { it1 -> getDifferenceBetweenDates((totalWorkoutDuration + it.startAt.time), it1.time) } }
            //diff = activeDuration!!.stopAt?.let { getDifferenceBetweenDates(( activeDuration!!.startAt.time), it.time) }
            Log.d("DBG", "activeDuration StopAt null. totalWorkoutDuration $totalWorkoutDuration")

            /*diff = getDifferenceBetweenDates(totalWorkoutDuration + activeDuration?.startAt?.time!!,
                activeDuration?.stopAt!!.time
            )*/
            diff = getDifferenceBetweenDates(totalWorkoutDuration)
        }

        return "${diff.hours}:${
            diff.minutes.toString().padStart(2, '0')
        }:${diff.seconds.toString().padStart(2, '0')}"

    }
}