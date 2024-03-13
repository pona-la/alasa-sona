namespace cc.blackquill.alasasona

use alloy#simpleRestJson

@simpleRestJson
service HandwritingService {
  version: "1.0.0"
  operations: [SubmitHandwritingSample, ViewHandwritingSamples]
}

@http(method: "POST", uri: "/handwriting/submit")
operation SubmitHandwritingSample {
  input: SubmitHandwritingSampleInput
}

@input
structure SubmitHandwritingSampleInput {
  @required
  name: String
  @required
  date: Timestamp
  @required
  sample: Blob
  @required
  community: String
  @required
  link: String
  @required
  notableGlyphs: NotableGlyphs
  @required
  writingImplement: String
}

list NotableGlyphs {
  member: String
}

@http(method: "GET", uri: "/handwriting/list")
@paginated(inputToken: "next", outputToken: "next", pageSize: "maxResults", items: "samples")
operation ViewHandwritingSamples {
  input: ViewHandwritingSamplesInput
  output: ViewHandwritingSamplesOutput
}

@input
structure ViewHandwritingSamplesInput {
  @httpQuery("next")
  next: String

  @httpQuery("maxResults")
  maxResults: Integer
}

@output
structure ViewHandwritingSamplesOutput {
  next: String
  samples: SampleList
}

list SampleList {
  member: Sample
}

structure Sample {
  @required
  uuid: String

  @required
  name: String

  @required
  date: Timestamp

  @required
  sample: Blob

  @required
  community: String

  @required
  link: String

  @required
  notableGlyphs: NotableGlyphs

  @required
  writingImplement: String
}
