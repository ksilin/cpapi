package com.example.cpapi

import io.restassured.RestAssured
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
import io.restassured.response.{Response, ValidatableResponse, ValidatableResponseLogSpec}
import io.restassured.specification.RequestSpecification
import org.scalatest.freespec.AnyFreeSpecLike

import scala.jdk.CollectionConverters._

class MdsDockerSpec extends AnyFreeSpecLike {

  // assumes a docker-compose running

  val host = "localhost"
  val port = 8090

  val alice = "alice"
  val aliceSecret = "alice-secret"

  val mdsUser = "mds"
  val mdsSecret = "mds-secret"

  // need this data for further requests
  var clusterId: String = _
  var token: String = _

  RestAssured.port = port
  RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

  // https://docs.confluent.io/platform/current/kafka-rest/api.html#cluster-v3
  "can get a bearer token" in {
    val rq: RequestSpecification = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret)
    val res: Response = rq.get("/security/1.0/authenticate")
    token = res.body().jsonPath().getString("auth_token")
    println(s"acquired token: $token")
    val TTL = res.body().jsonPath().getString("expires_in")
    val ttlInt = Integer.parseInt(TTL)
    println(s"valid for $TTL sec = ${ttlInt/60} min = ${ttlInt/3600} hrs")
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

  "can register clusters" in {

    val body =
      """
        |[{
        |"clusterName": "exampleCluster",
        | "scope": {
        |   "clusters": {
        |     "kafka-cluster": "myRegisteredCluster"
        |   }
        |  },
        | "hosts": [
        |   {
        |     "host": "localhost",
        |     "port": 9092
        |   }
        | ],
        | "protocol": "SASL_PLAINTEXT"
        |}
        |]
        |""".stripMargin

    val rq = RestAssured.`given`().auth.preemptive().basic(mdsUser, mdsSecret)//.basic(alice, aliceSecret)
      .when().contentType("application/json").body(body)
      .post("/security/1.0/registry/clusters")
      .Then().statusCode(204)

    rq.log().all()
  }

  "can NOT get clusters as user" in {
    val rq = RestAssured.`given`().auth.preemptive().basic(alice, aliceSecret).when().get("/security/1.0/registry/clusters")
      .Then().statusCode(200)

    // TODO - check for empty array

    rq.log().all()
  }

  "can get clusters as superUser" in {
    val rq = RestAssured.`given`().auth.preemptive().basic(mdsUser, mdsSecret).when().get("/security/1.0/registry/clusters")
      .Then().statusCode(200)
    rq.log().all()
  }


  // can register fantasy cluster

  "can authorize" in {

    // TODO - needs cluster ID and resource to authorize for

    val rq = RestAssured.`given`().auth().preemptive().basic(alice, aliceSecret).when().get("/security/1.0/authorize")
      .Then().statusCode(200)

    rq.log().all()
  }

}
