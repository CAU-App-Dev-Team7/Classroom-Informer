package com.example.classroominformer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.classroominformer.ui.*
import com.example.classroominformer.ui.home.*
import com.example.classroominformer.ui.login.LoginScreen
import com.example.classroominformer.ui.login.UserRole
import com.example.classroominformer.ui.theme.ClassroomInformerTheme

@Composable
fun ClassroomInformerApp() {

    val navController = rememberNavController()

    ClassroomInformerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {

                // ------------------------------------------------------
                // LOGIN
                // ------------------------------------------------------
                composable("login") {
                    LoginScreen { email, _password, role ->

                        val userName = email.substringBefore("@")

                        val target = when (role) {
                            UserRole.Professor -> "professorMain/$userName"
                            UserRole.Student -> "studentMain/$userName"
                        }

                        navController.navigate(target) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                // ------------------------------------------------------
                // STUDENT MAIN
                // ------------------------------------------------------
                composable("studentMain/{userName}") { entry ->
                    val userName = entry.arguments?.getString("userName") ?: "guest"

                    StudentMainScreen(
                        userName = userName,
                        onSearchClick = {
                            navController.navigate("search/normal/$userName")
                        },
                        onTimetableClick = {
                            // Default room (change later)
                            navController.navigate("timetable/310/617")
                        },
                        onFavouritesClick = {
                            navController.navigate("favourites/$userName")
                        },
                        onMapClick = { navController.navigate("map") },
                        onNotificationsClick = {
                            navController.navigate("notifications/$userName")
                        },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // ------------------------------------------------------
                // PROFESSOR MAIN
                // ------------------------------------------------------
                composable("professorMain/{userName}") { entry ->
                    val userName = entry.arguments?.getString("userName") ?: "guest"

                    ProfessorMainScreen(
                        userName = userName,
                        onSearchClick = {
                            navController.navigate("search/normal/$userName")
                        },
                        onTimetableClick = {
                            navController.navigate("timetable/310/617")
                        },
                        onFavouritesClick = {
                            navController.navigate("favourites/$userName")
                        },
                        onReservationsClick = {
                            navController.navigate("search/reservation/$userName")
                        },
                        onMapClick = { navController.navigate("map") },
                        onNotificationsClick = {
                            navController.navigate("notifications/$userName")
                        },
                        onLogoutClick = {
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                // ------------------------------------------------------
                // MAP
                // ------------------------------------------------------
                composable("map") {
                    MapScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                // ------------------------------------------------------
                // UNIFIED SEARCH SCREEN
                // mode = normal / reservation
                // ------------------------------------------------------
                composable("search/{mode}/{userName}") { entry ->

                    val mode = entry.arguments?.getString("mode") ?: "normal"
                    val userName = entry.arguments?.getString("userName") ?: "guest"

                    val isReservation = mode == "reservation"

                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        isReservationMode = isReservation,
                        onRoomSelected = { roomId: Int ->
                            navController.navigate("roomDetail/$roomId")
                        }
                    )
                }

                // ------------------------------------------------------
                composable("roomDetail/{roomId}") { entry ->
                val roomId = entry.arguments?.getString("roomId")!!.toLong()

                RoomDetailScreen(
                    roomId = roomId,
                    onBack = { navController.popBackStack() },
                    onOpenTimetable = { building, room ->
                        navController.navigate("timetable/$building/$room")
                    }
                )
            }


                // ------------------------------------------------------
                // TIMETABLE SCREEN
                // ------------------------------------------------------
                composable("timetable/{building}/{room}") { entry ->
                    val building = entry.arguments?.getString("building")!!
                    val room = entry.arguments?.getString("room")!!

                    TimetableScreen(
                        buildingCode = building,
                        roomNumber = room,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // ------------------------------------------------------
                // FAVOURITES
                // ------------------------------------------------------
                composable("favourites/{userName}") { entry ->
                    val userName = entry.arguments?.getString("userName") ?: "guest"

                    FavouritesScreen(
                        userName = userName,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // ------------------------------------------------------
                // NOTIFICATIONS
                // ------------------------------------------------------
                composable("notifications/{userName}") { entry ->
                    val userName = entry.arguments?.getString("userName") ?: "guest"

                    NotificationsScreen(
                        userName = userName,
                        onBackClick = { navController.popBackStack() },
                        onNotificationClick = { noti ->
                            navController.navigate("notificationDetail/$userName/${noti.id}")
                        }
                    )
                }

                // ------------------------------------------------------
                // NOTIFICATION DETAIL
                // ------------------------------------------------------
                composable("notificationDetail/{userName}/{notificationId}") { entry ->
                    val userName = entry.arguments?.getString("userName") ?: "guest"
                    val notiId = entry.arguments?.getString("notificationId")!!.toLong()

                    NotificationDetailScreen(
                        userName = userName,
                        notificationId = notiId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
