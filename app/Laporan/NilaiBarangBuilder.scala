package Laporan

import DatabaseExecutionContexts.DatabaseExecutionContext
import anorm.SqlParser.get
import anorm.{SQL, ~}
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

case class NilaiBarang(sku:String,itemName:String,jumlah:Int,harga:Int,total:Int)

@Singleton
class NilaiBarangBuilder @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    * Parse a Object from a ResultSet
    * output : id object
    */
  private[Laporan] val LaporanNilaiBarangStructure = {
    get[Option[String]]("laporan_nilai_barang.sku") ~
      get[Option[String]]("laporan_nilai_barang.item_name") ~
      get[Option[Int]]("laporan_nilai_barang.jumlah") ~
      get[Option[Int]]("laporan_nilai_barang.harga_beli_avg") ~
      get[Option[Int]]("laporan_nilai_barang.total") map {
      case sku ~ itemName ~ jumlah ~ beli ~ total=> NilaiBarang(sku.getOrElse(""), itemName.getOrElse(""), jumlah.getOrElse(0), beli.getOrElse(0), total.getOrElse(0))
    }
  }

  def getLaporanNilaiBarang(dateStart:String="",dateEnd:String=""):List[NilaiBarang] = db.withConnection{implicit connection =>
    val query = s"select * from laporan_nilai_barang where 1"

    return SQL(query).as(LaporanNilaiBarangStructure *)
  }
}
