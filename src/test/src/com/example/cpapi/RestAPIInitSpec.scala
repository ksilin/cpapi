package com.example.cpapi

import io.restassured.RestAssured
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
import io.restassured.response.{Response, ValidatableResponse, ValidatableResponseLogSpec}
import net.christophschubert.cp.testcontainers.{CPTestContainerFactory, ConfluentServerContainer}
import org.hamcrest.Matchers._
import org.scalatest.freespec.AnyFreeSpecLike
import org.testcontainers.containers.Network

import scala.jdk.CollectionConverters._

class RestAPIInitSpec extends AnyFreeSpecLike {

  val alice = "alice"
  val aliceSecret = "alice-secret"

  val containerFactory = new CPTestContainerFactory(Network.newNetwork())
  //also available: createKafka()
  val broker: ConfluentServerContainer = containerFactory.createConfluentServer()
  val ldap = containerFactory.createLdap(Set(alice).asJava)
  ldap.start()
  //broker.enableRbac()
  broker.start()
  // startAll(ldap, broker)

  RestAssured.port = broker.getMdsPort

  val mdsWrapper = new MdsRestWrapper(broker.getMdsPort, alice, aliceSecret)

  // full cluster data will contain references to:
  // controller
  // acls
  // brokers
  // broker_configs
  // consumer_groups
  // topics
  // partition_reassignments

  // https://docs.confluent.io/platform/current/kafka-rest/api.html#cluster-v3
  "get full cluster data" in {
    RestAssured.`given`().when().get("/kafka/v3/clusters/")
      .Then().statusCode(200)
      .and().body("data[0].cluster_id", notNullValue())
  }

  "must retrieve cluster id" in {
    val check: ValidatableResponse = RestAssured.`given`().when().get("/v1/metadata/id")
      .Then().statusCode(200)
    .and().body("id", notNullValue())
    .and().body("scope.clusters.kafka-cluster", notNullValue())
  }

  "must retrieve cluster id via RBAC endpoint" in {
    val check: ValidatableResponse = RestAssured.`given`().when().get("/security/1.0/metadataClusterId")
      .Then().statusCode(404)
    val log: ValidatableResponseLogSpec[ValidatableResponse, Response] = check.and().log()
    println(log.all())
  }

}
