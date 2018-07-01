package controllers

import Catatan.{BarangKeluarBuilder, BarangMasukBuilder, JumlahBarangBuilder}
import Converter.{DateConverter, IntegerToRupiah}
import DataMigration.{CSVExporter, CSVImporter}
import Laporan.{LaporanPenjualanBuilder, NilaiBarangBuilder}
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.io.Source

@Singleton
class DataMigrationController @Inject()(cc: ControllerComponents, nb:NilaiBarangBuilder, lp:LaporanPenjualanBuilder,jb:JumlahBarangBuilder,bm:BarangMasukBuilder,bk:BarangKeluarBuilder) extends AbstractController(cc){
  def exportLaporanPenjualan(ds:String,de:String) = Action{
    //minta laporan sesuai tanggal yang diminta
    val data = lp.getLaporanPenjualan(ds,de)
    //hitung : tanggal cetak, tanggal request, total omzet, total laba kotor, total penjualan, total barang
    val (omzet,laba,penjualan,barang,newdata) = lp.countLaporanPenjualan(data)

    val preconstructData = Json.obj(
      "tanggal_cetak" ->DateConverter.convertTanggalCetak(DateTime.now()),
      "tanggal"->DateConverter.convertRangeTanggal(ds,de),
      "total_omzet"->IntegerToRupiah.convert(omzet),
      "total_laba_kotor"->IntegerToRupiah.convert(laba),
      "total_penjualan"->penjualan,
      "total_barang"->barang,
      "data"->newdata
    )

    //export to CSV
    val path = CSVExporter.exportLaporanPenjualan(preconstructData)

    Ok(s"laporan penjualan disimpan di $path")
  }

  def exportLaporanNilaiBarang() = Action{
    //minta laporan sesuai tanggal yang diminta
    val data = nb.getLaporanNilaiBarang()

    //hitung: jumlah sku, total barang, akumulasi nilai total, tanggal cetak hari ini
    val (skuCount,totalBarang,totalNilai,newdata) = nb.countLaporanNilaiBarang(data)

    val preconstructData = Json.obj(
      "tanggal_cetak"-> DateConverter.convertTanggalCetak(DateTime.now()),
      "jumlah_sku"->skuCount,
      "jumlah_total_barang"->totalBarang,
      "total_nilai"->IntegerToRupiah.convert(totalNilai),
      "data"->newdata
    )
    //export to csv
    val path = CSVExporter.exportLaporanNilaiBarang(preconstructData)

    Ok(s"laporan nilai barang disimpan di $path")
  }

  def importJumlahBarang(filename:String) = Action{
    val path = s"${System.getProperty("user.dir")}/Import/JumlahBarang/$filename.csv"
    val bufferedSource = Source.fromFile(path)

    for (line <- bufferedSource.getLines.drop(1)) {
      val row = line.split(",").map(_.trim)
      //insert row ke database
      val itemName = s"${row(1)},${row(2)}"
      jb.createJumlahBarangData(row(0),itemName,row(3).toInt)
    }
    Ok("data imported!")
  }

  def importBarangMasuk(filename:String) = Action{
    val path = s"${System.getProperty("user.dir")}/Import/BarangMasuk/$filename.csv"
    val bufferedSource = Source.fromFile(path)

    for (line <- bufferedSource.getLines.drop(1)) {
      val oldrow = line.split(",").map(_.trim)
      //insert row ke database
      val row = appendSeparatedString(oldrow.toList)
      bm.createBarangMasuk((row(0),row(1),row(2),row(3).toInt,row(4).toInt,row(5).toInt,row(7),row(8)))
    }
    Ok("data imported!")
  }

  def importBarangKeluar(filename:String) = Action{
    val path = s"${System.getProperty("user.dir")}/Import/BarangKeluar/$filename.csv"
    val bufferedSource = Source.fromFile(path)

    for (line <- bufferedSource.getLines.drop(1)) {
      val row = line.split(",").map(_.trim)
      //insert row ke database
      val newrow = appendSeparatedString(row.toList)
      bk.createBarangKeluar((newrow(0),newrow(1),newrow(2),newrow(3).toInt,newrow(4).toInt,newrow(6)))
    }
    Ok("data imported!")
  }

  private def appendSeparatedString(data:List[String]):List[String] = {
    var result:List[String] = List()

    var i = 0
    var temp = ""

    data.foreach{datum=>
      datum.count(_ == '"') match {
        case 0 => if (i==0) result = result :+ datum.replace("Rp","") else temp += ","+datum
        case 2 => if (i==0) result = result :+ datum.replace("Rp","") else temp += ","+datum
        case 1 =>{
          if (i ==0){
            i = 1
            temp += datum
          }else{
            //kalo i == 1, append datum dan temp
            var fin = temp + "," + datum

            if (fin.contains("Rp")) fin = fin.replace("Rp","").replace(",","")
            result = result :+ fin.trim.replace("\"","")
            i = 0
            temp = ""
          }
        }
      }
    }

    result
  }
}
