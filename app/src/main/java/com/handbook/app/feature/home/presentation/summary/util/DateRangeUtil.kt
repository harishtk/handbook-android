package com.handbook.app.feature.home.presentation.summary.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object DateRangeUtil {

    private val defaultZoneId: ZoneId = ZoneId.systemDefault()

    fun getTodayRange(): Pair<Long, Long> {
        val today = LocalDate.now(defaultZoneId)
        val startOfDay = today.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
        val endOfDay = today.atTime(LocalTime.MAX).atZone(defaultZoneId).toInstant().toEpochMilli()
        return Pair(startOfDay, endOfDay)
    }

    fun getThisWeekRange(): Pair<Long, Long> {
        val today = LocalDate.now(defaultZoneId)
        val firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) // Assuming Monday is the start of the week
        val lastDayOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val startOfWeek = firstDayOfWeek.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
        val endOfWeek = lastDayOfWeek.atTime(LocalTime.MAX).atZone(defaultZoneId).toInstant().toEpochMilli()
        return Pair(startOfWeek, endOfWeek)
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        return getMonthRange(YearMonth.now(defaultZoneId))
    }

    fun getMonthRange(yearMonth: YearMonth): Pair<Long, Long> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()

        val startOfMonth = firstDayOfMonth.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
        val endOfMonth = lastDayOfMonth.atTime(LocalTime.MAX).atZone(defaultZoneId).toInstant().toEpochMilli()
        return Pair(startOfMonth, endOfMonth)
    }

    fun getCurrentYearRange(): Pair<Long, Long> {
        return getYearRange(LocalDate.now(defaultZoneId).year)
    }

    fun getYearRange(year: Int): Pair<Long, Long> {
        val firstDayOfYear = LocalDate.of(year, 1, 1)
        val lastDayOfYear = LocalDate.of(year, 12, 31)

        val startOfYear = firstDayOfYear.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
        val endOfYear = lastDayOfYear.atTime(LocalTime.MAX).atZone(defaultZoneId).toInstant().toEpochMilli()
        return Pair(startOfYear, endOfYear)
    }

    fun toEpochMillisAtStartOfDay(date: LocalDate): Long {
        return date.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
    }

    fun toEpochMillisAtEndOfDay(date: LocalDate): Long {
        return date.atTime(LocalTime.MAX).atZone(defaultZoneId).toInstant().toEpochMilli()
    }
}