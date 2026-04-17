package com.crowfff.orientdbtoy

import io.circe.parser.decode
import io.circe.syntax._
import munit.FunSuite

final class EntityRoutesSuite extends FunSuite {
  test("EntityRequest decoder parses id payload") {
    val decoded = decode[EntityRequest]("""{"id":100}""")
    assertEquals(decoded, Right(EntityRequest(100)))
  }

  test("EntityEvent encoder serializes add and remove events") {
    assertEquals((EntityEvent.Add(100): EntityEvent).asJson.noSpaces, "{\"add\":100}")
    assertEquals((EntityEvent.Remove(100): EntityEvent).asJson.noSpaces, "{\"remove\":100}")
  }
}
