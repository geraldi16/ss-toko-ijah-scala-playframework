package DataMigration

import Converter.DateConverter
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.natures.csv.Model2CsvConversions._
import org.joda.time.DateTime
import play.api.libs.json.JsObject

sealed trait CSVExporter {
  def exportLaporanPenjualan(data:JsObject):String = {
    //isi row berisi rangkuman
    val coreRow = List(
      Row(style = CellStyle(font = Font(bold = true))).withCellValues("LAPORAN PENJUALAN"),
      Row().withCellValues(),
      Row().withCellValues("Tanggal Cetak",(data \ "tanggal_cetak").as[String]),
      Row().withCellValues("Tanggal",(data \ "tanggal").as[String]),
      Row().withCellValues("Total Omzet",(data \ "total_omzet").as[Int]),
      Row().withCellValues("Total Laba Kotor",(data \ "total_laba_kotor").as[Int]),
      Row().withCellValues("Total Penjualan",(data \ "total_penjualan").as[Int]),
      Row().withCellValues("Total Barang",(data \ "total_barang").as[Int]),
      Row().withCellValues(),
      Row(style = CellStyle(font = Font(bold = true))).withCellValues("ID Pesanan","Waktu","SKU","Nama Barang","Jumlah","Harga Jual","Total","Harga Beli","Laba")
    )
    //isi row data laporan penjualan
    val contentRow = convertDataPenjualanToRowContent((data \ "data").as[List[JsObject]])

    ///define sheet
    val laporanPenjualanSheet = Sheet(name="Laporan Penjualan")
      .withRows(coreRow ++ contentRow)
      .withColumns(
        Column(index=0,autoSized = true),
        Column(index=1,autoSized = true),
        Column(index=2,autoSized = true),
        Column(index=3,autoSized = true),
        Column(index=4,autoSized = true),
        Column(index=5,autoSized = true),
        Column(index=6,autoSized = true),
        Column(index=7,autoSized = true),
        Column(index=8,autoSized = true)
      )

    //bikin filename
    val filename = s"${DateConverter.convertSheetFormatDate(DateTime.now())}-Laporan-Penjualan.csv"
    //save ke csv
    val dir = s"${System.getProperty("user.dir")}/Export/Penjualan/$filename"
    laporanPenjualanSheet.saveAsCsv(dir)
    dir

  }

  def exportLaporanNilaiBarang(data:JsObject):String = {
    //isi row yang berisi rangkuman
    val coreRow = List(
      Row(style = CellStyle(font = Font(bold = true))).withCellValues("LAPORAN NILAI BARANG"),
      Row().withCellValues(),
      Row().withCellValues("Tanggal Cetak",(data \ "tanggal_cetak").as[String]),
      Row().withCellValues("Jumlah SKU",(data \ "jumlah_sku").as[Int]),
      Row().withCellValues("Jumlah Total Barang",(data \ "jumlah_total_barang").as[Int]),
      Row().withCellValues("Jumlah Total Nilai",(data \ "total_nilai").as[Int]),
      Row().withCellValues(),
      Row(style = CellStyle(font = Font(bold = true))).withCellValues("SKU","Nama Item","Jumlah","Rata-rata Harga Beli","Total")
    )

    //isi row yang isinya data nilai barang
    val contentRow = convertDataNilaiBarangToRowContent((data \ "data").as[List[JsObject]])

    //define sheet
    val laporanNilaiBarangSheet = Sheet(name="Laporan Nilai Barang")
      .withRows(coreRow ++ contentRow)
      .withColumns(
        Column(index=0,autoSized = true),
        Column(index=1,autoSized = true),
        Column(index=2,autoSized = true),
        Column(index=3,autoSized = true),
        Column(index=4,autoSized = true),
        Column(index=5,autoSized = true)
      )

    //bikin filename
    val filename = s"${DateConverter.convertSheetFormatDate(DateTime.now())}-Laporan-Nilai-Barang.csv"
    //save ke csv
    val dir = s"${System.getProperty("user.dir")}/Export/NilaiBarang/$filename"
    laporanNilaiBarangSheet.saveAsCsv(dir)
    dir
  }

  private def convertDataNilaiBarangToRowContent(data:List[JsObject]):List[Row] = {
    val result = data.map{datum=>
      Row().withCellValues(
        (datum \ "sku").as[String],
        (datum \ "item_name").as[String],
        (datum \ "jumlah").as[Int],
        (datum \ "harga_average").as[String],
        (datum \ "total").as[String]
      )
    }
    result
  }

  private def convertDataPenjualanToRowContent(data:List[JsObject]):List[Row] = {
    val result = data.map{datum=>
      Row().withCellValues(
        (datum \ "id_pesanan").as[String],
        (datum \ "waktu").as[String],
        (datum \ "sku").as[String],
        (datum \ "item_name").as[String],
        (datum \ "jumlah").as[Int],
        (datum \ "harga_jual").as[String],
        (datum \ "total").as[String],
        (datum \ "harga_beli").as[String],
        (datum \ "laba").as[String]
      )
    }
    result
  }
}

object CSVExporter extends CSVExporter {

}
