package com.example.cpapi

import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse
import io.restassured.response.{Response, ResponseBodyExtractionOptions, ValidatableResponse, ValidatableResponseLogSpec}
import net.christophschubert.cp.testcontainers.{CPTestContainerFactory, ConfluentServerContainer}
import org.scalatest.freespec.AnyFreeSpecLike
import org.testcontainers.containers.Network

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class RestAssuredInitSpec extends AnyFreeSpecLike {

  val containerFactory = new CPTestContainerFactory(Network.newNetwork())
  val broker: ConfluentServerContainer = containerFactory.createConfluentServer()
  broker.start()

  RestAssured.port = broker.getMdsPort

  // TODO - try filters such as
  // 	assertThat().
  //		body("cars.car.findAll{it.country=='Japan'}.size()", equalTo(1));
  // from https://www.ontestautomation.com/using-jsonpath-and-xmlpath-in-rest-assured/

  "get full cluster data and use extractors" in {

    val check: ValidatableResponse = RestAssured.`given`().when().get("/kafka/v3/clusters/")
      .Then().statusCode(200)

    println("status:")
    println(check.extract().statusCode())

    println("headers:")
    val headers: Iterable[Header] = check.extract().headers().asScala
    headers foreach println

    println("cookies:")
    val cookies: mutable.Map[String, String] = check.extract().cookies().asScala
    cookies foreach println

    val body: ResponseBodyExtractionOptions = check.extract().body()
    println(body.asString())
  }

  "get full cluster data and log it" in {

    val check: ValidatableResponse = RestAssured.`given`().when().get("/kafka/v3/clusters/")
      .Then().statusCode(200)

    val log: ValidatableResponseLogSpec[ValidatableResponse, Response] =  check.and().log()
    println("all:")
    println(log.all()) // while not explicitly stated, all seems to be identical to everything.
    println("everything:")
    println(log.everything())
  }

  "get full cluster data and use jsonPath on extracted body" in {

     val check: ValidatableResponse = RestAssured.`given`().when().get("/kafka/v3/clusters/")
      .Then().statusCode(200)

    val body: ResponseBodyExtractionOptions = check.extract().body()

    val jsonPath = body.jsonPath()
    // leading forwards slashes nto allowed
    val kind = jsonPath.getString("kind")//.cluster_id")
    println(s"kind: $kind")

    val clusterId = jsonPath.getString("cluster_id")//.cluster_id")
    println(s"clusterId: $clusterId") // this is NULL and I am surprised

    val meta: util.Map[String, String] = jsonPath.getJsonObject("metadata")
    println(s"meta: $meta")

    val data: util.Map[String, String] = jsonPath.getJsonObject("data[0]")
    println(s"data: $data")

    val dataKind: String = jsonPath.getJsonObject("data[0].kind")
    println(s"dataKind: $dataKind")

    val dataCluster: String = jsonPath.getJsonObject("data[0].cluster_id")
    println(s"dataCluster: $dataCluster")

  }

  // comment refs

  // {
  //    "kind": "KafkaClusterList",
  //    "metadata": {
  //        "self": "http://localhost:49171/kafka/v3/clusters",
  //        "next": null
  //    },
  //    "data": [
  //        {
  //            "kind": "KafkaCluster",
  //            "metadata": {
  //                "self": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw",
  //                "resource_name": "crn:///kafka=KzYyn7D8STSBLSlYYxIiVw"
  //            },
  //            "cluster_id": "KzYyn7D8STSBLSlYYxIiVw",
  //            "controller": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/brokers/1"
  //            },
  //            "acls": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/acls"
  //            },
  //            "brokers": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/brokers"
  //            },
  //            "broker_configs": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/broker-configs"
  //            },
  //            "consumer_groups": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/consumer-groups"
  //            },
  //            "topics": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/topics"
  //            },
  //            "partition_reassignments": {
  //                "related": "http://localhost:49171/kafka/v3/clusters/KzYyn7D8STSBLSlYYxIiVw/topics/-/partitions/-/reassignment"
  //            }
  //        }
  //    ]
  //}

}
