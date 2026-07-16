package com.example.infocard.scanner

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.infocard.theme.CardBorderGold
import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc

@Composable
fun CardScannerView(
    language: AppLanguage,
    progressPercent: Int,
    onClose: () -> Unit,
    onPreviewViewReady: (PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.background(Color.Black)) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val guideWidth = screenWidth * 0.85f
        val guideHeight = guideWidth / 1.58f

        // Camera Preview Feed
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    onPreviewViewReady(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent cutout overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gw = guideWidth.toPx()
            val gh = guideHeight.toPx()
            val gx = (w - gw) / 2
            val gy = (h - gh) / 2

            val cropRect = RoundRect(
                rect = Rect(gx, gy, gx + gw, gy + gh),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            val path = Path().apply {
                addRoundRect(cropRect)
            }

            // Draw semi-transparent black overlay outside the guide box
            clipPath(path, clipOp = ClipOp.Difference) {
                drawRect(
                    SolidColor(Color.Black.copy(alpha = 0.65f)),
                    size = Size(w, h)
                )
            }
        }

        // Gold Crop Border Frame
        Box(
            modifier = Modifier
                .size(width = guideWidth, height = guideHeight)
                .align(Alignment.Center)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = CardBorderGold.copy(alpha = 0.5f),
                    spotColor = CardBorderGold
                )
                .border(2.5.dp, CardBorderGold, RoundedCornerShape(14.dp))
        )

        // Cancel Close Button
        Text(
            text = Loc.CANCEL.get(language),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 20.dp, top = 54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .clickable { onClose() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .align(Alignment.TopStart)
        )

        // Instruction Text (above guide frame)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = guideHeight / 2 + 60.dp)
        ) {
            Text(
                text = Loc.SCANNER_INSTRUCTION.get(language),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(elevation = 2.dp)
                    .padding(horizontal = 24.dp)
            )
        }

        // Status Progress (below guide frame)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = guideHeight / 2 + 30.dp)
        ) {
            val statusText = if (progressPercent > 0) {
                if (language == AppLanguage.TR) "Kart taranıyor... %$progressPercent" else "Scanning card... $progressPercent%"
            } else {
                Loc.SCANNER_STEADY.get(language)
            }
            Text(
                text = statusText,
                color = CardBorderGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(elevation = 2.dp)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}
