ThisBuild / scalaVersion := "2.13.16"

val Http4sVersion = "0.23.30"
val CatsEffectVersion = "3.5.7"
val CirceVersion = "0.14.12"
val OrientDbVersion = "3.2.29"

lazy val root = (project in file("."))
  .settings(
    name := "orientdb-scala",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "co.fs2" %% "fs2-core" % "3.12.2",
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "com.orientechnologies" % "orientdb-client" % OrientDbVersion,
      "com.orientechnologies" % "orientdb-core" % OrientDbVersion,
      "org.scalameta" %% "munit" % "1.1.1" % Test
    )
  )
