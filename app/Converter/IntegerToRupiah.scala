package Converter

import java.util.{Currency, Locale}

object IntegerToRupiah {
  val rupiahFormatter = java.text.NumberFormat.getCurrencyInstance
  rupiahFormatter.setCurrency(Currency.getInstance(new Locale("id", "ID")))
  def convert(num:Int):String = {
    rupiahFormatter.format(num)
  }
}
