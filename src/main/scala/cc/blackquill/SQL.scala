package cc.blackquill

import cats.effect.{IO, Resource}
import skunk.*
import skunk.implicits.*
import skunk.data.Completion
import natchez.Trace.Implicits.noop

object SQL:
    def apply(resource: Resource[IO, Session[IO]], name: String): IO[SQL] =
        for
            _ <- resource.use { session =>
                session.execute(sql"""
                CREATE TABLE IF NOT EXISTS _Migrations (
                    Name TEXT PRIMARY KEY
                );
                """.command)
            }
        yield new SQL(resource, name)
    def apply(config: Config, name: String): Resource[IO, SQL] =
        for
            pool <- Session.pooled[IO](
                host = config.host,
                port = config.port,
                user = config.user,
                database = config.database,
                max = 5,
                password = Some(config.password),
            )
            sql <- Resource.liftK(SQL(pool, name))
        yield sql

class SQL private(resource: Resource[IO, Session[IO]], val name: String):
    def session[T](fn: Session[IO] ?=> IO[T]): IO[T] =
        resource.use { session =>
            fn(using session)
        }
    def command[T](fragment: Fragment[T], parameters: T)(using s: Session[IO]): IO[Completion] =
        for
            res <- s.execute(fragment.command)(parameters)
        yield res
    def queryList[In, Out](fragment: Fragment[In], decoder: Decoder[Out], parameters: In)(using s: Session[IO]): IO[List[Out]] =
        for
            res <- s.stream(fragment.query(decoder))(parameters, 64).compile.toList
        yield res
    def queryOne[In, Out](fragment: Fragment[In], decoder: Decoder[Out], parameters: In)(using s: Session[IO]): IO[Out] =
        for
            res <- s.unique(fragment.query(decoder))(parameters)
        yield res
