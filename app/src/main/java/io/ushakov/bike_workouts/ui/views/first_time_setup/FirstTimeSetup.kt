package io.ushakov.bike_workouts.ui.views.first_time_setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.ui.views.first_time_setup.components.*
import io.ushakov.bike_workouts.util.rememberApplication
import kotlinx.coroutines.launch

@Composable
fun FirstTimeSetup(onUserCreated: (id: Long) -> Unit) {
    val navController = rememberNavController()
    var name by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var weight by remember { mutableStateOf<Int?>(null) }

    var isSavingUser by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val application = rememberApplication()

    suspend fun saveUser() {
        isSavingUser = true

        val userId = application.userRepository.insert(
            User(
                firstName = name!!,
                age = age!!,
                weight = weight!!
            )
        )

        onUserCreated(userId)
    }


    NavHost(navController = navController, startDestination = "name") {
        composable("name") {
            Name(initialName = name) {
                name = it
                navController.navigate("measurements")
            }
        }
        composable("measurements") {
            Measurements(
                name ?: return@composable,
                age,
                weight
            ) { resultAge, resultWeight ->
                age = resultAge
                weight = resultWeight
                navController.navigate("permissions")
            }
        }
        composable("permissions") {
            Permissions {
                navController.navigate("finish")
            }
        }
        composable("finish") {
            BackHandler {
                navController.popBackStack("measurements", false)
            }

            Layout(
                titleText = "All done! 👏"
            ) {
                Spacer(Modifier.weight(1f))
                Button(
                    enabled = !isSavingUser,
                    modifier = Modifier.align(CenterHorizontally),
                    onClick = {
                    scope.launch {
                        saveUser()
                    }
                }) {
                    Text("Finish")
                }
            }
        }
    }
}


