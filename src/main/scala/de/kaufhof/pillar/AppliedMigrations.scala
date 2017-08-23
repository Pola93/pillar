package de.kaufhof.pillar

import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.collection.JavaConverters
import com.datastax.driver.core._


import java.util.Date

object AppliedMigrations {
  def apply(session: Session, registry: Registry, appliedMigrationsTableName: String): AppliedMigrations = {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from(appliedMigrationsTableName))
    new AppliedMigrations(JavaConverters.asScalaBuffer(results.all()).map {
      row => registry(MigrationKey(row.getTimestamp("authored_at"), row.getString("description")))
    })
  }

  def check(session: Session, registry: Registry, appliedMigrationsTableName: String) {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from(appliedMigrationsTableName))
    try {
      new AppliedMigrations(JavaConverters.asScalaBuffer(results.all()).map {
        row => registry(MigrationKey(row.getTimestamp("authored_at"), row.getString("description")))
      })
    } catch {
      case ex: Throwable => {
        dropTables(session)
        createMigrationsTable(session, appliedMigrationsTableName)
      }
    }

  }

  def dropTables(session: Session) {
    println(" !! WARNING ALL TABLES in keyspace:" + session.getLoggedKeyspace + " are being DROPED !!")
    val metadata = session.getCluster.getMetadata();
    val tablesMetadata = metadata.getKeyspace(session.getLoggedKeyspace).getTables

    tablesMetadata.forEach(meta => println("Dropping: " + meta.getName))
    tablesMetadata.forEach(meta => session.execute("drop table " + meta.getName))
  }

  def createMigrationsTable(session: Session, appliedMigrationsTableName: String) = {
    session.execute(
      """
        | CREATE TABLE IF NOT EXISTS %s (
        |   authored_at timestamp,
        |   description text,
        |   applied_at timestamp,
        |   PRIMARY KEY (authored_at, description)
        |  )
      """.stripMargin.format(appliedMigrationsTableName)
    )
  }

}

class AppliedMigrations(applied: Seq[Migration]) {
  def length: Int = applied.length

  def apply(index: Int): Migration = applied.apply(index)

  def iterator: Iterator[Migration] = applied.iterator

  def authoredAfter(date: Date): Seq[Migration] = applied.filter(migration => migration.authoredAfter(date))

  def contains(other: Migration): Boolean = applied.contains(other)
}
