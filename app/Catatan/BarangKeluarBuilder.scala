package Catatan

import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

import scala.util.{Failure, Success, Try}

case class BarangKeluar(waktu:String,sku:String,itemName:String,jumlahKeluar:Int,hargaJual:Int,total:Int,catatan:String)

@Singleton
class BarangKeluarBuilder @Inject()(dbapi: DBApi,jb:JumlahBarangBuilder)(implicit ec: DatabaseExecutionContext){
  private val db = dbapi.database("default")

  private val columnType = Map(
    "waktu"->"s",
    "sku"->"s",
    "item_name"->"s",
    "jumlah_keluar"->"i",
    "harga_jual"->"i",
    "total"->"i",
    "catatan"->"s",
    "id"-> "i"
  )

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

  def getBarangKeluarById(id:String):List[BarangKeluar] = db.withConnection{implicit connection =>
    val query = s"select * from barang_keluar where id = '$id'"
    return SQL(query).as(BarangKeluarStructure *)
  }

  def updateBarangKeluar(items:Map[String,Any],wheres:Map[String,Any]):String = db.withConnection { implicit connection =>
    var query = s"update barang_keluar SET "
    //variable buat update tabel jumlah barang JIKA ada update jumlah ke jumlah barang
    var isUpdateJumlah = false
    var isWhereId = false
    var newjumlah = 0
    var barangkeluardata:List[BarangKeluar] = List()

    //input items ke query
    items.foreach{item=>
      columnType.get(item._1).getOrElse("") match {
        case "s"=> query += s"${item._1 }= '${item._2}', "
        case "i" => {
          if (item._1 == "jumlah_keluar"){
            isUpdateJumlah = true
            newjumlah = item._2.toString.toInt
          }
          query += s"${item._1 }= ${item._2}, "
        }
        case _ => return s"${item._1} is not found"
      }
    }
    query = query.dropRight(2)+ s" "

    //input where
    if (wheres.isEmpty)
      return s"where cannot be null!"
    wheres.foreach{where=>
      columnType.get(where._1).getOrElse("") match {
        case "s"=> query += s"WHERE ${where._1} = '${where._2}'"
        case "i" => {
          if (where._1 == "id") {
            isWhereId = true
            barangkeluardata = barangkeluardata :+ getBarangKeluarById(where._2.toString).head
          }
          query += s"WHERE ${where._1} = ${where._2}"
        }
        case _ => {
          return s"${where._1} is not found"
        }
      }
    }

    //update ke sql nya
    Try(SQL(query).executeUpdate()) match{
      case Failure(e) => {
        return e.getMessage
      }
      case Success(s) => {
        //jika wherenya id dan ada jumlah yg di update, update qty di jumlah barang
        if (isUpdateJumlah && isWhereId){
          //get qty
          var qty = jb.getJumlahBarangData(barangkeluardata.head.sku).head.jumlah
          //update qty
          qty -= (newjumlah - barangkeluardata.head.jumlahKeluar)
          //update to db
          jb.updateJumlahBarangData(barangkeluardata.head.sku,barangkeluardata.head.itemName,qty)
        }
        return "success"
      }
    }
  }

  def createBarangKeluar(data:(String,String,String,Int,Int,String),isImport:Boolean=false):String = db.withConnection { implicit connection =>
    val query = s"INSERT INTO barang_keluar(waktu,sku,item_name,jumlah_keluar,harga_jual,total,catatan)" +
      s" VALUES('${data._1}','${data._2}','${data._3}',${data._4},${data._5},${data._5 * data._4},'${data._6}')"
    Try(SQL(query).executeInsert()) match{
      case Failure(e) => {
        return e.getMessage
      }
      case Success(s) => {
        if (!isImport){ // kalau import, cm masukin data trus beres ; kalo bukan, berarti dari REST, jumlah qty di jumlah barang diupdate
          // get qty di jumlah_barang
          var qty = jb.getJumlahBarangData(data._2).head.jumlah
          //ditambah sama jumlah_diterima
          qty -= data._4
          //update jumlah_barang
          jb.updateJumlahBarangData(data._2,data._3,qty)
        }
        return "success"
      }
    }
  }
}
