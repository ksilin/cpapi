# cpapi #

Welcome to cpapi! Playing with different on-prem APIs.

## TODOs

### custom docker images

using different settings from 

https://docs.confluent.io/platform/current/kafka-rest/production-deployment/confluent-server/config.html

* e.g. adding debug info to error responses: 

* TLS/HTTPS

https://docs.confluent.io/platform/current/kafka-rest/production-deployment/confluent-server/config.html#configuration-options-for-https

* security

https://docs.confluent.io/platform/current/kafka-rest/production-deployment/confluent-server/security.html

BASIC or mTLS for client -> API

### generate POJOs / case classes from OpenAPI spec



### RBAC

https://github.com/testcontainers-all-things-kafka/cp-testcontainers/blob/master/src/intTest/java/net/christophschubert/cp/testcontainers/CPServerTest.java

### Kafka REST API

https://docs.confluent.io/platform/current/kafka-rest/api.html

## v2

listing brokers, topics, partitions, consumers

## v3

* fetching cluster info

* configs CRUD

* ACL CRUD

* consumer groups

* partitions

* topics CRUD

* cluster linking

* broker

* partition

* replicas

* records

* balancer status

* brokerTask

* BrokerReplicaExclusion

* RemoveBrokerTask

* ReplicaStatus





## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.

## License ##

This code is open source software licensed under the
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
