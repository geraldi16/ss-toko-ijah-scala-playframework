package Laporan

import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

case class LaporanPenjualan(idPesanan:String,waktu:String,sku:String,itemName:String,jumlah:Int,hargaJual:Int,total:Int,hargaBeli:Int,laba:Int)

@Singleton
class LaporanPenjualanBuilder @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
  private val db = dbapi.database("default")

  /**
    * Parse a Object from a ResultSet
    * output : id object
    */
  private[Laporan] val LaporanPenjualanStructure = {
    get[Option[String]]("laporan_penjualan.sku") ~
      get[Option[String]]("laporan_penjualan.item_name") ~
      get[Option[String]]("laporan_penjualan.waktu") ~
      get[Option[Int]]("laporan_penjualan.jumlah") ~
      get[Option[Int]]("laporan_penjualan.harga_jual") ~
      get[Option[Int]]("laporan_penjualan.harga_beli") ~
      get[Option[Int]]("laporan_penjualan.total") ~
      get[Option[String]]("laporan_penjualan.id_pesanan") ~
      get[Option[Int]]("laporan_penjualan.laba") map {
      case sku ~ itemName ~ waktu ~ jumlah ~ jual ~ beli ~ total ~ id ~ laba => LaporanPenjualan(id.getOrElse(""),waktu.getOrElse(""), sku.getOrElse(""), itemName.getOrElse(""), jumlah.getOrElse(0), jual.getOrElse(0), total.getOrElse(0), beli.getOrElse(0), laba.getOrElse(0))
    }
  }

  def getLaporanPenjualan(dateStart:String="",dateEnd:String=""):List[LaporanPenjualan] = db.withConnection{implicit connection =>
    var query = ""
    if (dateEnd=="" || dateStart == ""){
      query = s"select * from laporan_penjualan where 1"
    }else{
      query = s"select * from laporan_penjualan where waktu >= '$dateStart' and waktu <= '$dateEnd'"
    }

    return SQL(query).as(LaporanPenjualanStructure *)
  }
}
