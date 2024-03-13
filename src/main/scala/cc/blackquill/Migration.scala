package cc.blackquill

import cats.effect.{IO}
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import skunk.data.Completion
import cats.syntax.all.*

private val countQuery: Query[String, Long] =
    sql"""
    SELECT COUNT(*) FROM _Migrations WHERE NAME = $text
    """.query(int8)

private val insertCommand: Command[String] =
    sql"""
    INSERT INTO _Migrations (Name) VALUES ($text)
    """.command

case class Migration(
    name: String,
    apply: List[Fragment[Void]],
    reverse: List[Fragment[Void]],
):
    def perform(using sql: SQL): IO[Unit] =
        sql.session {
            val s = summon[Session[IO]]
            s.transaction.use { tx =>
                for
                    count <- s.unique(countQuery)(name)
                    _ <-
                        if count == 0 then
                            for
                                _ <- apply.map(_.command).traverse(s.execute)
                                _ <- s.execute(insertCommand)(name)
                            yield ()
                        else
                            IO.unit
                yield ()
            }
        }
