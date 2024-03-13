namespace cc.blackquill.alasasona

use alloy#simpleRestJson

@simpleRestJson
service ConversationService {
  version: "1.0.0"
  operations: [CreateConversation, GetConversations]
}

@http(method: "POST", uri: "/conversations")
operation CreateConversation {
  input: CreateConversationInput
}

@input
structure CreateConversationInput {
  @required
  name: String

  @required
  date: Timestamp

  @required
  community: String

  @required
  link: String
}

@http(method: "GET", uri: "/conversations")
@paginated(inputToken: "next", outputToken: "next", pageSize: "maxResults", items: "conversations")
operation GetConversations {
  input: GetConversationsInput
  output: GetConversationsOutput
}

@input
structure GetConversationsInput {
  @httpQuery("next")
  next: String

  @httpQuery("maxResults")
  maxResults: Integer
}

@output
structure GetConversationsOutput {
  next: String
  conversations: ConversationList
}

list ConversationList {
  member: Conversation
}

structure Conversation {
  @required
  uuid: String

  @required
  name: String

  @required
  date: Timestamp

  @required
  community: String

  @required
  link: String
}
