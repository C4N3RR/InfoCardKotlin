package com.example.infocard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.infocard.data.Card
import com.example.infocard.data.CardProvider
import com.example.infocard.data.CardStore
import com.example.infocard.data.CardTheme
import com.example.infocard.theme.CardBorderGold
import com.example.infocard.theme.DarkBackground
import com.example.infocard.theme.GlowCyan
import com.example.infocard.theme.GlowPurple
import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditSheet(
    language: AppLanguage,
    cardToEdit: Card?,
    onSave: (Card) -> Unit,
    onScanRequest: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val haptic = LocalHapticFeedback.current

    // Form inputs state
    var cardName by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var cardNumber by remember { mutableStateOf(cardToEdit?.number ?: "") }
    var expiryDate by remember { mutableStateOf(cardToEdit?.expiry ?: "") }
    var cvv by remember { mutableStateOf(cardToEdit?.cvv ?: "") }
    var holderName by remember { mutableStateOf(cardToEdit?.holderName ?: "") }
    var provider by remember { mutableStateOf(cardToEdit?.provider ?: CardProvider.UNKNOWN) }
    var selectedTheme by remember { mutableStateOf(cardToEdit?.theme ?: CardTheme.PURPLE_PINK) }
    var customColorHex by remember { mutableStateOf(cardToEdit?.customColorHex) }

    var providerMenuExpanded by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }

    // Validation Highlights
    var highlightCardName by remember { mutableStateOf(false) }
    var highlightCardNumber by remember { mutableStateOf(false) }
    var highlightExpiryDate by remember { mutableStateOf(false) }
    var highlightCVV by remember { mutableStateOf(false) }

    val isFormValid = cardName.isNotBlank() &&
            cardNumber.replace(" ", "").length == 16 &&
            expiryDate.length == 5 &&
            (cvv.length == 3 || cvv.length == 4)

    // Form validation change trackers
    LaunchedEffect(cardName) { if (cardName.isNotBlank()) highlightCardName = false }
    LaunchedEffect(cardNumber) { if (cardNumber.replace(" ", "").length == 16) highlightCardNumber = false }
    LaunchedEffect(expiryDate) { if (expiryDate.length == 5) highlightExpiryDate = false }
    LaunchedEffect(cvv) { if (cvv.length >= 3) highlightCVV = false }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null,
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(DarkBackground)
        ) {
            // Ambient Glows
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .blur(70.dp)
                    .clip(CircleShape)
                    .background(GlowPurple.copy(alpha = 0.06f))
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header navigation bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Loc.CANCEL.get(language),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        }
                    )

                    Text(
                        text = if (cardToEdit == null) Loc.ADD_CARD_TITLE.get(language) else Loc.EDIT_CARD_TITLE.get(language),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (cardToEdit == null) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onScanRequest()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraEnhance,
                                contentDescription = "OCR Scan",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(36.dp))
                    }
                }

                // Live Card Preview
                // Live Card Preview (Matched size with main screen cards)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val previewCard = Card(
                        id = cardToEdit?.id ?: "",
                        name = cardName.ifBlank { Loc.CARD_NAME_DEFAULT.get(language) },
                        number = cardNumber.replace(" ", ""),
                        expiry = expiryDate,
                        cvv = cvv,
                        theme = selectedTheme,
                        customColorHex = customColorHex,
                        holderName = holderName,
                        provider = provider
                    )
                    CardView(
                        card = previewCard,
                        language = language,
                        roll = 0.0,
                        pitch = 0.0,
                        maskNumber = false,
                        isPreview = true
                    )
                }

                // Input fields block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card Name and Card Provider Row (combined to save vertical height)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card Name
                        Box(modifier = Modifier.weight(1.2f)) {
                            CustomInputField(
                                value = cardName,
                                onValueChange = { cardName = it },
                                placeholder = Loc.CARD_NAME_PLACEHOLDER.get(language),
                                icon = Icons.Default.CreditCard,
                                highlight = highlightCardName
                            )
                        }

                        // Provider Selector
                        Box(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                    .clickable { providerMenuExpanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CardProviderLogo(provider = provider, scale = 0.8f)
                                    Text(
                                        text = if (provider == CardProvider.UNKNOWN) Loc.SELECT.get(language) else provider.displayName(language),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = providerMenuExpanded,
                                onDismissRequest = { providerMenuExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E1E22))
                            ) {
                                CardProvider.values().forEach { prov ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CardProviderLogo(provider = prov, scale = 0.8f)
                                                Text(prov.displayName(language), color = Color.White)
                                            }
                                        },
                                        onClick = {
                                            provider = prov
                                            providerMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Cardholder Name
                    CustomInputField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        placeholder = Loc.CARD_HOLDER_PLACEHOLDER.get(language),
                        icon = Icons.Default.Person
                    )

                    // Card Number
                    CustomInputField(
                        value = cardNumber,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(16)
                            var formatted = ""
                            for (i in digits.indices) {
                                if (i > 0 && i % 4 == 0) {
                                    formatted += " "
                                }
                                formatted += digits[i]
                            }
                            cardNumber = formatted

                            // Auto-detect provider
                            val detected = CardProvider.detect(digits)
                            if (detected != CardProvider.UNKNOWN) {
                                provider = detected
                            }
                        },
                        placeholder = Loc.CARD_NUMBER_PLACEHOLDER.get(language),
                        icon = Icons.Default.Numbers,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        highlight = highlightCardNumber
                    )

                    // Expiry and CVV Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Expiry
                        Box(modifier = Modifier.weight(1f)) {
                            CustomInputField(
                                value = expiryDate,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }.take(4)
                                    expiryDate = if (digits.length > 2) {
                                        "${digits.substring(0, 2)}/${digits.substring(2)}"
                                    } else {
                                        digits
                                    }
                                },
                                placeholder = Loc.EXPIRY_PLACEHOLDER.get(language),
                                icon = Icons.Default.CalendarMonth,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                highlight = highlightExpiryDate
                            )
                        }

                        // CVV
                        Box(modifier = Modifier.weight(1f)) {
                            CustomInputField(
                                value = cvv,
                                onValueChange = { input ->
                                    cvv = input.filter { it.isDigit() }.take(4)
                                },
                                placeholder = Loc.CVV_PLACEHOLDER.get(language),
                                icon = Icons.Default.Lock,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                highlight = highlightCVV
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Card Color Theme selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Loc.CARD_COLOR_HEADER.get(language),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // preset gradients
                        items(CardTheme.values()) { theme ->
                            val isSelected = selectedTheme == theme && customColorHex == null
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = theme.colors,
                                            start = Offset(0f, 0f),
                                            end = Offset.Infinite
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        customColorHex = null
                                        selectedTheme = theme
                                    }
                            )
                        }

                        // Custom color picker rainbow circle
                        item {
                            val isCustomSelected = customColorHex != null
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                Color.Red, Color.Magenta, Color.Blue, Color.Cyan,
                                                Color.Green, Color.Yellow, Color.Red
                                            )
                                        )
                                    )
                                    .border(
                                        width = if (isCustomSelected) 2.5.dp else 0.dp,
                                        color = if (isCustomSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showColorPickerDialog = true
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add Color",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save button
                val saveColors = if (isFormValid) {
                    if (customColorHex != null) {
                        val baseColor = try {
                            val colorInt = customColorHex!!.trimStart('#').toLong(16)
                            Color(colorInt or 0xFF000000)
                        } catch (e: Exception) {
                            Color(0xFF6D28D9)
                        }
                        listOf(baseColor, baseColor.darkened())
                    } else {
                        selectedTheme.colors
                    }
                } else {
                    listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val cleanedNumber = cardNumber.replace(" ", "")
                            val savedCard = Card(
                                id = cardToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                                name = cardName,
                                number = cleanedNumber,
                                expiry = expiryDate,
                                cvv = cvv,
                                theme = selectedTheme,
                                customColorHex = customColorHex,
                                holderName = holderName.ifBlank { null },
                                provider = provider
                            )
                            onSave(savedCard)
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .border(
                                width = if (isFormValid) 0.dp else 1.dp,
                                color = if (isFormValid) Color.Transparent else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                brush = Brush.horizontalGradient(colors = saveColors),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Text(
                            text = Loc.SAVE.get(language),
                            color = if (isFormValid) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Custom Color Picker Dialog
    if (showColorPickerDialog) {
        val colorsPalette = listOf(
            "10B981", "3B82F6", "EF4444", "F59E0B", "EC4899", "8B5CF6",
            "14B8A6", "F43F5E", "6366F1", "06B6D4", "22C55E", "EAB308"
        )
        Dialog(onDismissRequest = { showColorPickerDialog = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E22))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Loc.SELECT.get(language) + " " + Loc.CARD_COLOR_HEADER.get(language),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Grid layout of colors
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val rowCount = 2
                        val itemsPerRow = colorsPalette.size / rowCount
                        for (r in 0 until rowCount) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                for (c in 0 until itemsPerRow) {
                                    val colorIndex = r * itemsPerRow + c
                                    val hex = colorsPalette[colorIndex]
                                    val color = Color(hex.toLong(16) or 0xFF000000)
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (customColorHex == hex) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                customColorHex = hex
                                                showColorPickerDialog = false
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = Loc.CANCEL.get(language),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showColorPickerDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    highlight: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 15.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) Color.Red else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (highlight) Color.Red else Color.White.copy(alpha = 0.2f),
            unfocusedBorderColor = if (highlight) Color.Red.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}
