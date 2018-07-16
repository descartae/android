package org.descartae.android.view.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.SimpleTimeZone
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateGMT {

  @Throws(ParseException::class)
  fun dateToGMT(date: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd")
    val cal = Calendar.getInstance(SimpleTimeZone(0, "GMT"))
    format.calendar = cal
    val gmt = format.parse(date)
    return gmt.toString()
  }

  fun getDate(year: Int, month: Int, day: Int): Calendar {
    val date = Calendar.getInstance()
    date.set(Calendar.YEAR, year)
    date.set(Calendar.MONTH, month)
    date.set(Calendar.DAY_OF_MONTH, day)
    date.set(Calendar.HOUR, date.getMinimum(Calendar.HOUR))
    date.set(Calendar.HOUR_OF_DAY, date.getMinimum(Calendar.HOUR_OF_DAY))
    date.set(Calendar.MINUTE, date.getMinimum(Calendar.MINUTE))
    date.set(Calendar.SECOND, date.getMinimum(Calendar.SECOND))
    date.set(Calendar.MILLISECOND, date.getMinimum(Calendar.MILLISECOND))
    return date.clone() as Calendar
  }

  fun dateDisplay(date: Calendar): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy")
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date.time)
  }

  fun dateDisplay(date: Calendar, format: String): String {
    val sdf = SimpleDateFormat(format)
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date.time)
  }

  fun dateDisplay(date: Long, format: String): String {
    val sdf = SimpleDateFormat(format)
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(Date(date))
  }

  fun days(start: Calendar, end: Calendar): Int {

    return if (end.timeInMillis - start.timeInMillis < 0) {
      0
    } else Math.round(TimeUnit.MILLISECONDS.toDays(end.timeInMillis - start.timeInMillis).toFloat())

  }

  fun stringToCalendar(date: String?): Calendar {

    val cDate = Calendar.getInstance().clone() as Calendar

    if (date == null) return cDate

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    try {
      cDate.time = simpleDateFormat.parse(date)
    } catch (e: ParseException) {
    }

    return cDate
  }
}