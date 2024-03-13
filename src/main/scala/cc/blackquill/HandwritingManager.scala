package cc.blackquill

import cats.effect.IO
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import java.time.OffsetDateTime
import skunk.data.{Completion, Arr}
import java.util.UUID

case class Blob(
    data: Array[Byte]
):
    override def equals(x: Any): Boolean =
        x match
            case Blob(x) =>
                data.sameElements(x)
            case _ =>
                false

case class Sample(
    author: String,
    date: OffsetDateTime,
    sample: Blob,
    community: String,
    link: String,
    notableGlyphs: List[String],
    writingImplement: String,
    conversation: Option[UUID],
)

class HandwritingManager private(using sql: SQL):
    private val blob = bytea.imap(Blob(_))(_.data)

    def submitSample(
        author: String,
        date: OffsetDateTime,
        sample: Blob,
        community: String,
        link: String,
        notableGlyphs: List[String],
        writingImplement: String,
        conversation: Option[UUID],
    )(using Session[IO]): IO[(UUID, Completion)] =
        for
            id <- IO { UUID.randomUUID() }
            res <- sql.command(sql"""
            INSERT INTO HandwritingSamples (
                ID, Author, Date, Sample, Community, Link, NotableGlyphs, WritingImplement, Conversation
            ) VALUES (
                $uuid, $text, $timestamptz, $blob, $text, $text, $_text, $text, ${uuid.opt}
            );
            """, (id, author, date, sample, community, link, Arr(notableGlyphs: _*), writingImplement, conversation))
        yield (id, res)

    def getSample(id: UUID)(using Session[IO]): IO[Sample] =
        sql.queryOne(sql"""
        SELECT
            Author, Date, Sample, Community, Link, NotableGlyphs, WritingImplement, Conversation
        FROM HandwritingSamples
        WHERE ID = $uuid
        """, (text *: timestamptz *: blob *: text *: text *: _text.imap(_.flattenTo(List))(x => Arr(x: _*)) *: text *: uuid.opt).to[Sample], id)

object HandwritingManager:
    def apply(using sql: SQL, cm: ConversationManager) =
        for
            _ <- Migration(
                "Initial Handwriting Manager",
                List(
                    sql"""
                    CREATE TABLE HandwritingSamples (
                        ID UUID PRIMARY KEY,
                        Author TEXT NOT NULL,
                        Date TIMESTAMPTZ NOT NULL,
                        Sample BYTEA NOT NULL,
                        Community TEXT NOT NULL,
                        Link TEXT NOT NULL,
                        NotableGlyphs TEXT[] NOT NULL,
                        WritingImplement TEXT NOT NULL,
                        Conversation UUID REFERENCES Conversations(ID)
                    );
                    """,
                ),
                List(
                    sql"""
                    DROP TABLE HandwritingSamples;
                    """
                )
            ).perform
        yield new HandwritingManager()