package io.github.interestinglab.waterdrop.input

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import com.typesafe.config.{Config, ConfigFactory}
import io.github.interestinglab.waterdrop.apis.BaseStaticInput
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.collection.JavaConversions._

class MongoDB extends BaseStaticInput {

  var config: Config = ConfigFactory.empty()

  var readConfig: ReadConfig = _

  val confPrefix = "readConfig"

  override def setConfig(config: Config): Unit = {
    this.config = config
  }

  override def getConfig(): Config = {
    this.config
  }

  override def checkConfig(): (Boolean, String) = {

    config.hasPath(confPrefix) && config.hasPath("table_name") match {
      case true => {
        val read = config.getConfig(confPrefix)
        read.hasPath("uri") && read.hasPath("database") && read.hasPath("collection") match {
          case true => (true, "")
          case false => (false, "please specify [readConfig.uri] and [readConfig.database] and [readConfig.collection]")
        }
      }
      case false => (false, "please specify [readConfig]  and [table_name]")
    }
  }

  override def prepare(spark: SparkSession): Unit = {
    super.prepare(spark)
    val map = new collection.mutable.HashMap[String, String]
    config
      .getConfig(confPrefix)
      .entrySet()
      .foreach(entry => {
        val key = entry.getKey
        val value = String.valueOf(entry.getValue.unwrapped())
        map.put(key, value)
      })
    readConfig = ReadConfig(map)
  }


  override def getDataset(spark: SparkSession): Dataset[Row] = {
    MongoSpark.load(spark, readConfig)
  }

}
