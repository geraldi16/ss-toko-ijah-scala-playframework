package controllers

import Converter.{DateConverter, IntegerToRupiah}
import Laporan.{NilaiBarang, NilaiBarangBuilder}
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class LaporanNilaiBarangController @Inject()(cc: ControllerComponents,nb:NilaiBarangBuilder) extends AbstractController(cc) {
  def showLaporanNilaiBarang() = Action {
    //minta laporan sesuai tanggal yang diminta
    val data:List[NilaiBarang] = nb.getLaporanNilaiBarang()

    //hitung: jumlah sku, total barang, akumulasi nilai total, tanggal cetak hari ini
    val (skuCount,totalBarang,totalNilai,newdata) = countLaporanNilaiBarang(data)

    Ok(Json.obj(
      "tanggal_cetak"-> DateConverter.convertTanggalCetak(DateTime.now()),
      "jumlah_sku"->skuCount,
      "jumlah_total_barang"->totalBarang,
      "total_nilai"->IntegerToRupiah.convert(totalNilai),
      "data"->newdata
    )).as(JSON)
  }

  def countLaporanNilaiBarang(data:List[NilaiBarang]):(Int,Int,Int,List[JsObject]) = {
    var sku = 0
    var jumlah = 0
    var total = 0
    var convertedValue = List.empty[JsObject]

    data.foreach{datum=>
      sku += 1
      jumlah += datum.jumlah
      total += datum.total
      convertedValue = convertedValue :+ Json.obj(
        "sku"->datum.sku,
        "item_name"->datum.itemName,
        "jumlah"->datum.jumlah,
        "harga_average"->datum.harga,
        "total"->datum.total
      )
    }

    (sku,jumlah,total,convertedValue)
  }
}
