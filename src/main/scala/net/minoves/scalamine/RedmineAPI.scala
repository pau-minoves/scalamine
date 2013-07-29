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

case class ProjectReference(
  id: Int,
  name: String)

case class Project(
  id: Int,
  name: String,
  identifier: String,
  description: String,
  homepage: String,
  created_on: String,
  updated_on: String)

case class Issue(
  id: Int,
  project: ProjectReference,
  subject: String,
  description: String,
  start_date: String,
  done_ratio: Int,
  spent_hours: Double,
  created_on: String,
  updated_on: String)

object RedmineProtocol extends DefaultJsonProtocol {
  /* {"issue":
   * {
   * "id":1,
   * "project":{"id":1,"name":"project1"},
   * "tracker":{"id":2,"name":"Feature"},
   * "status":{"id":1,"name":"New"},
   * "priority":{"id":2,"name":"Normal"},
   * "author":{"id":1,"name":"Pau Minoves"},
   * "assigned_to":{"id":1,"name":"Pau Minoves"},
   * "subject":"Feature 1",
   * "description":"Feature 1 for testing",
   * "start_date":"2013-07-04",
   * "done_ratio":0,
   * "spent_hours":0.0,
   * "created_on":"2013-07-04T22:13:38Z",
   * "updated_on":"2013-07-04T22:13:38Z"
   * }
   * }
  */
  implicit val projectReference = jsonFormat2(ProjectReference)
  implicit val projectFormat = jsonFormat7(Project)
  implicit val issueFormat = jsonFormat9(Issue)
}

import RedmineProtocol._

case class RedmineAPI(conf: Config, instance: RedmineInstance) extends Logging {

  implicit def resolveProject(pr: ProjectReference): Project = {
    project(pr.id)
  }

  trace("init start")

  val client = new DefaultHttpClient

  val brh = new BasicResponseHandler // needed so responses are parsed as String (implicitly of type [String])

  trace("init end")

  def get(partialPath: String): HttpGet = {
    new HttpGet(instance.host + partialPath + ".json?key=" + instance.apiKey)
  }

  def project(id: Int): Project = project(id.toString)

  def project(id: String): Project = {
    client.execute(get("/projects/" + id), brh)
      .asJson
      .asJsObject
      .fields("project")
      .convertTo[Project]
  }

  def issue(id: String): Issue = {
    client.execute(get("/issues/" + id), brh)
      .asJson
      .asJsObject
      .fields("issue")
      .convertTo[Issue]
  }

  def shutdown(): Unit = {
    (client getConnectionManager) shutdown
  }
}