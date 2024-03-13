package cc.blackquill

import cats.effect.IO
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import java.time.OffsetDateTime
import skunk.data.Completion
import java.util.UUID

case class Conversation(
    date: OffsetDateTime,
    community: String,
    link: String,
)

class ConversationManager private(using sql: SQL):
    def addConversation(
        date: OffsetDateTime,
        community: String,
        link: String,
    )(using Session[IO]): IO[(UUID, Completion)] =
        for
            id <- IO { UUID.randomUUID() }
            res <- sql.command(sql"""
            INSERT INTO Conversations (
                ID, Date, Community, Link
            ) VALUES (
                $uuid, $timestamptz, $text, $text
            );
            """, (id, date, community, link))
        yield (id, res)

    def getConversation(id: UUID)(using Session[IO]): IO[Conversation] =
        sql.queryOne(sql"""
        SELECT Date, Community, Link FROM Conversations WHERE ID = $uuid
        """, (timestamptz *: text *: text).to[Conversation], id)

object ConversationManager:
    def apply(using sql: SQL) =
        for
            _ <- Migration(
                "Initial Conversation Manager",
                List(
                    sql"""
                    CREATE TABLE Conversations (
                        ID UUID PRIMARY KEY,
                        Date TIMESTAMPTZ NOT NULL,
                        Community TEXT NOT NULL,
                        Link TEXT NOT NULL
                    );
                    """,
                ),
                List(
                    sql"""
                    DROP TABLE Conversations;
                    """
                )
            ).perform
        yield new ConversationManager()