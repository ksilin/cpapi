package com.example.cpapi

import io.restassured.RestAssured
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.scalatest.freespec.AnyFreeSpecLike

class MdsDockerRbacSpec extends AnyFreeSpecLike {

  // assumes a docker-compose running, see resources folder for details

  // https://docs.confluent.io/platform/current/security/rbac/rbac-config-using-rest-api.html#configure-rbac-using-the-rest-api

  val host = "localhost"
  val port = 8090

  val alice = "alice"
  val aliceSecret = "alice-secret"

  //superuser - see login.properties
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

  // RBAC UI - cluster visibility
  "can get cluster visibility" in {
    val rq = RestAssured.`given`().auth.preemptive().basic(mdsUser, mdsSecret)
      .when().get(s"/security/1.0/lookup/managed/clusters/principal/User:$mdsUser?cluster-type=kafka-cluster")
      .Then().statusCode(200)
    rq.log().all()
  }

}
