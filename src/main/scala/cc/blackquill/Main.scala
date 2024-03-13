package cc.blackquill

import cc.blackquill.alasasona._
import cats.effect._
import cats.implicits._
import org.http4s.implicits._
import org.http4s.ember.server._
import org.http4s._
import com.comcast.ip4s._
import smithy4s.http4s.SimpleRestJsonBuilder

class HandwritingImpl(using SQL) extends HandwritingService[IO]:
    def submitHandwritingSample(
        name: String,
        date: smithy4s.Timestamp,
        sample: smithy4s.Blob,
        community: String,
        link: String,
        notableGlyphs: List[String],
        writingImplement: String,
    ): IO[Unit] =
        IO.pure(???)

    def viewHandwritingSamples(
        next: Option[String],
        maxResults: Option[Int],
    ): IO[ViewHandwritingSamplesOutput] =
        IO.pure(???)

class Routes(using SQL):
    private val example: Resource[IO, HttpRoutes[IO]] =
        SimpleRestJsonBuilder.routes(HandwritingImpl()).resource

    private val docs: HttpRoutes[IO] =
        smithy4s.http4s.swagger.docs[IO](HandwritingService)

    val all: Resource[IO, HttpRoutes[IO]] = example.map(_ <+> docs)

object Main extends IOApp.Simple:
    def app =
        for
            given SQL <- SQL(Config(
                "localhost",
                5432,
                "alasa_sona_username",
                "alasa_sona_database",
                "alasa_sona_password",
            ), "live")
            _ <- Routes().all
                .flatMap { routes =>
                    EmberServerBuilder
                        .default[IO]
                        .withPort(port"9000")
                        .withHost(host"localhost")
                        .withHttpApp(routes.orNotFound)
                        .build
                }
        yield ()

    def run: IO[Unit] =
        app.use(_ => IO.never)
