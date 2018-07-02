package Catatan

import DatabaseExecutionContexts.DatabaseExecutionContext
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi
import anorm.SqlParser._
import anorm._
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}

case class JumlahBarang(sku:String,itemName:String,jumlah:Int)

@Singleton
class JumlahBarangBuilder @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    * Parse a Object from a ResultSet
    * output : id object
    */
  private[Catatan] val jumlahBarangStructure = {
    get[Option[String]]("jumlah_barang.sku")~
      get[Option[String]]("jumlah_barang.item_name")~
      get[Option[Int]]("jumlah_barang.qty") map{
      case sku~itemName~jumlah=> JumlahBarang(sku.getOrElse(""),itemName.getOrElse(""),jumlah.getOrElse(0))
    }
  }

  def getJumlahBarangData(sku:String=""):List[JumlahBarang] = db.withConnection { implicit connection =>
      var query = s"select * from jumlah_barang"
    if (sku != ""){
      query += s" where sku = '$sku'"
    }

      return SQL(query).as(jumlahBarangStructure *)
  }

  def createJumlahBarangData(sku:String,itemName:String,jumlah:Int):String = db.withConnection { implicit connection =>
    val query = s"INSERT INTO jumlah_barang VALUES('$sku','$itemName',$jumlah)"
    Try(SQL(query).executeInsert()) match{
      case Failure(e) => {
        return e.getMessage
      }
      case Success(s) => {
        return "success"
      }
    }
  }

  def updateJumlahBarangData(sku:String,itemName:String,jumlah:Int):String = db.withConnection{ implicit connection =>
    val query = s"UPDATE jumlah_barang SET item_name = '$itemName',qty = $jumlah WHERE sku = '$sku'"
    Try(SQL(query).executeUpdate()) match{
      case Failure(e) => {
        return e.getMessage
      }
      case Success(s) => {
        return "success"
      }
    }
  }

}

