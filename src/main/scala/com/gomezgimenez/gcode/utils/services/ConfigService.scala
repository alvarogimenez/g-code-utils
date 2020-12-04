package com.gomezgimenez.gcode.utils.services

import java.io.{BufferedWriter, File, FileWriter}

import com.gomezgimenez.gcode.utils.entities.{AlignmentFrames, Configuration}
import com.gomezgimenez.gcode.utils.model.DataModel
import org.json4s.{Formats, NoTypeHints}
import org.json4s.native.Serialization

import scala.io.Source
import scala.util.Try

case class ConfigService() {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  def loadConfiguration: Option[Configuration] = {
    val configFile = new File("./settings.config")
    if(configFile.exists()) {
      val source = Source.fromFile(configFile)
      val fileContents = source.mkString
      val configuration = Try(fromJson[Configuration](fileContents)).toOption
      source.close
      configuration
    } else {
      None
    }
  }

  def saveConfiguration(configuration: Configuration): Unit = {
    val configFile = new File("./settings.config")
    val bw = new BufferedWriter(new FileWriter(configFile))
    val json = toJson(configuration)
    json.split("\n").foreach { line =>
      bw.write(line + "\n")
    }
    bw.close()
  }

  def buildConfiguration(model: DataModel): Configuration = {
    val alignmentFrames = for {
      originalFrame <- model.originalFrame.get
      measuredFrame <- model.measuredFrame.get
    } yield {
      AlignmentFrames(originalFrame, measuredFrame)
    }
    Configuration(alignmentFrames)
  }
}
