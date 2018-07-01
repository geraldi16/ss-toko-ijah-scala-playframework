package controllers

import Catatan.{BarangKeluarBuilder, BarangMasukBuilder, JumlahBarangBuilder}
import Laporan.{LaporanPenjualan, LaporanPenjualanBuilder, NilaiBarang, NilaiBarangBuilder}
import javax.inject._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import Converter.{DateConverter, IntegerToRupiah}
import org.joda.time.DateTime


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

  def exportToCSV(name:String) = Action{
    Ok("exported!!")
  }
}
