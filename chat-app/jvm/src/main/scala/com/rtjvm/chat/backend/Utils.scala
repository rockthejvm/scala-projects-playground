package com.rtjvm.chat.backend

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField.MILLI_OF_SECOND

object Utils:
  private val TimestampFormatter = DateTimeFormatter.ofPattern("EEE dd MMM, yyyy hh:mm a")

  def epochMsNow: Long = System.currentTimeMillis

  def token: String =
    val ldt               = LocalDateTime.now
    val milliseconds      = ldt.getLong(MILLI_OF_SECOND)
    val formattedDateTime = ldt.format(TimestampFormatter)
    s"$formattedDateTime ($milliseconds)"
