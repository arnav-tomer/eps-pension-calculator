package com.example.epspension

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CeilingPeriod(val start: LocalDate, val cap: Int, val label: String)

enum class CeilingMode { HISTORIC, FLAT }

val PERIODS = listOf(
    CeilingPeriod(LocalDate.of(1985, 9, 1), 2500, "Until Oct 1990"),
    CeilingPeriod(LocalDate.of(1990, 11, 1), 3500, "Nov 1990 – Sep 1994"),
    CeilingPeriod(LocalDate.of(1994, 10, 1), 5000, "Oct 1994 – May 2001"),
    CeilingPeriod(LocalDate.of(2001, 6, 1), 6500, "Jun 2001 – Aug 2014"),
    CeilingPeriod(LocalDate.of(2014, 9, 1), 15000, "Sep 2014 – 16 Sep 2026"),
    CeilingPeriod(LocalDate.of(2026, 9, 17), 25000, "From 17 Sep 2026"),
)

val EPS95_START: LocalDate = LocalDate.of(1995, 11, 16)
private val END: LocalDate = LocalDate.of(2100, 1, 1)

data class EpsResult(
    val serviceYears: Int,
    val weightage: Int,
    val years: List<Double>,        // actual service per period
    val yearsCounted: List<Double>, // incl. weightage
    val ceilings: List<Int>,
    val pensions: List<Double>,
    val pension: Double,
    val fullYears: Int,
    val extraMonths: Int,
)

/** Retirement at 58: last day of the month in which the 58th birthday falls. */
fun retirementDate(dob: LocalDate): LocalDate {
    val d = dob.plusYears(58)
    return d.withDayOfMonth(d.lengthOfMonth())
}

fun ceilingsFor(mode: CeilingMode): List<Int> =
    PERIODS.mapIndexed { i, p -> if (mode == CeilingMode.FLAT && i < 4) 15000 else p.cap }

private fun span(s: LocalDate, e: LocalDate): Double =
    maxOf(0.0, ChronoUnit.DAYS.between(s, e) / 30.4375)

fun compute(doj: LocalDate, retire: LocalDate, mode: CeilingMode): EpsResult? {
    if (!retire.isAfter(doj)) return null
    var m = (retire.year - doj.year) * 12 + (retire.monthValue - doj.monthValue)
    if (retire.dayOfMonth < doj.dayOfMonth) m--
    val yy = m / 12
    val rr = m % 12
    val svc = if (rr >= 6) yy + 1 else yy
    val wt = if (svc >= 20) 2 else 0

    val ms = PERIODS.mapIndexed { i, p ->
        val e = if (i < PERIODS.size - 1) PERIODS[i + 1].start else END
        val s = if (doj.isAfter(p.start)) doj else p.start
        val en = if (retire.isBefore(e)) retire else e
        span(s, en)
    }
    val total = ms.sum().let { if (it == 0.0) 1.0 else it }
    val k = svc * 12.0 / total
    val yrs = ms.map { it * k / 12.0 }

    var last = 0
    yrs.forEachIndexed { i, v -> if (v > 0.001) last = i }
    val counted = yrs.toMutableList()
    counted[last] = counted[last] + wt

    val cp = ceilingsFor(mode)
    val pensions = counted.mapIndexed { i, v -> cp[i] * v / 70.0 }
    val base = pensions.sum()
    return EpsResult(svc, wt, yrs, counted, cp, pensions, maxOf(base, 1000.0), yy, rr)
}
