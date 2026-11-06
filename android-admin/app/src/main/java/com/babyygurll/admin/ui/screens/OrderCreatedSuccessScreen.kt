package com.babyygurll.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.admin.ui.theme.*

@Composable
fun OrderCreatedSuccessScreen(
    orderId: String,
    onViewOrder: (String) -> Unit,
    onBackToOrders: () -> Unit
) {
    Scaffold(
        containerColor = SoftIvory
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Checkmark Badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF))
                        .border(1.5.dp, Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = RoyalBlue,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Heading
                Text(
                    text = "Order created",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MidnightNavy,
                    fontSize = 26.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle
                val displayId = if (orderId.startsWith("#") || orderId.startsWith("BG")) orderId else "#$orderId"
                Text(
                    text = "Manual order $displayId is ready for confirmation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // View Order Button (Dark Navy)
                Button(
                    onClick = { onViewOrder(orderId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MidnightNavy)
                ) {
                    Text(
                        text = "View order",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Back to Orders Button (Outlined White)
                OutlinedButton(
                    onClick = onBackToOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = MidnightNavy
                    )
                ) {
                    Text(
                        text = "Back to orders",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MidnightNavy
                    )
                }
            }
        }
    }
}
