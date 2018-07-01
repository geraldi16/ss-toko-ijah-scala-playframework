package controllers

import Converter.{DateConverter, IntegerToRupiah}
import DataMigration.CSVExporter
import Laporan.{LaporanPenjualanBuilder, NilaiBarangBuilder}
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class DataMigrationController @Inject()(cc: ControllerComponents, nb:NilaiBarangBuilder, lp:LaporanPenjualanBuilder) extends AbstractController(cc){
  def exportLaporanPenjualan(ds:String,de:String) = Action{
    //minta laporan sesuai tanggal yang diminta
    val data = lp.getLaporanPenjualan(ds,de)
    //hitung : tanggal cetak, tanggal request, total omzet, total laba kotor, total penjualan, total barang
    val (omzet,laba,penjualan,barang,newdata) = lp.countLaporanPenjualan(data,false)

    val preconstructData = Json.obj(
      "tanggal_cetak" ->DateConverter.convertTanggalCetak(DateTime.now()),
      "tanggal"->DateConverter.convertRangeTanggal(ds,de),
      "total_omzet"->omzet,
      "total_laba_kotor"->laba,
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
    val (skuCount,totalBarang,totalNilai,newdata) = nb.countLaporanNilaiBarang(data,false)

    val preconstructData = Json.obj(
      "tanggal_cetak"-> DateConverter.convertTanggalCetak(DateTime.now()),
      "jumlah_sku"->skuCount,
      "jumlah_total_barang"->totalBarang,
      "total_nilai"->totalNilai,
      "data"->newdata
    )
    //export to csv
    val path = CSVExporter.exportLaporanNilaiBarang(preconstructData)

    Ok(s"laporan nilai barang disimpan di $path")
  }
}
