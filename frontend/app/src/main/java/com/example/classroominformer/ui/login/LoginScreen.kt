package com.example.classroominformer.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classroominformer.R
import com.example.classroominformer.ui.components.TopBlueHeader
import com.example.classroominformer.ui.home.NotificationsScreen
import com.example.classroominformer.ui.login.UserRole


// Simple role enum – backend can also return this later
enum class UserRole {
    Professor,
    Student
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String, role: UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.Professor) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Blue header
        TopBlueHeader(title = "Classroom Informer")

        // Space under header
        Spacer(modifier = Modifier.height(40.dp))

        // 2. Mascot row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.mascot),
                contentDescription = null,
                modifier = Modifier
                    .height(180.dp)
                    .width(180.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Card with fields + login button
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(3.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Role selector (Professor / Student)
                Text(
                    text = "Login as",
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedRole == UserRole.Professor,
                        onClick = { selectedRole = UserRole.Professor },
                        label = { Text("Professor") }
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.Student,
                        onClick = { selectedRole = UserRole.Student },
                        label = { Text("Student") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email or ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        onLogin(email.trim(), password, selectedRole)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D8BFF)
                    )
                ) {
                    Text("Login", fontSize = 17.sp)
                }
            }
        }
    }
}
