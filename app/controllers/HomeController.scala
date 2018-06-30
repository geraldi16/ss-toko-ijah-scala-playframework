package controllers

import dataLayer.PrimaryLayer
import javax.inject._
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def showLaporanNilaiBarang() = Action {
    //minta laporan sesuai tanggal yang diminta

    //hitung: jumlah sku, total barang, akumulasi nilai total, tanggal cetak hari ini

    Ok("ok")
  }

  def showLaporanPenjualan() = Action {
    //get date start dan date end

    //minta laporan sesuai tanggal yang diminta

    //hitung : tanggal cetak, tanggal request, total omzet, total laba kotor, total penjualan, total barang

    Ok("ok laporan penjualan")
  }

  def createCatatanJumlahBarang = Action {
    implicit request =>
      //get request params
      val jsonBody: Option[JsValue] = request.body.asJson

      //cek sku nilai barang

      //kalau ada, update angkanya

      //kalau ga ada, create baru
    Ok("catatan barang").as(JSON)
  }

  def createCatatanBarangMasuk = Action {implicit request =>
    //get request params
    val jsonBody: Option[JsValue] = request.body.asJson

    //create data baru

    Ok("catatan barang masuk").as(JSON)
  }

  def createCatatanBarangKeluar = Action {implicit request =>
    //get request params
    val jsonBody: Option[JsValue] = request.body.asJson

    //create data baru
    Ok("catatan barang keluar").as(JSON)
  }

  def exportToCSV(name:String) = Action{
    Ok("exported!!")
  }
}
