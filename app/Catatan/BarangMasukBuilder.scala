package Catatan

import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

import scala.util.{Failure, Success, Try}

case class BarangMasuk(waktu:String,sku:String,itemName:String,jumlahPemesanan:Int,jumlahDiterima:Int,hargaBeli:Int,total:Int,noKwin:String,catatan:String)
@Singleton
class BarangMasukBuilder @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    * Parse a Object from a ResultSet
    * output : id object
    */
  private[Catatan] val BarangMasukStructure = {
    get[Option[String]]("barang_masuk.sku") ~
      get[Option[String]]("barang_masuk.item_name") ~
      get[Option[String]]("barang_masuk.waktu") ~
      get[Option[Int]]("barang_masuk.jumlah_pemesanan") ~
      get[Option[Int]]("barang_masuk.jumlah_diterima") ~
      get[Option[Int]]("barang_masuk.harga_beli") ~
      get[Option[String]]("barang_masuk.nomer_kwitansi") ~
      get[Option[String]]("barang_masuk.catatan") ~
      get[Option[Int]]("jumlah_barang.total") map {
        case sku ~ itemName ~ waktu ~ pesan ~ terima ~ harga ~ kwit ~ cat ~ total => BarangMasuk(waktu.getOrElse(""), sku.getOrElse(""), itemName.getOrElse(""), pesan.getOrElse(0), terima.getOrElse(0), harga.getOrElse(0), total.getOrElse(0), kwit.getOrElse(""), cat.getOrElse(""))
      }
  }

  def getBarangMasuk(dateStart:String,dateEnd:String):List[BarangMasuk] = db.withConnection{implicit connection =>
    val query = s"select * from barang_masuk where waktu >= '$dateStart' and waktu <= '$dateEnd'"
    return SQL(query).as(BarangMasukStructure *)
  }

  def createBarangMasuk(data:(String,String,String,Int,Int,Int,Int,String,String)):String = db.withConnection { implicit connection =>
    val query = s"INSERT INTO barang_masuk VALUES('${data._1}','${data._2}','${data._3}',${data._4},${data._5},${data._6},${data._7},'${data._8}','${data._9}')"
    Try(SQL(query).executeInsert()) match{
      case Failure(e) => {
        return e.getMessage
      }
      case Success(s) => {
        return "success"
      }
    }
  }
}
