package com.babyygurll.store.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babyygurll.store.ui.theme.BrandDarkNavy
import com.babyygurll.store.ui.theme.BrandPrimary
import com.babyygurll.store.ui.theme.TextSecondary

/**
 * Stylized yarn ball logo and header text matching:
 * (🧶) BabyyGurll
 * BOUTIQUE • HANDMADE
 * Thoughtfully made. Beautifully yours.
 */
@Composable
fun YarnBoutiqueBranding(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Stylized yarn ball icon
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f
                val c = center

                // Outer Circle
                drawCircle(
                    color = BrandPrimary,
                    radius = r - 2f,
                    style = Stroke(width = 3.5f)
                )

                // Yarn skein crossing curves
                val p1 = Path().apply {
                    moveTo(c.x - r * 0.8f, c.y - r * 0.2f)
                    cubicTo(
                        c.x - r * 0.3f, c.y - r * 0.7f,
                        c.x + r * 0.3f, c.y - r * 0.7f,
                        c.x + r * 0.8f, c.y - r * 0.2f
                    )
                }
                drawPath(p1, color = BrandPrimary, style = Stroke(width = 3f))

                val p2 = Path().apply {
                    moveTo(c.x - r * 0.95f, c.y + r * 0.1f)
                    cubicTo(
                        c.x - r * 0.4f, c.y - r * 0.3f,
                        c.x + r * 0.4f, c.y - r * 0.3f,
                        c.x + r * 0.95f, c.y + r * 0.1f
                    )
                }
                drawPath(p2, color = BrandPrimary, style = Stroke(width = 3f))

                val p3 = Path().apply {
                    moveTo(c.x - r * 0.85f, c.y + r * 0.4f)
                    cubicTo(
                        c.x - r * 0.3f, c.y + r * 0.1f,
                        c.x + r * 0.3f, c.y + r * 0.1f,
                        c.x + r * 0.85f, c.y + r * 0.4f
                    )
                }
                drawPath(p3, color = BrandPrimary, style = Stroke(width = 3f))

                val p4 = Path().apply {
                    moveTo(c.x - r * 0.6f, c.y + r * 0.7f)
                    cubicTo(
                        c.x - r * 0.2f, c.y + r * 0.5f,
                        c.x + r * 0.2f, c.y + r * 0.5f,
                        c.x + r * 0.6f, c.y + r * 0.7f
                    )
                }
                drawPath(p4, color = BrandPrimary, style = Stroke(width = 3f))

                // Diagonal cross yarn
                val pDiag = Path().apply {
                    moveTo(c.x - r * 0.65f, c.y + r * 0.55f)
                    cubicTo(
                        c.x - r * 0.1f, c.y,
                        c.x + r * 0.2f, c.y - r * 0.3f,
                        c.x + r * 0.7f, c.y - r * 0.45f
                    )
                }
                drawPath(pDiag, color = BrandPrimary, style = Stroke(width = 2.5f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // BabyyGurll
        Text(
            text = "BabyyGurll",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BrandDarkNavy,
            letterSpacing = 0.3.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // BOUTIQUE • HANDMADE
        Text(
            text = "BOUTIQUE • HANDMADE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = BrandPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Thoughtfully made. Beautifully yours.
        Text(
            text = "Thoughtfully made. Beautifully yours.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

