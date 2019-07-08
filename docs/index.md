Siddhi IO JMS
===================

  [![Jenkins Build Status](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-jms/badge/icon)](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-jms/)
  [![GitHub Release](https://img.shields.io/github/release/siddhi-io/siddhi-io-jms.svg)](https://github.com/siddhi-io/siddhi-io-jms/releases)
  [![GitHub Release Date](https://img.shields.io/github/release-date/siddhi-io/siddhi-io-jms.svg)](https://github.com/siddhi-io/siddhi-io-jms/releases)
  [![GitHub Open Issues](https://img.shields.io/github/issues-raw/siddhi-io/siddhi-io-jms.svg)](https://github.com/siddhi-io/siddhi-io-jms/issues)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/siddhi-io/siddhi-io-jms.svg)](https://github.com/siddhi-io/siddhi-io-jms/commits/master)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The **siddhi-io-jms extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that receives and publishes events via Java Message Service (JMS), supporting Message brokers such as ActiveMQ.

For information on <a target="_blank" href="https://siddhi.io/">Siddhi</a> and it's features refer <a target="_blank" href="https://siddhi.io/redirect/docs.html">Siddhi Documentation</a>. 

## Download

* Versions 2.x and above with group id `io.siddhi.extension.*` from <a target="_blank" href="https://mvnrepository.com/artifact/io.siddhi.extension.io.jms/siddhi-io-jms/">here</a>.
* Versions 1.x and lower with group id `org.wso2.extension.siddhi.*` from <a target="_blank" href="https://mvnrepository.com/artifact/org.wso2.extension.siddhi.io.jms/siddhi-io-jms">here</a>.

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-jms/api/2.0.2">2.0.2</a>.

## Features

* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-jms/api/2.0.2/#jms-sink">jms</a> *<a target="_blank" href="https://siddhi.io/en/v5.0/docs/query-guide/#sink">(Sink)</a>*<br><div style="padding-left: 1em;"><p>JMS Sink allows users to subscribe to a JMS broker and publish JMS messages.</p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-jms/api/2.0.2/#jms-source">jms</a> *<a target="_blank" href="https://siddhi.io/en/v5.0/docs/query-guide/#source">(Source)</a>*<br><div style="padding-left: 1em;"><p>JMS Source allows users to subscribe to a JMS broker and receive JMS messages. It has the ability to receive Map messages and Text messages.</p></div>

## Dependencies 

For working with ActiveMQ. 

* Download the activemq client JAR from maven central.

    - [activemq-client-5.x.x.jar](http://central.maven.org/maven2/org/apache/activemq/activemq-client/5.9.0/activemq-client-5.9.0.jar).
    
* Following JARs are needed from `<ActiveMQ_HOME>/libs` directory

    - hawtbuf-1.9.jar
    - geronimo-j2ee-management_1.1_spec-1.0.1.jar
    - geronimo-jms_1.1_spec-1.1.1.jar

## Installation

For installing this extension on various siddhi execution environments refer Siddhi documentation section on <a target="_blank" href="https://siddhi.io/redirect/add-extensions.html">adding extensions</a>.

## Support and Contribution

* We encourage users to ask questions and get support via <a target="_blank" href="https://stackoverflow.com/questions/tagged/siddhi">StackOverflow</a>, make sure to add the `siddhi` tag to the issue for better response.

* If you find any issues related to the extension please report them on <a target="_blank" href="https://github.com/siddhi-io/siddhi-execution-string/issues">the issue tracker</a>.

* For production support and other contribution related information refer <a target="_blank" href="https://siddhi.io/community/">Siddhi Community</a> documentation.
