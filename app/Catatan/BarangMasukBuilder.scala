package Catatan

import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

import scala.util.{Failure, Success, Try}

case class BarangMasuk(waktu:String,sku:String,itemName:String,jumlahPemesanan:Int,jumlahDiterima:Int,hargaBeli:Int,total:Int,noKwin:String,catatan:String)

@Singleton
class BarangMasukBuilder @Inject()(dbapi: DBApi,jb:JumlahBarangBuilder)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  private val columnType = Map(
    "waktu"->"s",
    "sku"->"s",
    "item_name"->"s",
    "jumlah_pemesanan"->"i",
    "harga_beli"->"i",
    "total"->"i",
    "catatan"->"s",
    "jumlah_diterima"->"i",
    "nomer_kwitansi"->"s",
    "id"-> "i"
  )

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
      get[Option[Int]]("barang_masuk.id") map {
        case sku ~ itemName ~ waktu ~ pesan ~ terima ~ harga ~ kwit ~ cat ~ total => BarangMasuk(waktu.getOrElse(""), sku.getOrElse(""), itemName.getOrElse(""), pesan.getOrElse(0), terima.getOrElse(0), harga.getOrElse(0), total.getOrElse(0), kwit.getOrElse(""), cat.getOrElse(""))
      }
  }

  private[Catatan] val HargaBeliAverageStructure = {
    get[String]("barang_masuk.sku") ~
      get[Int]("pesan") ~
      get[Int]("harga_avg") map {
      case sku ~ pesan ~ total => {
        (sku->total/pesan)
      }
    }
  }

  def getBarangMasuk(dateStart:String="",dateEnd:String=""):List[BarangMasuk] = db.withConnection{implicit connection =>
    var query = ""
    if (dateEnd == "" || dateStart ==""){
      query = s"select * from barang_masuk where 1"
    }else{
      query = s"select * from barang_masuk where waktu >= '$dateStart' and waktu <= '$dateEnd'"
    }

    return SQL(query).as(BarangMasukStructure *)
  }

  def getBarangMasukById(id:String):List[BarangMasuk] = db.withConnection{implicit connection =>
    val query = s"select * from barang_masuk where id = $id"

    return SQL(query).as(BarangMasukStructure *)
  }

  /*
   * Fungsi utk mendapatkan harga beli rata dari tiap SKU
   * return : List(Map(sku, harga beli rata2))
   */
  def getHargaBeliAverageList(dateStart:String="",dateEnd:String=""):Map[String,Int] = db.withConnection{implicit connection =>
    var query = ""
    if (dateEnd == "" || dateStart ==""){
      query = s"select sku,sum(jumlah_pemesanan) as pesan,sum(total) as harga_avg from barang_masuk group by sku"
    }else{
      query = s"select sku,sum(jumlah_pemesanan) as pesan,sum(total) as harga_avg from barang_masuk where waktu >= '$dateStart' and waktu <= '$dateEnd' group by sku"
    }

    return SQL(query).as(HargaBeliAverageStructure *).foldLeft[Map[String,Int]](Map()){
      case (res,curr)=> {
        res + (curr._1 -> curr._2)
      }
    }
  }

  def updateBarangMasuk(items:Map[String,Any],wheres:Map[String,Any]):String = db.withConnection { implicit connection =>
    var query = s"update barang_masuk SET "
    //variable buat update tabel jumlah barang JIKA ada update jumlah ke jumlah barang
    var isUpdateJumlah = false
    var isWhereId = false
    var newjumlah = 0
    var barangmasukdata:List[BarangMasuk] = List()

    //input items ke query
    items.foreach{item=>
      columnType.get(item._1).getOrElse("") match {
        case "s"=> query += s"${item._1 }= '${item._2}', "
        case "i" => {
          if (item._1 == "jumlah_diterima"){
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
            barangmasukdata = barangmasukdata :+ getBarangMasukById(where._2.toString).head
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
          var qty = jb.getJumlahBarangData(barangmasukdata.head.sku).head.jumlah
          //update qty
          qty += (newjumlah - barangmasukdata.head.jumlahDiterima)
          //update to db
          jb.updateJumlahBarangData(barangmasukdata.head.sku,barangmasukdata.head.itemName,qty)
        }
        return "success"
      }
    }
  }

  def createBarangMasuk(data:(String,String,String,Int,Int,Int,String,String),isImport:Boolean=false):String = db.withConnection { implicit connection =>
    val query = s"INSERT INTO barang_masuk(waktu,sku,item_name,jumlah_pemesanan,jumlah_diterima,harga_beli,total,nomer_kwitansi,catatan)" +
      s" VALUES('${data._1}','${data._2}','${data._3}',${data._4},${data._5},${data._6},${data._6 * data._4},'${data._7}','${data._8}')"
    Try(SQL(query).executeInsert()) match{
      case Failure(e) => {
        return e.getMessage
      }
      case Success(s) => {
        if (!isImport){ // kalau import, cm masukin data trus beres ; kalo bukan, berarti dari REST, jumlah qty di jumlah barang diupdate
          // get qty di jumlah_barang
          var qty = jb.getJumlahBarangData(data._2).head.jumlah
          //ditambah sama jumlah_diterima
          qty += data._5
          //update jumlah_barang
          jb.updateJumlahBarangData(data._2,data._3,qty)
        }
        return "success"
      }
    }
  }
}

