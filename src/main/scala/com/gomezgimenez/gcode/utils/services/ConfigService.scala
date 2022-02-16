package com.gomezgimenez.gcode.utils.services

import java.io.{ BufferedWriter, File, FileWriter }

import com.gomezgimenez.gcode.utils.entities.{ AlignmentFrames, Configuration, Frame }
import com.gomezgimenez.gcode.utils.model.{ AlignToolModel, GlobalModel }
import org.json4s.{ Formats, NoTypeHints }
import org.json4s.native.Serialization

import scala.io.Source
import scala.util.Try

case class ConfigService() {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  def loadConfiguration: Option[Configuration] = {
    val configFile = new File("./settings.config")
    if (configFile.exists()) {
      val source        = Source.fromFile(configFile)
      val fileContents  = source.mkString
      val configuration = Try(fromJson[Configuration](fileContents)).toOption
      source.close
      configuration
    } else {
      None
    }
  }

  def saveConfiguration(configuration: Configuration): Unit = {
    val configFile = new File("./settings.config")
    val bw         = new BufferedWriter(new FileWriter(configFile))
    val json       = toJson(configuration)
    json.split("\n").foreach { line =>
      bw.write(line + "\n")
    }
    bw.close()
  }

  def buildConfiguration(globalModel: GlobalModel, alignToolModel: AlignToolModel): Configuration = {
    val alignmentFrames = for {
      originalFrame <- alignToolModel.originalFrame.get
      measuredFrame <- alignToolModel.measuredFrame.get
    } yield {
      AlignmentFrames(originalFrame, measuredFrame)
    }
    Configuration(alignmentFrames)
  }

  def populateModelFromConfig(config: Configuration, globalModel: GlobalModel, alignToolModel: AlignToolModel): Unit =
    config.alignmentFrames.foreach { f =>
      alignToolModel.originalTopLeftPoint.set(Some(f.originalFrame.topLeft))
      alignToolModel.originalTopRightPoint.set(Some(f.originalFrame.topRight))
      alignToolModel.originalBottomLeftPoint.set(Some(f.originalFrame.bottomLeft))
      alignToolModel.originalBottomRightPoint.set(Some(f.originalFrame.bottomRight))
      alignToolModel.originalFrame.set(
        Some(Frame(
          topLeft = alignToolModel.originalTopLeftPoint.get.get,
          topRight = alignToolModel.originalTopRightPoint.get.get,
          bottomLeft = alignToolModel.originalBottomLeftPoint.get.get,
          bottomRight = alignToolModel.originalBottomRightPoint.get.get
        )))
      alignToolModel.measuredTopLeftPoint.set(Some(f.measuredFrame.topLeft))
      alignToolModel.measuredTopRightPoint.set(Some(f.measuredFrame.topRight))
      alignToolModel.measuredBottomLeftPoint.set(Some(f.measuredFrame.bottomLeft))
      alignToolModel.measuredBottomRightPoint.set(Some(f.measuredFrame.bottomRight))
      alignToolModel.measuredFrame.set(
        Some(Frame(
          topLeft = alignToolModel.measuredTopLeftPoint.get.get,
          topRight = alignToolModel.measuredTopRightPoint.get.get,
          bottomLeft = alignToolModel.measuredBottomLeftPoint.get.get,
          bottomRight = alignToolModel.measuredBottomRightPoint.get.get
        )))
    }
}
