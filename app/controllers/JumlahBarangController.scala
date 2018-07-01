package controllers

import Catatan.JumlahBarangBuilder
import Converter.{DateConverter, IntegerToRupiah}
import Laporan.NilaiBarangBuilder
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class JumlahBarangController @Inject()(cc: ControllerComponents,jbb: JumlahBarangBuilder,nb:NilaiBarangBuilder) extends AbstractController(cc){

  /*
    * Fungsi untuk memasukkan data barang ke database
   */
  def createCatatanJumlahBarang = Action {
    implicit request =>
      var result = Json.obj()
      //get request params
      val jsonBody: Option[JsValue] = request.body.asJson
      val sku:String = jsonBody.map { json =>
        (json\"sku").as[String]
      }.get
      val itemName:String = jsonBody.map { json =>
        (json\"item_name").as[String]
      }.get
      val jumlah:Int = jsonBody.map { json =>
        (json\"jumlah").as[Int]
      }.get

      //insert ke database
      val create = jbb.createJumlahBarangData(sku,itemName,jumlah)

      create match {
        case "success"=>{
          //case jika data belum ada
          result = Json.obj(
            "status"->"insert new data success",
            "message"-> jsonBody.toString
          )
        }
        case "[SQLITE_CONSTRAINT_PRIMARYKEY]  A PRIMARY KEY constraint failed (UNIQUE constraint failed: jumlah_barang.sku)"=>{
          //case jika data sudah ada di db
          //update nilai data
          val update = jbb.updateJumlahBarangData(sku,itemName,jumlah)
          if (update == "success"){
            result = Json.obj(
              "status"->"update data success",
              "message"-> jsonBody.toString
            )
          }else{
            result = Json.obj(
              "status"->" update data error",
              "message"->update
            )
          }
        }
        case _=>{
          //jika terjadi error saat koneksi ke db
          result = Json.obj(
            "status"->"insert data error",
            "message"->create
          )
        }
      }

      Ok(result).as(JSON)
  }

  /*
    * untuk menampilkan laporan nilai barang
   */
  def showLaporanNilaiBarang() = Action {
    //minta laporan sesuai tanggal yang diminta
    val data = nb.getLaporanNilaiBarang()

    //hitung: jumlah sku, total barang, akumulasi nilai total, tanggal cetak hari ini
    val (skuCount,totalBarang,totalNilai,newdata) = nb.countLaporanNilaiBarang(data)

    Ok(Json.obj(
      "tanggal_cetak"-> DateConverter.convertTanggalCetak(DateTime.now()),
      "jumlah_sku"->skuCount,
      "jumlah_total_barang"->totalBarang,
      "total_nilai"->IntegerToRupiah.convert(totalNilai),
      "data"->newdata
    )).as(JSON)
  }

}
