package Laporan

import Catatan.BarangMasukBuilder
import Converter.IntegerToRupiah
import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi
import play.api.libs.json.{JsObject, Json}

case class LaporanPenjualan(idPesanan:String,waktu:String,sku:String,itemName:String,jumlah:Int,hargaJual:Int,total:Int,hargaBeli:Int,laba:Int)

@Singleton
class LaporanPenjualanBuilder @Inject()(dbapi: DBApi, bm:BarangMasukBuilder)(implicit ec: DatabaseExecutionContext){
  private val db = dbapi.database("default")
  private val hargaAverageList = bm.getHargaBeliAverageList()

  /**
    * Parse a Object from a ResultSet
    * output : id object
    */
  private[Laporan] val LaporanPenjualanStructure = {
    get[Option[String]]("barang_keluar.sku") ~
      get[Option[String]]("barang_keluar.item_name") ~
      get[Option[String]]("barang_keluar.waktu") ~
      get[Option[Int]]("barang_keluar.jumlah_keluar") ~
      get[Option[Int]]("barang_keluar.harga_jual") ~
      get[Option[Int]]("barang_keluar.total") ~
      get[Option[String]]("barang_keluar.catatan") map {
      case sku ~ itemName ~ waktu ~ jumlah ~ jual ~ total ~ catatan=> {
        val id = getIDPesanan(catatan.getOrElse(""))
        val (beli,laba) = getAverageAndProfit(sku.getOrElse(""),total.getOrElse(0),jumlah.getOrElse(0))
        LaporanPenjualan(id,waktu.getOrElse(""), sku.getOrElse(""), itemName.getOrElse(""), jumlah.getOrElse(0), jual.getOrElse(0), total.getOrElse(0),beli,laba)
      }
    }
  }

  def getLaporanPenjualan(dateStart:String="",dateEnd:String=""):List[LaporanPenjualan] = db.withConnection{implicit connection =>
    var query = ""
    if (dateEnd=="" || dateStart == ""){
      query = s"select * from barang_keluar where 1"
    }else{
      query = s"select * from barang_keluar where waktu >= '$dateStart' and waktu <= '$dateEnd'"
    }

    return SQL(query).as(LaporanPenjualanStructure *)
  }

  /*
   * fungsi utk cari harga beli rata-rata dan total nilai
   */
  def getAverageAndProfit(sku:String,total:Int,jumlah:Int):(Int,Int) = {
    val avg = hargaAverageList.get(sku).getOrElse(0)
    return (avg, total-(avg*jumlah))
  }

  /*
   * Fungsi untuk mendapatkan id pesanan
   */
  def getIDPesanan(catatan:String):String = {
    val regex = "(Pesanan )+".r

    regex.findFirstIn(catatan) match {
      case Some(s)=>{
        return regex.replaceAllIn(catatan,"")
      }
      case None =>
    }
    ""
  }

  /*
   * fungsi untuk membuat data rangkuman yang ada di awal report penjualan
   */
  def countLaporanPenjualan(data: List[LaporanPenjualan],convertRupiah:Boolean=true):(Int,Int,Int,Int,List[JsObject]) = {
    var omzet = 0
    var labakotor = 0
    var penjualan = 0
    var barang = 0
    var newdata = List.empty[JsObject]

    data.foreach{datum=>
      omzet += datum.total
      labakotor += datum.laba
      barang += datum.jumlah
      if (datum.idPesanan != ""){
        penjualan += 1
      }
      val harga_jual = if (convertRupiah) IntegerToRupiah.convert(datum.hargaJual) else datum.hargaJual.toString
      val harga_beli = if (convertRupiah) IntegerToRupiah.convert(datum.hargaBeli) else datum.hargaBeli.toString
      val total = if (convertRupiah) IntegerToRupiah.convert(datum.total) else datum.total.toString
      val laba = if (convertRupiah) IntegerToRupiah.convert(datum.laba) else datum.laba.toString

      newdata = newdata :+ Json.obj(
        "id_pesanan"->datum.idPesanan,
        "waktu"->datum.waktu,
        "sku"->datum.sku,
        "item_name"->datum.itemName,
        "jumlah"->datum.jumlah,
        "harga_jual"->harga_jual,
        "total"->total,
        "harga_beli"->harga_beli,
        "laba"->laba
      )
    }

    (omzet,labakotor,penjualan,barang,newdata)
  }
}
