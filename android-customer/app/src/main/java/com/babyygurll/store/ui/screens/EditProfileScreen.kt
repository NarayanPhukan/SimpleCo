package com.babyygurll.store.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.ui.theme.*
import com.babyygurll.store.ui.viewmodel.StoreViewModel

@Composable
fun EditProfileScreen(
    viewModel: StoreViewModel,
    onBack: () -> Unit,
    onNavigateToSavedAddresses: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var name by remember { mutableStateOf(currentUser?.name ?: "Ananya Sharma") }
    var email by remember { mutableStateOf(currentUser?.email ?: "ananya@example.com") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "+91 98765 43210") }
    var dob by remember { mutableStateOf("12 Aug 1998") }
    var gender by remember { mutableStateOf("Female") }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = BrandDarkNavy
                    )

                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = BrandPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BrandBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar & Name Card Section matching Screenshot
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    // Big Royal Blue Avatar with subtle glow ring
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .background(Color(0xFFDBEAFE), CircleShape)
                            .padding(4.dp)
                    ) {
                        Surface(
                            color = BrandDarkNavy,
                            shape = CircleShape,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = name.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                    .ifBlank { "AS" }
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Floating camera icon badge
                    Surface(
                        color = BrandPrimary,
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Change photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDarkNavy
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "MEMBER SINCE 2024",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Change Photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPrimary,
                        modifier = Modifier.clickable { }
                    )
                    Text(
                        text = "  |  ",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Remove",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.clickable { }
                    )
                }
            }

            // Card 1: PERSONAL INFORMATION matching Screenshot
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "PERSONAL INFORMATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        letterSpacing = 1.sp
                    )

                    // Full Name
                    Column {
                        Text("Full Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SurfaceBorder,
                                focusedBorderColor = BrandPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email Address
                    Column {
                        Text("Email Address", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = TextMuted)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SurfaceBorder,
                                focusedBorderColor = BrandPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Mobile Number
                    Column {
                        Text("Mobile Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            leadingIcon = {
                                Icon(Icons.Outlined.Phone, contentDescription = null, tint = TextMuted)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SurfaceBorder,
                                focusedBorderColor = BrandPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Date of Birth & Gender Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date of Birth", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gender", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedBorderColor = BrandPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Card 2: ACCOUNT & SECURITY matching Screenshot
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SurfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ACCOUNT & SECURITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 1: Mobile verified
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Phone,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Mobile verified",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = phone,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "VERIFIED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.7f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 2: Change Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToChangePassword() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Change Password",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = "Last updated 3 months ago",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.7f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 3: Saved Addresses
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSavedAddresses() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Saved Addresses",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandDarkNavy
                                )
                                Text(
                                    text = "Manage delivery locations",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons matching Screenshot
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Save Changes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BrandDarkNavy),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandDarkNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        onBack()
                    }
                ) {
                    Text(
                        text = "Sign out / View authentication",
                        color = BrandPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
