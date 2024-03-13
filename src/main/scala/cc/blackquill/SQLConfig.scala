package cc.blackquill

import org.spongepowered.configurate.CommentedConfigurationNode
import scala.util.Try

case class Config(
    host: String,
    port: Int,
    user: String,
    database: String,
    password: String,
)

enum ConfigError:
    case missingHost
    case missingPort
    case missingUser
    case missingDatabase
    case missingPassword
    case error(of: Throwable)

object Config:
    private def get[V](
        config: CommentedConfigurationNode,
        node: String,
        klass: Class[V],
        left: ConfigError,
    ): Either[ConfigError, V] =
        for {
            valueOpt <- Try(Option(config.node(node).get(klass))).toEither.left
                .map(ConfigError.error.apply)
            value <- valueOpt.toRight(left)
        } yield value

    def from(config: CommentedConfigurationNode): Either[ConfigError, Config] =
        import ConfigError.*

        for {
            host <- get(config, "host", classOf[String], missingHost)
            port <- get(config, "port", classOf[Integer], missingPort)
            user <- get(config, "user", classOf[String], missingUser)
            data <- get(config, "database", classOf[String], missingDatabase)
            pass <- get(config, "password", classOf[String], missingPassword)
        } yield Config(host, port, user, data, pass)
