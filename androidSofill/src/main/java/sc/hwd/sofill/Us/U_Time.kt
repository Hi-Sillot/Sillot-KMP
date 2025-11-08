/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/30 上午6:37
 * updated: 2024/8/30 上午6:37
 */

package sc.hwd.sofill.Us

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val dateFormatFull1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
val dateFormatFull1SiYuan: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun formatFull1(date: LocalDateTime): String = dateFormatFull1.format(date)

fun formatFull1SiYuan(date: LocalDateTime): String = dateFormatFull1SiYuan.format(date)

fun getFormatter(pattern: String): DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)

fun formatFromMillis(millis: Long, pattern: String): String {
    val instant = Instant.ofEpochMilli(millis)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return getFormatter(pattern).format(dateTime)
}

fun formatFromLocalDateTime(dateTime: LocalDateTime, pattern: String): String {
    return getFormatter(pattern).format(dateTime)
}


/**
 * 用例：
 * ```kotlin
 * dateFormat_full1.format(Date())
 * ```
 * ```java
 * SimpleDateFormat dateFormat_full1 = new SimpleDateFormat("yyyyMMdd-HHmmss");
 * String dateStr = dateFormat_full1.format(new Date());
 * ```
 */
@Deprecated("Use formatFull1() instead")
val dateFormat_full1 = SimpleDateFormat("yyyyMMdd-HHmmss")
@Deprecated("Use formatFull1SiYuan() instead")
val dateFormat_full1_siyuan = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")