package persistence.db

import cats.effect.Sync
import cats.implicits.toFunctorOps
import config.DatabaseConfig
import org.flywaydb.core.Flyway

import javax.sql.DataSource

object FlywayMigrator {

  def migrate[F[_]: Sync](ds: DataSource): F[Unit] = Sync[F].delay {
    Flyway
      .configure()
      .dataSource(ds)
      .load()
      .migrate()
  }.void

  def execute[F[_]: Sync](config: DatabaseConfig): F[Unit] = {
    val ds = Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .load()
      .getConfiguration
      .getDataSource

    migrate(ds)
  }
}
