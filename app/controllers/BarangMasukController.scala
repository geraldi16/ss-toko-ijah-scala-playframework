package controllers

import Catatan.BarangMasukBuilder
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class BarangMasukController @Inject()(cc: ControllerComponents,bm:BarangMasukBuilder) extends AbstractController(cc) {
  def createCatatanBarangMasuk = Action {implicit request =>
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
      (json \ "waktu").as[String]
    }.get
    val pemesanan:Int = jsonBody.map { json =>
      (json\"pemesanan").as[Int]
    }.get
    val terima:Int = jsonBody.map { json =>
      (json\"terima").as[Int]
    }.get
    val beli:Int = jsonBody.map { json =>
      (json\"beli").as[Int]
    }.get
    val kwit:String = jsonBody.map { json =>
      (json\"kwitansi").as[String]
    }.get
    val cat:String = jsonBody.map { json =>
      (json\"catatan").as[String]
    }.get

    //create data baru
    val create = bm.createBarangMasuk((waktu,sku,itemName,pemesanan,terima,beli,kwit,cat))

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

  def updateCatatanBarangMasuk() = Action {implicit request =>
    var result = Json.obj()
    //get request params
    val jsonBody: JsValue = request.body.asJson.get
    val jsonItems = (jsonBody \"items").as[List[JsObject]]
    val jsonWhere = (jsonBody \"where").as[List[JsObject]]
    var items:Map[String,Any] = Map()
    var where:Map[String,Any] = Map()

    jsonItems.foreach{json=>
      items = items +((json \ "name").as[String]->(json \ "value").as[String])
    }

    jsonWhere.foreach{json=>
      where = where +((json \ "name").as[String]->(json \ "value").as[String])
    }

    //update data
    val update = bm.updateBarangMasuk(items,where)

    if (update == "success"){
      result = Json.obj(
        "status"->"success",
        "message"-> jsonBody.toString
      )
    }else{
      result = Json.obj(
        "status"->s"error",
        "message"->update
      )
    }
    Ok(result).as(JSON)
  }
}
