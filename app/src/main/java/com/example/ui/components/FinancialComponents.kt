package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.ui.theme.ColorReceived
import com.example.ui.theme.ColorRemaining
import com.example.ui.theme.ColorTotalBill
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPartialAmber
import com.example.ui.theme.StatusPartialBg
import com.example.ui.theme.StatusUnpaidBg
import com.example.ui.theme.StatusUnpaidRed
import java.text.NumberFormat
import java.util.Locale

fun formatRupee(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    formatter.minimumFractionDigits = 0
    return "₹" + formatter.format(amount)
}

/**
 * Requirement 14: Payment status badge with consistent icons, colors, and labels
 */
@Composable
fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (status) {
        PaymentStatus.FULLY_PAID -> Quadruple(
            StatusPaidBg,
            StatusPaidGreen,
            "FULLY PAID",
            Icons.Default.CheckCircle
        )
        PaymentStatus.PARTIALLY_PAID -> Quadruple(
            StatusPartialBg,
            StatusPartialAmber,
            "PARTIALLY PAID",
            Icons.Default.HourglassTop
        )
        PaymentStatus.UNPAID -> Quadruple(
            StatusUnpaidBg,
            StatusUnpaidRed,
            "UNPAID",
            Icons.Default.HourglassTop
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Requirement 2, 3, 17: Prominently shows exactly these three amounts:
 * 1️⃣ TOTAL BILL
 * 2️⃣ RECEIVED
 * 3️⃣ REMAINING
 */
@Composable
fun ThreeAmountsRow(
    totalBill: Double,
    received: Double,
    remaining: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AmountBox(
            title = "TOTAL BILL",
            amount = totalBill,
            accentColor = ColorTotalBill,
            bgColor = Color(0xFFEFF6FF),
            modifier = Modifier.weight(1f)
        )
        AmountBox(
            title = "RECEIVED",
            amount = received,
            accentColor = ColorReceived,
            bgColor = Color(0xFFF0FDF4),
            modifier = Modifier.weight(1f)
        )
        AmountBox(
            title = "REMAINING",
            amount = remaining,
            accentColor = if (remaining > 0) ColorRemaining else Color(0xFF64748B),
            bgColor = if (remaining > 0) Color(0xFFFFF7ED) else Color(0xFFF8FAFC),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AmountBox(
    title: String,
    amount: Double,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formatRupee(amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Requirement 13: Payment progress indicator
 */
@Composable
fun PaymentProgressBar(
    totalBill: Double,
    received: Double,
    remaining: Double,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalBill > 0) ((received / totalBill) * 100.0).coerceIn(0.0, 100.0) else 0.0
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100.0).toFloat(),
        label = "PaymentProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payment Progress",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${percentage.toInt()}% Received",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (percentage >= 100) ColorReceived else ColorRemaining
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (percentage >= 100) ColorReceived else ColorTotalBill,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Requirement 1 & 4: Live Bill Summary Box inside New Bill Form
 */
@Composable
fun LiveBillSummaryCard(
    totalBill: Double,
    received: Double,
    remaining: Double,
    status: PaymentStatus,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B) // Dark elegant slate card
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💰", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BILL SUMMARY (LIVE)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
                PaymentStatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Amount Grid in Live Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LiveSummaryAmountItem(
                    label = "Total Bill",
                    amount = totalBill,
                    color = Color(0xFF60A5FA)
                )
                LiveSummaryAmountItem(
                    label = "Received",
                    amount = received,
                    color = Color(0xFF4ADE80)
                )
                LiveSummaryAmountItem(
                    label = "Remaining",
                    amount = remaining,
                    color = if (remaining > 0) Color(0xFFFB923C) else Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progressPercent = if (totalBill > 0) ((received / totalBill) * 100.0).coerceIn(0.0, 100.0) else 0.0
            LinearProgressIndicator(
                progress = { (progressPercent / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progressPercent >= 100) Color(0xFF4ADE80) else Color(0xFF60A5FA),
                trackColor = Color(0xFF334155)
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ $errorMessage",
                    color = Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LiveSummaryAmountItem(
    label: String,
    amount: Double,
    color: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatRupee(amount),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

/**
 * Requirement 18: Quick Financial Status bar (compact)
 * "₹2,500 Total | ₹500 Received | ₹2,000 Remaining | 20% Received | 🟡 Payment Pending"
 */
@Composable
fun QuickFinancialStatusBar(
    totalBill: Double,
    received: Double,
    remaining: Double,
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val pct = if (totalBill > 0) ((received / totalBill) * 100.0).coerceIn(0.0, 100.0).toInt() else 0

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatRupee(totalBill)} Total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTotalBill
                    )
                    Text(
                        text = " • ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatRupee(received)} Rec.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorReceived
                    )
                    Text(
                        text = " • ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatRupee(remaining)} Rem.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining > 0) ColorRemaining else ColorReceived
                    )
                }
                Text(
                    text = "$pct% Received",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PaymentStatusBadge(status = status)
        }
    }
}

/**
 * Requirement 8: Home Dashboard 4 Cards:
 * 💰 TOTAL BILLING
 * ✅ RECEIVED
 * ⏳ REMAINING
 * 📈 NET PROFIT
 */
@Composable
fun DashboardFourCards(
    totalBilling: Double,
    totalReceived: Double,
    totalRemaining: Double,
    netProfit: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardMetricCard(
                title = "TOTAL BILLING",
                amount = totalBilling,
                icon = Icons.Default.ReceiptLong,
                iconTint = ColorTotalBill,
                cardBg = Color(0xFFEFF6FF),
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = "RECEIVED",
                amount = totalReceived,
                icon = Icons.Default.CheckCircle,
                iconTint = ColorReceived,
                cardBg = Color(0xFFF0FDF4),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardMetricCard(
                title = "REMAINING",
                amount = totalRemaining,
                icon = Icons.Default.HourglassTop,
                iconTint = ColorRemaining,
                cardBg = Color(0xFFFFF7ED),
                modifier = Modifier.weight(1f)
            )
            DashboardMetricCard(
                title = "NET PROFIT",
                amount = netProfit,
                icon = Icons.Default.TrendingUp,
                iconTint = Color(0xFF059669),
                cardBg = Color(0xFFECFDF5),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    iconTint: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(iconTint.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatRupee(amount),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
