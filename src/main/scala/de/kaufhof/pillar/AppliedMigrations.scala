package de.kaufhof.pillar

//import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
//import com.datastax.driver.core.TableMetadata
import com.datastax.driver.core._
import scala.collection.JavaConverters._

import scala.collection.JavaConversions
import java.util.Date

object AppliedMigrations {
  def apply(session: Session, registry: Registry, appliedMigrationsTableName: String): AppliedMigrations = {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from(appliedMigrationsTableName))
    new AppliedMigrations(JavaConversions.asScalaBuffer(results.all()).map {
      row => registry(MigrationKey(row.getTimestamp("authored_at"), row.getString("description")))
    })
  }

  def dropTables(session: Session) {
    val metadata = session.getCluster.getMetadata();
    val tablesMetadata = metadata.getKeyspace(session.getLoggedKeyspace).getTables
    tablesMetadata.forEach(println(_))
    //tablesMetadata.forEach(session.execute("drop table _*"))
    //val dropTableCQL = SimpleStatement("drop table test_table");
  }

}

class AppliedMigrations(applied: Seq[Migration]) {
  def length: Int = applied.length

  def apply(index: Int): Migration = applied.apply(index)

  def iterator: Iterator[Migration] = applied.iterator

  def authoredAfter(date: Date): Seq[Migration] = applied.filter(migration => migration.authoredAfter(date))

  def contains(other: Migration): Boolean = applied.contains(other)
}
