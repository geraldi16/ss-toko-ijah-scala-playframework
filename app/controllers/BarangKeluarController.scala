package controllers

import Catatan.BarangKeluarBuilder
import Converter.{DateConverter, IntegerToRupiah}
import Laporan.LaporanPenjualanBuilder
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class BarangKeluarController @Inject()(cc: ControllerComponents,bk:BarangKeluarBuilder,lp:LaporanPenjualanBuilder) extends AbstractController(cc) {
  /*
   * Fungsi untuk menyimpan catatan barang keluar ke database
   */
  def createCatatanBarangKeluar = Action {implicit request =>
    var result = Json.obj()
    //get request params
    val jsonBody: Option[JsValue] = request.body.asJson

    val sku:String = jsonBody.map { json =>
      (json\"sku").as[String]
    }.get
    val itemName:String = jsonBody.map { json =>
      (json\"item_name").as[String]
    }.get
    val waktu:String = jsonBody.map { json =>
      (json\"waktu").as[String]
    }.get
    val catatan:String = jsonBody.map { json =>
      (json\"catatan").as[String]
    }.get
    val keluar:Int = jsonBody.map { json =>
      (json\"keluar").as[Int]
    }.get
    val jual:Int = jsonBody.map { json =>
      (json\"jual").as[Int]
    }.get

    //create data baru
    val create = bk.createBarangKeluar((waktu,sku,itemName,keluar,jual,catatan))

    if (create == "success"){
      result = Json.obj(
        "status"->"success",
        "message"-> jsonBody.toString
      )
    }else{
      result = Json.obj(
        "status"->s"error",
        "message"->create
      )
    }
    Ok(result).as(JSON)
  }

  def updateCatatanBarangKeluar() = Action {implicit request =>
    var result = Json.obj()
    //get request params
    val jsonBody: JsValue = request.body.asJson.get
    val jsonItems = (jsonBody \"items").as[List[JsObject]]
    val jsonWhere = (jsonBody \"where").as[List[JsObject]]
    var items:Map[String,Any] = Map()
    var where:Map[String,Any] = Map()

    jsonItems.foreach{json=>
      items = items +((json \ "name").as[String]->(json \ "value").as[String])
    }

    jsonWhere.foreach{json=>
      where = where +((json \ "name").as[String]->(json \ "value").as[String])
    }

    //update data
    val update = bk.updateBarangKeluar(items,where)

    if (update == "success"){
      result = Json.obj(
        "status"->"success",
        "message"-> jsonBody.toString
      )
    }else{
      result = Json.obj(
        "status"->s"error",
        "message"->update
      )
    }
    Ok(result).as(JSON)
  }

  /*
    * fungsi untuk menampilkan report dalam bentuk JSON
   */
  def showLaporanPenjualan(ds:String,de:String) = Action {
    //minta laporan sesuai tanggal yang diminta
    val data = lp.getLaporanPenjualan(ds,de)
    //hitung : tanggal cetak, tanggal request, total omzet, total laba kotor, total penjualan, total barang
    val (omzet,laba,penjualan,barang,newdata) = lp.countLaporanPenjualan(data)

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

}
