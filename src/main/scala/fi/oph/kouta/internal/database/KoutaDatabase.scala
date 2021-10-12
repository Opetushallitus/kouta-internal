package fi.oph.kouta.internal.database

import java.util.concurrent.TimeUnit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.oph.kouta.internal.{KoutaConfigurationFactory, KoutaDatabaseConfiguration}
import fi.vm.sade.utils.slf4j.Logging
import org.apache.commons.lang3.builder.ToStringBuilder
import org.flywaydb.core.Flyway
import org.postgresql.util.PSQLException
import java.util.ConcurrentModificationException
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal
import com.github.takezoe.slick.blocking.BlockingH2Driver.blockingApi._

class KoutaDatabase(settings: KoutaDatabaseConfiguration) extends Logging {
  val db = initDb()

  def runBlocking[R](operations: DBIOAction[R, NoStream, Effect]): R = {
    db.withTransaction { implicit transactional =>
      operations.run
    }
  }

  def runBlockingTransactionally[R](
      operations: DBIOAction[R, NoStream, Effect],
      description: String,
      wait: Duration = Duration(1, TimeUnit.SECONDS),
      retries: Int = 1
  ): Either[Throwable, R] = {
    val SERIALIZATION_VIOLATION = "40001"
    try {
      Right(runBlocking(operations))
    } catch {
      case e: PSQLException if e.getSQLState == SERIALIZATION_VIOLATION =>
        if (retries > 0) {
          logger.warn(s"$description failed because of an concurrent action, retrying after $wait")
          Thread.sleep(wait.toMillis)
          runBlockingTransactionally(operations, description, wait + wait, retries - 1)
        } else {
          Left(new ConcurrentModificationException(s"$description failed because of an concurrent action.", e))
        }
      case NonFatal(e) => Left(e)
    }
  }

  def destroy(): Unit = {
    db.close()
  }

  private def initDb() = {
    migrate()

    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(settings.url)
    hikariConfig.setUsername(settings.username)
    hikariConfig.setPassword(settings.password)
    val maxPoolSize = settings.maxConnections.getOrElse(10)
    hikariConfig.setMaximumPoolSize(maxPoolSize)
    settings.minConnections.foreach(hikariConfig.setMinimumIdle)
    settings.registerMbeans.foreach(hikariConfig.setRegisterMbeans)
    //settings.initializationFailTimeout.foreach(hikariConfig.setI)
    //hikariConfig.setLeakDetectionThreshold(settings.leakDetectionThresholdMillis.getOrElse(settings.getMaxLifetime))
    val executor = AsyncExecutor("koutainternal", maxPoolSize, 1000)

    val className    = classOf[HikariConfig].getSimpleName
    val executorName = ToStringBuilder.reflectionToString(executor)
    val hikariString = ToStringBuilder
      .reflectionToString(hikariConfig)
      .replaceAll("password=.*?,", "password=<HIDDEN>,")

    logger.info(s"Configured Hikari with $className $hikariString and executor $executorName")

    Database.forDataSource(new HikariDataSource(hikariConfig), maxConnections = Some(maxPoolSize))
  }

  private def migrate(): Unit = {
    val flyway = new Flyway()
    flyway.setDataSource(settings.url, settings.username, settings.password)
    flyway.setLocations(
      "flyway/migration"
    ) // Vältetään defaulttia, koska se törmää testeissä kouta-backendin migraatioihin
    flyway.migrate()
  }
}

object KoutaDatabase extends KoutaDatabase(KoutaConfigurationFactory.configuration.databaseConfiguration)
