package fi.oph.kouta.internal.database

import java.util.concurrent.TimeUnit

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.oph.kouta.internal.{KoutaConfigurationFactory, KoutaDatabaseConfiguration}
import fi.vm.sade.utils.slf4j.Logging
import org.apache.commons.lang3.builder.ToStringBuilder
import org.flywaydb.core.Flyway
import org.postgresql.util.PSQLException
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.TransactionIsolation.Serializable
import java.util.ConcurrentModificationException

import slick.jdbc.TransactionIsolation

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try
import scala.util.control.NonFatal

class KoutaDatabase(settings: KoutaDatabaseConfiguration) extends Logging {
  val db = initDb()

  def runBlocking[R](operations: DBIO[R], timeout: Duration = Duration(10, TimeUnit.MINUTES)): R = {
    Await.result(
      db.run(operations.withStatementParameters(statementInit = st => st.setQueryTimeout(timeout.toSeconds.toInt))),
      timeout + Duration(1, TimeUnit.SECONDS)
    )
  }

  def runBlockingTransactionallyKB[R](
      operations: DBIO[R],
      timeout: Duration = Duration(20, TimeUnit.SECONDS),
      isolation: TransactionIsolation = Serializable
  ): Try[R] = {
    Try(runBlocking(operations.transactionally.withTransactionIsolation(isolation), timeout))
  }

  def runBlockingTransactionally[R](
      operations: DBIO[R],
      timeout: Duration = Duration(20, TimeUnit.SECONDS),
      description: String,
      wait: Duration = Duration(1, TimeUnit.SECONDS),
      retries: Int = 1
  ): Either[Throwable, R] = {
    val SERIALIZATION_VIOLATION = "40001"
    try {
      Right(runBlocking(operations.transactionally.withTransactionIsolation(Serializable), timeout))
    } catch {
      case e: PSQLException if e.getSQLState == SERIALIZATION_VIOLATION =>
        if (retries > 0) {
          logger.warn(s"$description failed because of an concurrent action, retrying after $wait")
          Thread.sleep(wait.toMillis)
          runBlockingTransactionally(operations, timeout, description, wait + wait, retries - 1)
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
    val numThreads = 10
    val queueSize  = 1000
    logger.info(s"Initdb with hikari max pool size $maxPoolSize, numthreads $numThreads and queue size $queueSize")
    //val executor     = AsyncExecutor("koutainternal", numThreads, queueSize)
    val executor = AsyncExecutor(
      name = "koutainternal",
      minThreads = maxPoolSize,
      maxThreads = maxPoolSize,
      queueSize = 0,
      maxConnections = Integer.MAX_VALUE,
      registerMbeans = true
    )
    val className    = classOf[HikariConfig].getSimpleName
    val executorName = ToStringBuilder.reflectionToString(executor)
    val hikariString = ToStringBuilder
      .reflectionToString(hikariConfig)
      .replaceAll("password=.*?,", "password=<HIDDEN>,")

    logger.info(s"Configured Hikari with $className $hikariString and executor $executorName")

    Database.forDataSource(new HikariDataSource(hikariConfig), maxConnections = Some(maxPoolSize), executor)
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
