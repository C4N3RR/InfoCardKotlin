package com.example.infocard.ui.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.CornerRadius

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infocard.data.Card
import com.example.infocard.data.CardStore
import com.example.infocard.theme.DarkBackground
import com.example.infocard.theme.GlowCyan
import com.example.infocard.theme.GlowPurple
import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MainWalletView(
    language: AppLanguage,
    cardStore: CardStore,
    roll: Double,
    pitch: Double,
    onSettingsClick: () -> Unit,
    onAddCardClick: () -> Unit,
    onEditCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cards by cardStore.cards.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var showCardDetails by remember { mutableStateOf(false) }

    // Toast message state
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var toastJob by remember { mutableStateOf<Job?>(null) }

    // Wobble animation in Edit mode
    val infiniteTransition = rememberInfiniteTransition(label = "EditWobbleTransition")
    val wobbleRotation by infiniteTransition.animateFloat(
        initialValue = -0.7f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WobbleRotation"
    )
    val wobbleOffset by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WobbleOffset"
    )

    // Reset editing on card count change
    LaunchedEffect(cards.size) {
        if (cards.isEmpty()) isEditing = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Glowing Ambient Backdrops
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenW = maxWidth
            val screenH = maxHeight
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = (-80).dp, y = 120.dp)
                    .blur(80.dp)
                    .clip(CircleShape)
                    .background(GlowPurple.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .offset(x = screenW - 200.dp, y = screenH - 350.dp)
                    .blur(90.dp)
                    .clip(CircleShape)
                    .background(GlowCyan.copy(alpha = 0.10f))
            )
        }

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header navigation area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Actions: Settings & Edit Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSettingsClick()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                            .shadow(elevation = 5.dp, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
                            .shadow(elevation = 5.dp, shape = RoundedCornerShape(20.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isEditing = !isEditing
                            }
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = if (isEditing) Loc.DONE_BUTTON.get(language) else Loc.EDIT_BUTTON.get(language),
                            color = if (isEditing) Color(0xFFFBBF24) else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Center Title
                Text(
                    text = Loc.WALLET_TITLE.get(language),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Right Actions: Details Eye Toggle & Add Card
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCardDetails = !showCardDetails
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                            .shadow(elevation = 5.dp, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (showCardDetails) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Show details",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddCardClick()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                            .shadow(elevation = 5.dp, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Cards ScrollView
            if (cards.isEmpty()) {
                // Empty State View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val strokeColor = Color.White.copy(alpha = 0.15f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAddCardClick()
                            }
                    ) {
                        // Dashed card outline
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()), 0f)
                            drawRoundRect(
                                color = strokeColor,
                                style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect),
                                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCardOff,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Loc.NO_CARDS_SAVED.get(language),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Loc.TAP_TO_ADD_CARD.get(language),
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                // Card Listing List
                val listState = rememberLazyListState()
                var draggedItemIdx by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableStateOf(0f) }

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    itemsIndexed(cards) { index, card ->
                        var isContextMenuOpen by remember { mutableStateOf(false) }

                        val isCurrentlyDragged = draggedItemIdx == index
                        val yOffset = if (isCurrentlyDragged) dragOffset.dp else 0.dp

                        val rotationModifier = if (isEditing && !isCurrentlyDragged) {
                            Modifier.graphicsLayer {
                                rotationZ = wobbleRotation
                                translationY = wobbleOffset * density
                            }
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(rotationModifier)
                                .offset(y = yOffset)
                                .pointerInput(isEditing) {
                                    if (!isEditing) return@pointerInput
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            draggedItemIdx = index
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.y / density

                                            // Reorder math
                                            val threshold = 195f + 20f // card height + spacing
                                            val targetOffset = dragOffset / threshold
                                            val targetIndex = (index + targetOffset.roundToInt()).coerceIn(0, cards.size - 1)

                                            if (targetIndex != index && targetIndex != draggedItemIdx) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                cardStore.moveCard(index, targetIndex)
                                                draggedItemIdx = targetIndex
                                                dragOffset = 0f
                                            }
                                        },
                                        onDragEnd = {
                                            draggedItemIdx = null
                                            dragOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggedItemIdx = null
                                            dragOffset = 0f
                                        }
                                    )
                                }
                                .pointerInput(isEditing) {
                                    if (isEditing) return@pointerInput
                                    // Custom long press handler for normal mode context menu
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isContextMenuOpen = true
                                        },
                                        onDrag = { _, _ -> },
                                        onDragEnd = {},
                                        onDragCancel = {}
                                    )
                                }
                        ) {
                            CardView(
                                card = card,
                                language = language,
                                roll = roll,
                                pitch = pitch,
                                maskNumber = !showCardDetails,
                                isPreview = false,
                                onTap = {
                                    if (!isEditing) {
                                        // Copy to clipboard
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Card Number", card.number)
                                        clipboard.setPrimaryClip(clip)

                                        // Toast trigger
                                        toastJob?.cancel()
                                        toastMessage = Loc.TOAST_COPIED.get(language)
                                        showToast = true
                                        toastJob = coroutineScope.launch {
                                            delay(2000)
                                            showToast = false
                                        }
                                    }
                                }
                            )

                            // Dropdown Context Menu for Edit/Delete
                            DropdownMenu(
                                expanded = isContextMenuOpen,
                                onDismissRequest = { isContextMenuOpen = false },
                                modifier = Modifier.background(Color(0xFF1E1E22))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                            Text(Loc.EDIT_BUTTON.get(language), color = Color.White)
                                        }
                                    },
                                    onClick = {
                                        isContextMenuOpen = false
                                        onEditCardClick(card)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                            Text(if (language == AppLanguage.TR) "Sil" else "Delete", color = Color.Red)
                                        }
                                    },
                                    onClick = {
                                        isContextMenuOpen = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        cardStore.deleteCard(card)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Toast Notification overlay
        AnimatedVisibility(
            visible = showToast,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            ToastView(message = toastMessage)
        }
    }
}

@Composable
fun ToastView(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
            .background(Color(0xFF18181B).copy(alpha = 0.85f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF10B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = message,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
