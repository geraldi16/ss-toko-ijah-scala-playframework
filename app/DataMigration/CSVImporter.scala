package DataMigration

import Catatan.JumlahBarangBuilder
import DatabaseExecutionContexts.DatabaseExecutionContext
import play.api.db.{DBApi, Database}
import akka.actor.{Actor, ActorSystem}
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy

import scala.io.Source

sealed trait CSVImporter extends Actor {

  def importJumlahBarang(filename:String) = {
    val path = s"${System.getProperty("user.dir")}/Import/JumlahBarang/$filename.csv"
    val bufferedSource = Source.fromFile(path)

    for (line <- bufferedSource.getLines.drop(1)) {
      val row = line.split(",").map(_.trim)
      //insert row ke database

    }

  }

  def importBarangMasuk() =  {

  }

  def importBarangKeluar() = {

  }
}
