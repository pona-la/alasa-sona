package cc.blackquill

import cats.effect.{IO}
import munit.CatsEffectSuite
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.nio.charset.StandardCharsets

val HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII)
def bytesToHex(array: Array[Byte]): String =
    val hexChars = new Array[Byte](array.size * 2)
    for j <- 0 until array.length do
        val v = array(j)
        hexChars(j*2) = HEX_ARRAY(v >>> 4)
        hexChars(j*2 + 1) = HEX_ARRAY(v & 0x0F)
    String(hexChars, StandardCharsets.UTF_8)

class HandwritingManagerSuite extends CatsEffectSuite:
    val sql: FunFixture[SQL] =
      FunFixture[SQL](TestDatabase.setup, TestDatabase.teardown)

    sql.test("you can add a handwriting sample") { implicit sql =>
        for
            conversations <- ConversationManager(using sql)
            manager <- HandwritingManager(using sql, conversations)
            time <- IO { OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS) }
            author = "akesi"
            community = "ma pona pi toki pona"
            sample = Blob(Array[Byte](0x01, 0x02, 0x03))
            other = Blob(Array[Byte](0x01, 0x02, 0x03))
            _ <- IO.pure(sample == other).assert
            link = "https://discord.com/channels/301377942062366741/700124625618862181/1217577408006193213"
            notable = List("weird akesi", "normal akesi")
            tool = "pencil attached to a rat"
            result <- sql.session {
                manager.submitSample(
                    author = author,
                    date = time,
                    sample = sample,
                    community = community,
                    link = link,
                    notableGlyphs = notable,
                    writingImplement = "tool",
                    conversation = None,
                )
            }
            (id, _) = result
            _ <- sql.session {
                manager.getSample(id).assertEquals(
                    Sample(
                        author = author,
                        date = time,
                        sample = sample,
                        community = community,
                        link = link,
                        notableGlyphs = notable,
                        writingImplement = "tool",
                        conversation = None,
                    )
                )
            }
        yield ()
    }
