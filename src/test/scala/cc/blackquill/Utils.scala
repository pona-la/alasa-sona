package cc.blackquill

import cats.effect.{Resource, IO}
import skunk.*
import skunk.implicits.*
import skunk.util.Origin
import natchez.Trace.Implicits.noop
import cats.effect.unsafe.implicits.global

object TestDatabase:
    def setup(opts: munit.TestOptions): SQL =
        val session: Resource[IO, Session[IO]] = Session.single(
            host = "localhost",
            port = 5432,
            user = "civcubed",
            database = "civcubed",
            password = Some("shitty password"),
        )
        val cleanName =
            opts.name.replace(" ", "").replace("-", "").replace("'", "")
        val nameFragment =
            Fragment(List(Left(cleanName)), Void.codec, Origin.unknown)
        session
            .use { s =>
                s.execute(sql"""
            CREATE DATABASE $nameFragment;
            """.command)
            }
            .unsafeRunSync()
        val testSession: Resource[IO, Session[IO]] = Session.single(
            host = "localhost",
            port = 5432,
            user = "civcubed",
            database = cleanName,
            password = Some("shitty password"),
        )
        testSession
            .use { s =>
                s.execute(sql"""
            CREATE EXTENSION postgis;
            """.command)
            }
            .unsafeRunSync()
        SQL(testSession, cleanName).unsafeRunSync()

    def teardown(s: SQL): Unit =
        val session: Resource[IO, Session[IO]] = Session.single(
            host = "localhost",
            port = 5432,
            user = "civcubed",
            database = "civcubed",
            password = Some("shitty password"),
        )
        val nameFragment =
            Fragment(List(Left(s.name)), Void.codec, Origin.unknown)
        session
            .use { s =>
                s.execute(sql"""
            DROP DATABASE $nameFragment;
            """.command)
            }
            .unsafeRunSync()
        ()
