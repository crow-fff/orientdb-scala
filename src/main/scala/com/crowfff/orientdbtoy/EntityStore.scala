package com.crowfff.orientdbtoy

import fs2.Stream

trait EntityStore[F[_]] {
  def add(id: Int): F[Unit]
  def remove(id: Int): F[Unit]
  def events: Stream[F, EntityEvent]
}
