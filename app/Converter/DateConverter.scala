package Converter

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DateConverter {
  val datetimeFormatter = DateTimeFormat.forPattern("d MMM YYYY")
  def convertTanggalCetak(dt:DateTime):String = {
    datetimeFormatter.print(dt)
  }

  def convertRangeTanggal(ds:String,de:String):String = {
    val inputFormat = DateTimeFormat.forPattern("YYYY-MM-dd")

    s"${datetimeFormatter.print(inputFormat.parseDateTime(ds).getMillis)} - ${datetimeFormatter.print(inputFormat.parseDateTime(de).getMillis)}"
  }
}
