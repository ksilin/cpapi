package com.example.cpapi

import io.restassured.RestAssured
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
import io.restassured.response.{Response, ValidatableResponse, ValidatableResponseLogSpec}
import io.restassured.specification.RequestSpecification
import net.christophschubert.cp.testcontainers.{CPTestContainerFactory, ConfluentServerContainer}
import org.hamcrest.Matchers._
import org.scalatest.freespec.AnyFreeSpecLike
import org.testcontainers.containers.Network

import java.util.Base64
import scala.jdk.CollectionConverters._

class MdsDockerSpec extends AnyFreeSpecLike {

  // assumes a docker-compose running

  val host = "localhost"
  val port = 8090

  val alice = "alice"
  val aliceSecret = "alice-secret"

  val mdsUser = "mds"
  val mdsSecret = "mds-secret"

  var clusterId: String = _

  // full cluster data will contain references to:
  // controller
  // acls
  // brokers
  // broker_configs
  // consumer_groups
  // topics
  // partition_reassignments

  var token: String = _ //"eyJhbGciOiJSUzI1NiIsImtpZCI6bnVsbH0.eyJqdGkiOiIwM1dURDVPN1llREp3Zi1rLUczTElRIiwiaXNzIjoiQ29uZmx1ZW50Iiwic3ViIjoiYWxpY2UiLCJleHAiOjE2NTUzMTcwNzksImlhdCI6MTY1NTI5NTQ3OSwibmJmIjoxNjU1Mjk1NDE5LCJhenAiOiJhbGljZSIsImF1dGhfdGltZSI6MTY1NTI5NTQ3OX0.GYOMX78MUUHhRUDPjgGh0p05Mh6_tU9axA74LTPFyyokW6rB9STzE8m64MA9yPtoIgZoCU1jTC7FLKpCCJmNmxXHQdEsXtMmOrPuu2s3bS229opZZqiQDEfM-eEYrGeEF-Q_4qsDa_FxWQWP3-CzlcvxsgSYxSU9zUuRYVw99L5wZvJSkOa2_Jx5OHV4ayqHChjAkZFlDa5W-lMXKEdT2wCQCqLrtXyq1wvqACDVHXKEGuhZLLUVInn3QHNGUZV0ssa2-XbM392h55DLwaPL9lt2JslQ4h6VrDYnMaxdAPyVuuFTjcFud7f6wE8HsKYoUKTf61MPXVnhmluNfCqxyA"

  RestAssured.port = port
  RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

  // https://docs.confluent.io/platform/current/kafka-rest/api.html#cluster-v3
  "can get a bearer token" in {
    // .auth().basic(alice, aliceSecret) - does not work for some reason, not seing the header in logs
    val rq: RequestSpecification = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret)
    rq.log().all()

    val res: Response = rq.get("/security/1.0/authenticate")
    println(res.body().prettyPrint())

    token = res.body().jsonPath().getString("auth_token")
    println(s"acquired token: $token")

      //.when().get("/security/1.0/authenticate")
      //.Then().statusCode(200)
    //rq.log().all()
  }

  "can get cluster id" in {
    // header("Authorization", s"Basic ${encode(alice, aliceSecret)}")
    val rq = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret).when().get("/security/1.0/metadataClusterId")
      .Then().statusCode(200)
    clusterId = rq.extract().body().asString()
    println(s"clusterId: $clusterId")
  }

  "can get roles" in {
    val rq = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret).when().get("/security/1.0/roles")
      .Then().statusCode(200)
    rq.log().all()
  }

  "can get roles - using bearer token" in {
    val rq = RestAssured.`given`().header("Authorization", s"Bearer ${token}").when().get("/security/1.0/roles")
      .Then().statusCode(200)
    rq.log().all()
  }


  // cluster registry

  "can get clusters as user" in {
    val rq = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret).when().get("/security/1.0/registry/clusters")
      .Then().statusCode(200)
    rq.log().all()
  }

  "can get clusters as superUser" in {
    val rq = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret).when().get("/security/1.0/registry/clusters")
      .Then().statusCode(200)
    rq.log().all()
  }

  // can register real cluster
  // can register fantasy cluster

  "can authorize" in {

    // TODO - needs cluster ID and resource to authorize for

    val rq = RestAssured.`given`().auth().basic(alice, aliceSecret).when().get("/security/1.0/authorize")
      .Then().statusCode(200)

    rq.log().all()
  }

}
