package controllers

import Converter.{DateConverter, IntegerToRupiah}
import Laporan.{LaporanPenjualan, LaporanPenjualanBuilder}
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class LaporanPenjualanController @Inject()(cc: ControllerComponents,lp:LaporanPenjualanBuilder) extends AbstractController(cc)  {

}
