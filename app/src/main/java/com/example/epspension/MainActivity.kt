package com.example.epspension

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PERIOD_COLORS = listOf(
    Color(0xFF8B9BB3), Color(0xFF5F86B5), Color(0xFF3B9BB0),
    Color(0xFF0E7C7B), Color(0xFF6A9F33), Color(0xFFE29A0B),
)
private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

private fun inr(v: Double): String =
    "₹" + NumberFormat.getIntegerInstance(Locale("en", "IN")).format(Math.round(v))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EpsTheme { EpsApp() } }
    }
}

@Composable
fun EpsTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = Color(0xFF38B8B6), onPrimary = Color(0xFF04201F),
            background = Color(0xFF0C131D), onBackground = Color(0xFFEAF0F7),
            surface = Color(0xFF152131), onSurface = Color(0xFFEAF0F7),
            onSurfaceVariant = Color(0xFF9AA9BC), outline = Color(0xFF25354A),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0E7C7B), onPrimary = Color.White,
            background = Color(0xFFEEF1F4), onBackground = Color(0xFF0F1B2D),
            surface = Color.White, onSurface = Color(0xFF0F1B2D),
            onSurfaceVariant = Color(0xFF5D6B7E), outline = Color(0xFFD9DFE6),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpsApp() {
    var dojDay by rememberSaveable { mutableStateOf(LocalDate.of(1999, 1, 5).toEpochDay()) }
    var dobDay by rememberSaveable { mutableStateOf(LocalDate.of(1976, 12, 15).toEpochDay()) }
    var flat by rememberSaveable { mutableStateOf(false) }

    val doj = LocalDate.ofEpochDay(dojDay)
    val dob = LocalDate.ofEpochDay(dobDay)
    val retire = retirementDate(dob)
    val mode = if (flat) CeilingMode.FLAT else CeilingMode.HISTORIC
    val r = compute(doj, retire, mode)

    val shown by animateFloatAsState(
        targetValue = (r?.pension ?: 0.0).toFloat(),
        animationSpec = tween(350),
        label = "pension",
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.padding(pad)) {
            // Result bar
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("EPS Pension Calculator", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                if (r == null) {
                    Text("Check dates", color = MaterialTheme.colorScheme.onPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(inr(shown.toDouble()), color = MaterialTheme.colorScheme.onPrimary, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                        Text(" / month", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Text("Estimated, retirement at age 58", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
                }
            }

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Service timeline
                AppCard("Your service") {
                    if (r == null) {
                        Text("Retirement at 58 must fall after the joining date.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    } else {
                        Row(
                            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            PERIODS.indices.forEach { i ->
                                val y = r.years[i]
                                if (y > 0.01) {
                                    Box(
                                        Modifier.weight(y.toFloat()).fillMaxHeight().background(PERIOD_COLORS[i]),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (y >= 4) Text(
                                            "%.0f".format(y), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                                            color = if (i == 5) Color(0xFF1B1300) else Color.White,
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        PERIODS.indices.forEach { i ->
                            if (r.years[i] > 0.01) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                    Dot(PERIOD_COLORS[i])
                                    Text("${PERIODS[i].label}: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("%.1f".format(r.years[i]), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Retirement at age 58", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(retire.format(DATE_FMT), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Total service: ${r.fullYears} years ${r.extraMonths} months → counted as ${r.serviceYears} years" +
                                if (r.weightage > 0) " (+2 weightage)" else "",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (doj.isBefore(EPS95_START)) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Joining is before 16 Nov 1995 (start of EPS-95). Service before that date is treated under a different rule; here all service uses the same formula.",
                                fontSize = 12.sp, color = Color(0xFFB8680A),
                            )
                        }
                    }
                }

                // Dates
                AppCard("Dates") {
                    DateField("Date of birth", dob) { dobDay = it.toEpochDay() }
                    Text(
                        "Retirement is taken at the end of the month you turn 58.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                    )
                    DateField("Date of joining", doj) { dojDay = it.toEpochDay() }
                }

                // Calculation table
                if (r != null) {
                    AppCard("Calculation") {
                        TableRow("Ceiling period", "Years", "Ceiling", "Pension", header = true)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        PERIODS.indices.forEach { i ->
                            if (r.yearsCounted[i] > 0.01) {
                                TableRow(
                                    PERIODS[i].label, "%.1f".format(r.yearsCounted[i]),
                                    inr(r.ceilings[i].toDouble()), inr(r.pensions[i]),
                                    dot = PERIOD_COLORS[i],
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Weightage (20+ years of service)", fontSize = 13.sp)
                            Text(if (r.weightage > 0) "+2 years" else "None", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Option
                AppCard("Option") {
                    Text("Service before Sep 2014 is counted at", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !flat, onClick = { flat = false }, label = { Text("Ceiling of that time") })
                        FilterChip(selected = flat, onClick = { flat = true }, label = { Text("₹15,000 for all") })
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            dojDay = LocalDate.of(1999, 1, 5).toEpochDay()
                            dobDay = LocalDate.of(1976, 12, 15).toEpochDay()
                            flat = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Reset") }
                }

                // History
                AppCard("EPF wage ceiling history") {
                    PERIODS.indices.forEach { i ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Dot(PERIOD_COLORS[i])
                            Text(PERIODS[i].label, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(inr(PERIODS[i].cap.toDouble()), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text(
                    "Formula: for each ceiling period, (ceiling × years of service in that period) ÷ 70, then add them up. " +
                        "Wage is always taken at the ceiling of that period. Service of 6 months or more rounds up to a full year; " +
                        "2 years of weightage are added with 20+ years at age 58. Minimum pension ₹1,000. Ceiling dates come from news " +
                        "reports (Upstox, BusinessToday), not verified against the Gazette. The pro-rata rule is based on secondary reports " +
                        "of EPS 2026. Estimate only; EPFO decides the final figure from your contribution record.",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AppCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
            content()
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
    Spacer(Modifier.width(6.dp))
}

@Composable
private fun TableRow(a: String, b: String, c: String, d: String, header: Boolean = false, dot: Color? = null) {
    val size = if (header) 11.sp else 12.sp
    val weight = if (header) FontWeight.SemiBold else FontWeight.Normal
    val col = if (header) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(2.4f), verticalAlignment = Alignment.CenterVertically) {
            if (dot != null) Dot(dot)
            Text(a, fontSize = size, fontWeight = weight, color = col)
        }
        Text(b, Modifier.weight(0.8f), fontSize = size, fontWeight = weight, color = col, textAlign = TextAlign.End)
        Text(c, Modifier.weight(1.1f), fontSize = size, fontWeight = weight, color = col, textAlign = TextAlign.End)
        Text(d, Modifier.weight(1.1f), fontSize = size, fontWeight = if (header) weight else FontWeight.Bold, color = col, textAlign = TextAlign.End)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate, onPick: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(date.format(DATE_FMT), fontWeight = FontWeight.Bold)
        }
    }
    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}
