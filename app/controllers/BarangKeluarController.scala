package controllers

import Catatan.BarangKeluarBuilder
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class BarangKeluarController @Inject()(cc: ControllerComponents,bk:BarangKeluarBuilder) extends AbstractController(cc) {
  def createCatatanBarangKeluar = Action {implicit request =>
    var result = Json.obj()
    //get request params
    val jsonBody: Option[JsValue] = request.body.asJson

    val sku:String = jsonBody.map { json =>
      (json\"sku").as[String]
    }.get
    val itemName:String = jsonBody.map { json =>
      (json\"item_name").as[String]
    }.get
    val waktu:String = jsonBody.map { json =>
      (json\"waktu").as[String]
    }.get
    val catatan:String = jsonBody.map { json =>
      (json\"catatan").as[String]
    }.get
    val keluar:Int = jsonBody.map { json =>
      (json\"keluar").as[Int]
    }.get
    val jual:Int = jsonBody.map { json =>
      (json\"jual").as[Int]
    }.get
    val total:Int = jsonBody.map { json =>
      (json\"total").as[Int]
    }.get

    //create data baru
    val create = bk.createBarangKeluar((waktu,sku,itemName,keluar,jual,total,catatan))

    if (create == "success"){
      result = Json.obj(
        "status"->"success",
        "message"-> jsonBody.toString
      )
    }else{
      result = Json.obj(
        "status"->s"error",
        "message"->create
      )
    }
    Ok(result).as(JSON)
  }
}
