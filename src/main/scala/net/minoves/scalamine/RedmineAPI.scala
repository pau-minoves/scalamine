package net.minoves.scalamine

import com.typesafe.config.Config
import spray.json._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import grizzled.slf4j.Logging
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.ResponseHandler
import org.apache.http.impl.client.BasicResponseHandler

import java.util.Date

case class RedmineInstance(
  host: String,
  path: String,
  port: Int,
  apiKey: String)

case class Project(
  id: Int,
  name: String,
  identifier: String,
  description: String,
  homepage: String,
  createdOn: String,
  updatedOn: String)
case class Issue(id: Int, subject: String)

object RedmineProtocol extends DefaultJsonProtocol {
  /* {"project":{
   * "id":1,
   * "name":"project1",
   * "identifier":"project1",
   * "description":"Description for project 1 (non-public)",
   * "homepage":"",
   * "created_on":"2013-07-04T22:12:57Z",
   * "updated_on":"2013-07-04T22:12:57Z"}}
  */
  implicit val projectFormat = jsonFormat7(Project)
  implicit val issueFormat = jsonFormat2(Issue)
}

import RedmineProtocol._

case class RedmineAPI(conf: Config, instance: RedmineInstance) extends Logging {

  trace("init start")

  val client = new DefaultHttpClient

  val brh = new BasicResponseHandler // needed so responses are parsed as String

  trace("init end")

  def project(identifier: String): Project = {

    val get = new HttpGet(instance.host + "/projects/" + identifier + ".json?key=" + instance.apiKey)

    val project = client.execute(get, brh)

    debug(project)
    debug(project.asJson.x)
    debug(project.asJson.asJsObject("project"))

    return project.asJson.convertTo[Project]
  }

  def shutdown(): Unit = {
    (client getConnectionManager) shutdown
  }
}