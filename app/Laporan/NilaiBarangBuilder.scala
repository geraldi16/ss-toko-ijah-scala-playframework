package Laporan

import Catatan.BarangMasukBuilder
import Converter.IntegerToRupiah
import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi
import play.api.libs.json.{JsObject, Json}

case class NilaiBarang(sku:String,itemName:String,jumlah:Int,harga:Int,total:Int)

@Singleton
class NilaiBarangBuilder @Inject()(dbapi: DBApi,bm:BarangMasukBuilder)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")
  private val hargaAverageList = bm.getHargaBeliAverageList()
  /**
    * Parse a Object from a ResultSet
    * output : id object
    */
  private[Laporan] val LaporanNilaiBarangStructure = {
    get[Option[String]]("jumlah_barang.sku") ~
      get[Option[String]]("jumlah_barang.item_name") ~
      get[Option[Int]]("jumlah_barang.qty") map {
      case sku ~ itemName ~ jumlah => {
        val (beli,total) = getAverageAndTotal(sku.getOrElse(""),jumlah.getOrElse(0))
        NilaiBarang(sku.getOrElse(""), itemName.getOrElse(""), jumlah.getOrElse(0), beli,total)
      }
    }
  }

  def getLaporanNilaiBarang(dateStart:String="",dateEnd:String=""):List[NilaiBarang] = db.withConnection{implicit connection =>
    val query = s"select * from jumlah_barang where 1"

    return SQL(query).as(LaporanNilaiBarangStructure *)
  }

  /*
    * fungsi utk cari harga beli rata-rata dan total nilai
   */
  def getAverageAndTotal(sku:String,jumlah:Int):(Int,Int) = {
    val avg = hargaAverageList.get(sku).getOrElse(0)
    return (avg, avg * jumlah)
  }

  /*
    * fungsi utk membuat data rangkuman yang ada di awal report sheet
   */
  def countLaporanNilaiBarang(data:List[NilaiBarang],convertRupiah:Boolean=true):(Int,Int,Long,List[JsObject]) = {
    var sku = 0
    var jumlah = 0
    var totalNilai = 0
    var convertedValue = List.empty[JsObject]

    data.foreach{datum=>
      sku += 1
      jumlah += datum.jumlah
      totalNilai += datum.total
      val harga = if (convertRupiah) IntegerToRupiah.convert(datum.harga) else datum.harga.toString
      val total = if (convertRupiah) IntegerToRupiah.convert(datum.total) else datum.harga.toString
      convertedValue = convertedValue :+ Json.obj(
        "sku"->datum.sku,
        "item_name"->datum.itemName,
        "jumlah"->datum.jumlah,
        "harga_average"-> harga,
        "total"->total
      )
    }

    (sku,jumlah,totalNilai,convertedValue)
  }
}
