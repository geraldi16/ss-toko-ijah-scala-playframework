package Catatan

import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

import scala.util.{Failure, Success, Try}

case class BarangKeluar(waktu:String,sku:String,itemName:String,jumlahKeluar:Int,hargaJual:Int,total:Int,catatan:String)

@Singleton
class BarangKeluarBuilder @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
  private val db = dbapi.database("default")

  /**
  * Parse a Object from a ResultSet
  * output : id object
  */
  private[Catatan] val BarangKeluarStructure = {
    get[Option[String]]("barang_keluar.sku") ~
      get[Option[String]]("barang_keluar.item_name") ~
      get[Option[String]]("barang_keluar.waktu") ~
      get[Option[Int]]("barang_keluar.jumlah_keluar") ~
      get[Option[Int]]("barang_keluar.total") ~
      get[Option[Int]]("barang_keluar.harga_jual") ~
      get[Option[String]]("barang_keluar.catatan") map {
      case sku ~ itemName ~ waktu ~ keluar ~ total ~ harga ~ cat => BarangKeluar(waktu.getOrElse(""), sku.getOrElse(""), itemName.getOrElse(""), keluar.getOrElse(0), harga.getOrElse(0), total.getOrElse(0), cat.getOrElse(""))
    }
  }

  def getBarangKeluar(dateStart:String,dateEnd:String):List[BarangKeluar] = db.withConnection{implicit connection =>
    val query = s"select * from barang_keluar where waktu >= '$dateStart' and waktu <= '$dateEnd'"
    return SQL(query).as(BarangKeluarStructure *)
  }

  def createBarangKeluar(data:(String,String,String,Int,Int,String)):String = db.withConnection { implicit connection =>
    val query = s"INSERT INTO barang_keluar VALUES('${data._1}','${data._2}','${data._3}',${data._4},${data._5},${data._5 * data._4},'${data._6}')"
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
