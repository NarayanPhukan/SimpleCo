package com.babyygurll.store.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.R
import com.babyygurll.store.ui.theme.BrandDarkNavy
import com.babyygurll.store.ui.theme.BrandPrimary
import com.babyygurll.store.ui.theme.TextSecondary

/**
 * Official BabyyGurll Boutique Branding Logo:
 * BG
 * BABYYGURLL
 * BOUTIQUE
 */
@Composable
fun YarnBoutiqueBranding(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_logo_full),
            contentDescription = "BabyyGurll Boutique Logo",
            modifier = Modifier
                .width(180.dp)
                .height(124.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Thoughtfully made. Beautifully yours.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

