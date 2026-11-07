package com.babyygurll.store.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.ui.theme.BrandDarkNavy
import com.babyygurll.store.ui.theme.BrandPrimary
import com.babyygurll.store.ui.theme.SurfaceBorder
import com.babyygurll.store.ui.theme.TextSecondary

/**
 * Top App Header matching the quiet luxury design:
 * (BG) [BabyyGurll / B O U T I Q U E] [Bag Icon with badge]
 */
@Composable
fun BoutiqueHeader(
    cartBadgeCount: Int = 0,
    notificationBadgeCount: Int = 0,
    onBagClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLogoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left (BG) circular emblem
            Surface(
                color = Color.White,
                shape = CircleShape,
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onLogoClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = com.babyygurll.store.R.drawable.brand_logo_emblem),
                        contentDescription = "BabyyGurll Emblem",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center Brand Title & Sub-brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onLogoClick() }
            ) {
                Text(
                    text = "BabyyGurll",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = BrandDarkNavy
                )
                Text(
                    text = "B O U T I Q U E",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.5.sp,
                    color = TextSecondary
                )
            }

            // Right Action Icons: Notification Bell & Shopping Bag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (notificationBadgeCount > 0) {
                                Badge(
                                    containerColor = BrandPrimary,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                                ) {
                                    Text(
                                        text = if (notificationBadgeCount > 9) "9+" else "$notificationBadgeCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onBagClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (cartBadgeCount > 0) {
                                Badge(
                                    containerColor = BrandPrimary,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                                ) {
                                    Text(
                                        text = if (cartBadgeCount > 99) "99+" else "$cartBadgeCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = "Shopping Bag",
                            tint = BrandDarkNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
