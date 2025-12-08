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
import com.example.classroominformer.data.RetrofitClient
import com.example.classroominformer.data.LoginRequest
import com.example.classroominformer.data.SignupRequest
import com.example.classroominformer.data.AuthManager
import kotlinx.coroutines.launch

// Simple role enum – backend can also return this later
enum class UserRole {
    Professor,
    Student
}

@Composable
fun LoginScreen(
    // ClassroomInformerApp에서 사용 중인 기존 시그니처
    onLogin: (email: String, password: String, role: UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.Professor) }

    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Blue header
        TopBlueHeader(title = "Classroom Informer")

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

        // 3. Card with fields + buttons
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
                    label = { Text("Email") },
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

                // 🔐 LOGIN: /auth/login 호출 후 토큰 저장 + 네비게이션 콜백
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null
                            try {
                                val api = RetrofitClient.authApi

                                val res = api.login(
                                    LoginRequest(
                                        email = email.trim(),
                                        password = password
                                    )
                                )

                                // 🔥 FastAPI에서 받은 토큰/유저 정보 저장
                                AuthManager.saveAuth(
                                    access = res.access_token,
                                    refresh = res.refresh_token,
                                    userIdValue = res.user_id
                                )

                                // 기존 네비게이션 흐름 유지
                                onLogin(email.trim(), password, selectedRole)

                                message = "로그인 성공"
                            } catch (e: Exception) {
                                message = "로그인 실패: ${e.localizedMessage ?: "알 수 없는 오류"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D8BFF)
                    )
                ) {
                    Text(
                        text = if (isLoading) "Loading..." else "Login",
                        fontSize = 17.sp
                    )
                }

                // ✏️ SIGN UP: /auth/signup 호출
                TextButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null
                            try {
                                val api = RetrofitClient.authApi
                                val res = api.signup(
                                    SignupRequest(
                                        email = email.trim(),
                                        password = password,
                                        name = null // 나중에 이름 필드 추가하면 연결
                                    )
                                )
                                message = "회원가입 성공: ${res.message}"
                            } catch (e: Exception) {
                                message = "회원가입 실패: ${e.localizedMessage ?: "알 수 없는 오류"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Sign up")
                }

                // 메시지 출력
                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
