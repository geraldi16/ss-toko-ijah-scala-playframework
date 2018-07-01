package controllers

import Converter.{DateConverter, IntegerToRupiah}
import Laporan.{LaporanPenjualan, LaporanPenjualanBuilder}
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class LaporanPenjualanController @Inject()(cc: ControllerComponents,lp:LaporanPenjualanBuilder) extends AbstractController(cc)  {
  def showLaporanPenjualan(ds:String,de:String) = Action {
    //minta laporan sesuai tanggal yang diminta
    val data:List[LaporanPenjualan] = lp.getLaporanPenjualan(ds,de)
    //hitung : tanggal cetak, tanggal request, total omzet, total laba kotor, total penjualan, total barang
    val (omzet,laba,penjualan,barang,newdata) = countLaporanPenjualan(data)

    Ok(Json.obj(
      "tanggal_cetak" ->DateConverter.convertTanggalCetak(DateTime.now()),
      "tanggal"->DateConverter.convertRangeTanggal(ds,de),
      "total_omzet"->IntegerToRupiah.convert(omzet),
      "total_laba_kotor"->IntegerToRupiah.convert(laba),
      "total_penjualan"->penjualan,
      "total_barang"->barang,
      "data"->newdata
    )).as(JSON)
  }

  def countLaporanPenjualan(data: List[LaporanPenjualan]):(Int,Int,Int,Int,List[JsObject]) = {
    var omzet = 0
    var laba = 0
    var penjualan = 0
    var barang = 0
    var newdata = List.empty[JsObject]

    data.foreach{datum=>
      omzet += datum.total
      laba += datum.laba
      barang += datum.jumlah
      if (datum.idPesanan != ""){
        penjualan += 1
      }
      newdata = newdata :+ Json.obj(
        "id_pesanan"->datum.idPesanan,
        "waktu"->datum.waktu,
        "sku"->datum.sku,
        "item_name"->datum.itemName,
        "jumlah"->datum.jumlah,
        "harga_jual"->datum.hargaJual,
        "total"->datum.total,
        "harga_beli"->datum.hargaBeli,
        "laba"->datum.laba
      )
    }

    (omzet,laba,penjualan,barang,newdata)
  }
}
