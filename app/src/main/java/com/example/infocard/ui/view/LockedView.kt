package com.example.infocard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infocard.theme.DarkBackground
import com.example.infocard.theme.GlowPurple
import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc

@Composable
fun LockedView(
    language: AppLanguage,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Background ambient glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .blur(80.dp)
                .clip(CircleShape)
                .background(GlowPurple.copy(alpha = 0.12f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Glowing Lock Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                // Glow behind the icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .blur(10.dp)
                        .background(GlowPurple.copy(alpha = 0.5f))
                )

                // Shield/Lock icon with gradient
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield Lock",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(elevation = 10.dp, shape = CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF6D28D9), Color(0xFFDB2777))
                            ),
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Loc.LOCKED_TITLE.get(language),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = Loc.LOCKED_DESC.get(language),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), clip = false)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6D28D9), Color(0xFFDB2777))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = Loc.VERIFY_IDENTITY.get(language),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
