package cc.blackquill

import cats.effect.{IO}
import munit.CatsEffectSuite
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class ConversationManagerSuite extends CatsEffectSuite:
    val sql: FunFixture[SQL] =
      FunFixture[SQL](TestDatabase.setup, TestDatabase.teardown)

    sql.test("you can add a conversation") { implicit sql =>
        for
            manager <- ConversationManager(using sql)
            time <- IO { OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS) }
            community = "ma pona pi toki pona"
            link = "https://discord.com/channels/301377942062366741/700124625618862181/1217577408006193213"
            result <- sql.session {
                manager.addConversation(time, community, link)
            }
            (id, _) = result
            _ <- sql.session {
                manager.getConversation(id).assertEquals(
                    Conversation(time, community, link)
                )
            }
        yield ()
    }