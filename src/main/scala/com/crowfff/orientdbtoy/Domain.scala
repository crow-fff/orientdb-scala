package com.crowfff.orientdbtoy

import io.circe.{Encoder, Json}
import io.circe.generic.semiauto.deriveDecoder

final case class EntityRequest(id: Int)
object EntityRequest {
  implicit val decoder: io.circe.Decoder[EntityRequest] = deriveDecoder[EntityRequest]
}

sealed trait EntityEvent {
  def id: Int
}

object EntityEvent {
  final case class Add(id: Int) extends EntityEvent
  final case class Remove(id: Int) extends EntityEvent

  implicit val encoder: Encoder[EntityEvent] = Encoder.instance {
    case Add(id)    => Json.obj("add" -> Json.fromInt(id))
    case Remove(id) => Json.obj("remove" -> Json.fromInt(id))
  }
}
