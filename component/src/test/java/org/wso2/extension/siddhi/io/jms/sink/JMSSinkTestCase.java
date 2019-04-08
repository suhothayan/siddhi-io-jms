/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.extension.siddhi.io.jms.sink;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.jms.sink.util.JMSClient;
import org.wso2.extension.siddhi.io.jms.sink.util.ResultContainer;

/**
 * Class implementing the Test cases for JMS Sink.
 */
public class JMSSinkTestCase {

    /**
     * Test for configure the JMS Sink publish the message to an ActiveMQ topic.
     */
    @Test
    public void jmsTopicPublishTest() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        ResultContainer resultContainer = new ResultContainer(2);
        JMSClient client = new JMSClient("activemq", "DAS_JMS_OUTPUT_TEST", "", resultContainer);
        try {
            //init
            Thread listenerThread = new Thread(client);
            listenerThread.start();
            Thread.sleep(1000);
            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', @map(type='xml'), "
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "provider.url='vm://localhost',"
                    + "destination='DAS_JMS_OUTPUT_TEST',"
                    + "connection.factory.jndi.name='TopicConnectionFactory',"
                    + "connection.factory.type='topic',"
                    + "subscription.durable='true' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
            InputHandler inputStream = executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
            inputStream.send(new Object[]{"JAMES", 23, "USA"});
            inputStream.send(new Object[]{"MIKE", 23, "Germany"});
            Assert.assertTrue(resultContainer.assertMessageContent("JAMES"));
            Assert.assertTrue(resultContainer.assertMessageContent("MIKE"));
        } finally {
            client.shutdown();
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    /**
     * Test for configure the JMS Sink publish message to an ActiveMQ queue.
     */
    @Test(dependsOnMethods = "jmsTopicPublishTest")
    public void jmsTopicPublishTest1() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        ResultContainer resultContainer = new ResultContainer(2);
        JMSClient client = new JMSClient("activemq", "", "DAS_JMS_OUTPUT_TEST", resultContainer);
        try {
            //init
            Thread listenerThread = new Thread(client);
            listenerThread.start();
            Thread.sleep(1000);
            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', @map(type='json'), "
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "provider.url='vm://localhost',"
                    + "destination='DAS_JMS_OUTPUT_TEST' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
            InputHandler inputStream = executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
            inputStream.send(new Object[]{"JAMES", 23, "USA"});
            inputStream.send(new Object[]{"MIKE", 23, "Germany"});
            Assert.assertTrue(resultContainer.assertMessageContent("JAMES"));
            Assert.assertTrue(resultContainer.assertMessageContent("MIKE"));
        } finally {
            client.shutdown();
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    @Test(dependsOnMethods = "jmsTopicPublishTest1")
    public void jmsTopicPublishTest4() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        ResultContainer resultContainer = new ResultContainer(2, 5);
        try {

            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', @map(type='xml'),"
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "provider.url='vm://localhost',"
                    + "destination='DAS_JMS_OUTPUT_TEST' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
            InputHandler inputStream = executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
            inputStream.send(new Object[]{"JAMES", 23, "USA"});
            inputStream.send(new Object[]{"MIKE", 23, "Germany"});
            Assert.assertFalse(resultContainer.assertMessageContent("JAMES"));
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void jmsTopicPublishTest2() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        try {

            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', @map(type='json'), "
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "destination='DAS_JMS_OUTPUT_TEST' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void jmsTopicPublishTest3() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        try {
            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', "
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "provider.url='vm://localhost',"
                    + "destination='DAS_JMS_OUTPUT_TEST' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    /**
     * Test for configure the JMS Sink publish message to an ActiveMQ queue, when payload is an instanceof Binary
     */
    @Test(dependsOnMethods = "jmsTopicPublishTest3")
    public void jmsTopicPublishTest5() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        ResultContainer resultContainer = new ResultContainer(2);
        JMSClient client = new JMSClient("activemq", "", "DAS_JMS_OUTPUT_TEST", resultContainer);
        try {
            //init
            Thread listenerThread = new Thread(client);
            listenerThread.start();
            Thread.sleep(1000);
            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', @map(type='binary'), "
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "provider.url='vm://localhost',"
                    + "destination='DAS_JMS_OUTPUT_TEST' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
            InputHandler inputStream = executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
            Thread.sleep(100);
            inputStream.send(new Object[]{"JAMES", 23, "USA"});
            inputStream.send(new Object[]{"MIKE", 23, "Germany"});

            Assert.assertTrue(resultContainer.assertMessageContent("JAMES"));
            Assert.assertTrue(resultContainer.assertMessageContent("MIKE"));
        } finally {
            client.shutdown();
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    /**
     * Test for configure the JMS Sink publish message to an ActiveMQ queue,when payload is an instanceof Map.
     */
    @Test(dependsOnMethods = "jmsTopicPublishTest")
    public void jmsTopicPublishTest6() throws InterruptedException {
        SiddhiAppRuntime executionPlanRuntime = null;
        ResultContainer resultContainer = new ResultContainer(2);
        JMSClient client = new JMSClient("activemq", "", "DAS_JMS_OUTPUT_TEST", resultContainer);
        try {
            //init
            Thread listenerThread = new Thread(client);
            listenerThread.start();
            Thread.sleep(1000);

            // deploying the execution plan
            SiddhiManager siddhiManager = new SiddhiManager();
            String inStreamDefinition = "" +
                    "@sink(type='jms', @map(type='keyvalue'), "
                    + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                    + "provider.url='vm://localhost',"
                    + "destination='DAS_JMS_OUTPUT_TEST' "
                    + ")" +
                    "define stream inputStream (name string, age int, country string);";
            executionPlanRuntime = siddhiManager.
                    createSiddhiAppRuntime(inStreamDefinition);
            InputHandler inputStream = executionPlanRuntime.getInputHandler("inputStream");
            executionPlanRuntime.start();
            Thread.sleep(100);
            inputStream.send(new Object[]{"JAMES", 23, "USA"});
            inputStream.send(new Object[]{"MIKE", 23, "Germany"});

            Assert.assertTrue(resultContainer.assertMessageContent("JAMES"));
            Assert.assertTrue(resultContainer.assertMessageContent("MIKE"));
        } finally {
            client.shutdown();
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }
}
