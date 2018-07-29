package com.giacomoparisi.funktionalkit.extensions

import arrow.core.Option
import arrow.core.Try
import arrow.core.toOption
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Giacomo Parisi on 19/07/18.
 * https://github.com/giacomoParisi
 */

const val YEAR = "yyyy"
const val MONTH = "MM"
const val DAY = "dd"
const val T = "'T'"
const val HOURS = "HH"
const val MINUTES = "mm"
const val SECONDS = "ss"

fun String.formatDate(
        fromFormat: String,
        toFormat: String,
        fromLocale: Locale = Locale.getDefault(),
        toLocale: Locale = Locale.getDefault()
): Try<String> {

    val simpleDateFormatFrom = SimpleDateFormat(fromFormat, fromLocale)
    return Try { simpleDateFormatFrom.parse(this) }.map {
        val simpleDateFormatTo = SimpleDateFormat(toFormat, toLocale)
        simpleDateFormatTo.format(it)
    }
}

fun String.toCalendar(
        format: String,
        locale: Locale = Locale.getDefault()
): Try<Calendar> {

    val calendarNow = Calendar.getInstance()
    val simpleDateFormatFrom = SimpleDateFormat(format, locale)
    return Try { simpleDateFormatFrom.parse(this) }.map {
        calendarNow.time = it
        calendarNow
    }
}