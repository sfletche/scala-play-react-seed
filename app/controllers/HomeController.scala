package controllers

import javax.inject._
import scalikejdbc._

import play.api.libs.json.Json
import play.api.mvc._


@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def appSummary = Action {
    Ok(Json.obj("content" -> "Scala Play React Seed"))
  }

  def appLegoList = Action {
    // initialize JDBC driver & connection pool
    Class.forName("com.mysql.jdbc.Driver")
    ConnectionPool.singleton("jdbc:mysql://localhost/legos?characterEncoding=UTF-8", "root", "")

    // ad-hoc session provider on the REPL
    implicit val session = AutoSession

    val query: List[Map[String, Any]] = {
      sql"select id, name, completed from lego_kits;".map(_.toMap).list.apply()
    }

    Ok(Json.obj(
      "content" -> Json.toJsFieldJsValueWrapper(Json.obj(
        "list" -> query.map(row => Json.obj(
          "id" -> s"${row("id")}",
          "name" -> s"${row("name")}",
          "completed" -> s"${row("completed")}"
        ))
      ))
    ))
  }

  def getLegoDetails(id: String) = Action {
    // initialize JDBC driver & connection pool
    Class.forName("com.mysql.jdbc.Driver")
    ConnectionPool.singleton("jdbc:mysql://localhost/legos?characterEncoding=UTF-8", "root", "")

    // ad-hoc session provider on the REPL
    implicit val session = AutoSession

    case class Lego(id: String, name: String, completed: Boolean)
    val details: Option[Lego] = {
      sql"select id, name, completed from lego_kits where id = ${id};".map(rs =>
        Lego(rs.string("id"), rs.string("name"), rs.boolean("completed"))).single.apply()
    }

    Ok(Json.obj(
      "content" -> Json.toJsFieldJsValueWrapper(Json.obj(
        "id" -> s"${details.get.id}",
        "name" -> s"${details.get.name}",
        "completed" -> s"${details.get.completed}"
      ))
    ))
  }

  def setLegoDetails() = Action { request =>
    val json = request.body.asJson.get
    val id = (json \ "id").asOpt[String]
    val name = (json \ "name").asOpt[String]
    val completed = (json \ "completed").asOpt[String]

    //    val requestObj = Map(
    //      "id" -> (json \ "id").asOpt[String],
    //      "name" -> (json \ "name").asOpt[String],
    //      "completed" -> (json \ "completed").asOpt[String]
    //    )

    // initialize JDBC driver & connection pool
    Class.forName("com.mysql.jdbc.Driver")
    ConnectionPool.singleton("jdbc:mysql://localhost/legos?characterEncoding=UTF-8", "root", "")

    // ad-hoc session provider on the REPL
    implicit val session = AutoSession

    val query =
      sql"""
           update lego_kits
           set name = ${name.get}, completed = ${completed.get.toBoolean}
           where id = ${id.get};
        """
    DB localTx { implicit session => query.update.apply() }

    case class Lego(id: String, name: String, completed: Boolean)
    val details: Option[Lego] = {
      sql"select id, name, completed from lego_kits where id = ${id.get};".map(rs =>
        Lego(rs.string("id"), rs.string("name"), rs.boolean("completed"))).single.apply()
    }

    Ok(Json.obj(
      "content" -> Json.toJsFieldJsValueWrapper(Json.obj(
        "id" -> s"${details.get.id}",
        "name" -> s"${details.get.name}",
        "completed" -> s"${details.get.completed}"
      ))
    ))
  }
}
