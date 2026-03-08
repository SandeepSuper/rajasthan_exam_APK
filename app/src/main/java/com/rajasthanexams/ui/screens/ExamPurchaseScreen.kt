package com.rajasthanexams.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Brand colours (always visible on gradient hero — no theme switch needed)
private val PremiumGold    = Color(0xFFFFD600)
private val PremiumGoldDim = Color(0xFFFFA000)
private val DeepBlue       = Color(0xFF1A237E)
private val MidBlue        = Color(0xFF283593)
private val AccentAmber    = Color(0xFFFF6F00)
private val GreenSuccess   = Color(0xFF00C853)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamPurchaseScreen(
    examTitle: String,
    examPrice: Double,
    discountPercent: Int = 0,
    userCoins: Int = 0,
    onBackClick: () -> Unit,
    onBuyClick: (useCoins: Boolean, coinsToUse: Int) -> Unit
) {
    val discountedPrice = if (discountPercent > 0)
        (examPrice * (1.0 - discountPercent / 100.0)).roundToInt().toDouble()
    else examPrice
    val savings = (examPrice - discountedPrice).roundToInt()

    // Coin discount logic (max 10% of discountedPrice)
    val coinValueRs = 0.10  // 1 coin = ₹0.10
    val maxCoinDiscountPercent = 10
    val maxCoinDiscountAmount = (discountedPrice * maxCoinDiscountPercent / 100.0)
    val maxUsableCoins = minOf(userCoins, (maxCoinDiscountAmount / coinValueRs).toInt())
    val coinDiscountAmount = (maxUsableCoins * coinValueRs * 100).toLong() / 100.0

    var useCoins by remember { mutableStateOf(false) }
    val finalPrice = if (useCoins) maxOf(0.0, discountedPrice - coinDiscountAmount) else discountedPrice
    val isFreeWithCoins = useCoins && finalPrice == 0.0

    // Theme-aware colours
    val surface     = MaterialTheme.colorScheme.surface
    val onSurface   = MaterialTheme.colorScheme.onSurface
    val outline     = MaterialTheme.colorScheme.outline
    val onSurface60 = onSurface.copy(alpha = 0.6f)
    val primary     = MaterialTheme.colorScheme.primary

    // Pulsing badge animation
    val pulse = rememberInfiniteTransition(label = "pulse")
    val badgeScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeScale"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Unlock Premium", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = onSurface,
                    navigationIconContentColor = onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Premium Hero Banner (intentionally keeps brand gradient in both modes) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DeepBlue, MidBlue))),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(12.dp, CircleShape)
                            .background(
                                Brush.radialGradient(listOf(PremiumGold, PremiumGoldDim)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = DeepBlue,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = examTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Complete access to every test & solution",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }

                // Pulsing discount badge — only shown when there is a real discount
                if (discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 16.dp)
                            .scale(badgeScale)
                            .background(AccentAmber, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$discountPercent% OFF",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Price Card ───────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Limited time tag — only when there is a real discount
                    if (discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .background(
                                    AccentAmber.copy(alpha = 0.12f),
                                    RoundedCornerShape(50)
                                )
                                .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🎯  Limited Time Offer",
                                color = AccentAmber,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Strikethrough MRP — only when discount is active
                    if (discountPercent > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("MRP  ", style = MaterialTheme.typography.bodySmall, color = onSurface60)
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            textDecoration = TextDecoration.LineThrough,
                                            color = onSurface60,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) { append("₹${examPrice.toInt()}") }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Price (discounted / coin-reduced)
                    Text(
                        text = "₹${finalPrice.toInt()}",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isFreeWithCoins) GreenSuccess else primary
                    )

                    // Savings callout — only when discount is active
                    if (discountPercent > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(GreenSuccess.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🎉  You save ₹$savings  ($discountPercent% off)",
                                color = GreenSuccess,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "One-time payment · No subscription",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface60
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Coin Discount Toggle ──────────────────────────────────────────────
            if (userCoins > 0 && maxUsableCoins > 0) {
                Card(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (useCoins) Color(0xFFFFF8E1) else surface
                    ),
                    border = if (useCoins) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Use $maxUsableCoins Coins",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF795548)
                            )
                            Text(
                                if (useCoins) "Saving ₹${String.format("%.0f", coinDiscountAmount)}  →  Pay ₹${finalPrice.toInt()}"
                                else "Save ₹${String.format("%.0f", coinDiscountAmount)} on this purchase",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8D6E63)
                            )
                        }
                        Switch(
                            checked = useCoins,
                            onCheckedChange = { useCoins = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFD700),
                                checkedTrackColor = Color(0xFFFF8F00)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Feature Cards ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "What you get",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                PremiumFeatureRow(
                    icon  = Icons.Default.CheckCircle,
                    tint  = GreenSuccess,
                    title = "All Premium Tests",
                    sub   = "Mock, Full, PYQ & Live tests included",
                    surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f), primary = primary
                )
                PremiumFeatureRow(
                    icon  = Icons.Default.Star,
                    tint  = PremiumGoldDim,
                    title = "Detailed Solutions",
                    sub   = "Step-by-step answers for every question",
                    surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f), primary = primary
                )
                PremiumFeatureRow(
                    icon  = Icons.Default.Create,
                    tint  = Color(0xFF7C4DFF),
                    title = "Performance Analytics",
                    sub   = "Track accuracy, speed & weak areas",
                    surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f), primary = primary
                )
                PremiumFeatureRow(
                    icon  = Icons.Default.ThumbUp,
                    tint  = Color(0xFF0288D1),
                    title = "Ad-free Experience",
                    sub   = "Distraction-free study environment",
                    surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f), primary = primary
                )
                PremiumFeatureRow(
                    icon  = Icons.Default.Refresh,
                    tint  = Color(0xFFE53935),
                    title = "Unlimited Attempts",
                    sub   = "Retake any test as many times as you like",
                    surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f), primary = primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Trust Row ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrustChip(emoji = "🔒", label = "Secure\nPayment",   surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f))
                TrustChip(emoji = "↩",  label = "Easy\nRefund",      surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f))
                TrustChip(emoji = "⚡", label = "Instant\nAccess",   surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f))
                TrustChip(emoji = "🤝", label = "50k+\nStudents",    surface = surface, onSurface = onSurface, outline = outline.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── CTA Button ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(listOf(DeepBlue, MidBlue)),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Button(
                    onClick = { onBuyClick(useCoins, if (useCoins) maxUsableCoins else 0) },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFreeWithCoins) Color.Transparent else Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        if (isFreeWithCoins) Icons.Default.Star else Icons.Default.Lock,
                        contentDescription = null, modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isFreeWithCoins) "🎉 Unlock Free with Coins!"
                                   else if (useCoins) "Pay ₹${finalPrice.toInt()} + $maxUsableCoins Coins"
                                   else "Buy Now  ₹${discountedPrice.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        if (useCoins && !isFreeWithCoins) {
                            Text(
                                text = "${maxUsableCoins} coins save you ₹${String.format("%.0f", coinDiscountAmount)}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        } else if (discountPercent > 0 && !useCoins) {
                            Text(
                                text = "Was ₹${examPrice.toInt()} · Save ₹$savings",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "By purchasing, you agree to our Terms & Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                color = onSurface60,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun PremiumFeatureRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    sub: String,
    surface: Color,
    onSurface: Color,
    outline: Color,
    primary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .border(1.dp, outline, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = primary)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun TrustChip(
    emoji: String,
    label: String,
    surface: Color,
    onSurface: Color,
    outline: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .border(1.dp, outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}
