/**
 *
 */
package net.minoves.scalamine

import scala.collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException
import com.typesafe.config.Config
import org.rogach.scallop._
import org.redmine.ta.RedmineManager
import java.io.File

class CLIConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  mainOptions = Seq(quiet, verbose)

  version("Scalamine 0.0.1")
  banner("""A scala Redmine versatile command-line client.
		  	|Usage: test [OPTION] command [sub-commands] args ...
		  	|
            |Options:
            |""".stripMargin)
  val quiet = opt[Boolean](descr = "Suppress output.")
  val verbose = tally(descr = "Incress output level. Each mention rises logging level by one.")
  // needed to have -h, see main.
  val help = opt[Boolean](descr = "Short for --help.")

  val properties = props[Double]()
  val editor = opt[String](descr = "Override editor defined in configuration (or $EDITOR)")
  val pager = opt[String](descr = "Override pager defined in configuration (or $EDITOR)")

  val server = opt[String](descr = "Server to use, must be specified in the configuration file.")

  val view = new Subcommand("view") {

    val direct_view = trailArg[String](descr = "Let scalamine guess the resource to view.",
      required = false)

    val view_project = new Subcommand("project") {
      val vproject = trailArg[String](required = true)
    }
  }

  val edit = new Subcommand("edit") {

    val direct_edit = trailArg[String](descr = "Let scalamine guess the resource to edit.",
      required = false)

    val edit_project = new Subcommand("project") {
      val eproject = trailArg[String](required = true)
    }
  }

  footer("\nEnjoy!");
}

object Scalamine {

  def main(args: Array[String]): Unit = {

    main0(Array("-v", "-s", "test", "view", "project", "project1"))

    //    main0(Array("-v", "-s", "test", "view", "123"))

    //    main0(Array())

    //    main0(Array("--version")) // Scallop calls exit after this

    //    main0(Array("--help")) // Scallop calls exit after this

    //    main0(Array("-q", "hola", "adeu"))
  }
  def main0(args: Array[String]): Unit = {

    try {

      val cliconf = new CLIConf(args)

      if (cliconf.help())
        cliconf.printHelp

      if (cliconf.verbose() > 0) {
        println("CLI parameters:")
        println(cliconf.summary) // Options marked with * are supplied, instead of default value.
      }

      var homeconf = new File(sys.props("user.home") + "/.scalaminerc")
      val conf = ConfigFactory parseFile (homeconf) withFallback (ConfigFactory load "scalamine")

      // question? how to let homeconf be out of scope without making it a var
      homeconf = null;

      val editor = cliconf.editor.get match {
        case Some(e) => e
        case None    => sys.env getOrElse ("EDITOR", conf getString ("editor"))
      }

      val pager = cliconf.pager.get match {
        case Some(p) => p
        case None    => sys.env getOrElse ("PAGER", conf getString ("pager"))
      }

      cliconf.view.commandName match { // TODO Wrond, commandName is fixed
        case "view" =>
          cmdViewProject()
        case _ =>
          throw new Exception("what?")
      }

      /*

      val host = c getString ("servers.test.host")
      val apiKey = c getString ("servers.test.apiKey")

      val rm = new RedmineManager(host, apiKey)

      println("Scalamine v0.0.1")
      println("current user: |" + rm.getCurrentUser() + "|")

      rm.getProjects foreach println

      for { p <- rm.getProjects } yield println(p)
    */

    } catch {
      case ex: ConfigException => {
        println(ex.getMessage)
      }
      case e: Exception => {
        println("Oops: " + e)
      }
    }

    def cmdViewProject() {
      println("view!")
    }
  }
}