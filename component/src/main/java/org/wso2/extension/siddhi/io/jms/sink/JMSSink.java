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

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.transport.DynamicOptions;
import io.siddhi.core.util.transport.Option;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.query.api.definition.StreamDefinition;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.jms.util.JMSOptionsMapper;
import org.wso2.transport.jms.contract.JMSClientConnector;
import org.wso2.transport.jms.exception.JMSConnectorException;
import org.wso2.transport.jms.impl.JMSConnectorFactoryImpl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.wso2.extension.siddhi.io.jms.util.JMSOptionsMapper.DESTINATION;

/**
 * JMS output transport class.
 * Dynamic options: destination
 */
@Extension(
        name = "jms",
        namespace = "sink",
        description = "JMS Sink allows users to subscribe to a JMS broker and publish JMS messages.",
        parameters = {
                @Parameter(name = JMSOptionsMapper.DESTINATION,
                        description = "Queue/Topic name which JMS Source should subscribe to",
                        type = DataType.STRING,
                        dynamic = true
                ),
                @Parameter(name = JMSOptionsMapper.CONNECTION_FACTORY_JNDI_NAME,
                        description = "JMS Connection Factory JNDI name. This value will be used for the JNDI "
                                + "lookup to find the JMS Connection Factory.",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = "QueueConnectionFactory"),
                @Parameter(name = JMSOptionsMapper.FACTORY_INITIAL,
                        description = "Naming factory initial value",
                        type = DataType.STRING),
                @Parameter(name = JMSOptionsMapper.PROVIDER_URL,
                        description = "Java naming provider URL. Property for specifying configuration "
                                + "information for the service provider to use. The value of the property should "
                                + "contain a URL string (e.g. \"ldap://somehost:389\")",
                        type = DataType.STRING),
                @Parameter(name = JMSOptionsMapper.CONNECTION_FACTORY_TYPE,
                        description = "Type of the connection connection factory. This can be either queue or "
                                + "topic.",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = "queue"),
                @Parameter(name = JMSOptionsMapper.CONNECTION_USERNAME,
                        description = "username for the broker.",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = "None"),
                @Parameter(name = JMSOptionsMapper.CONNECTION_PASSWORD,
                        description = "Password for the broker",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = "None"),
                @Parameter(name = JMSOptionsMapper.CONNECTION_FACTORY_NATURE,
                        description = "Connection factory nature for the broker(cached/pooled).",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = "default")
        },
        examples = {
                @Example(description = "This example shows how to publish to an ActiveMQ topic.",
                        syntax = "@sink(type='jms', @map(type='xml'), "
                                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                                + "provider.url='vm://localhost',"
                                + "destination='DAS_JMS_OUTPUT_TEST', "
                                + "connection.factory.type='topic',"
                                + "connection.factory.jndi.name='TopicConnectionFactory'"
                                + ")\n" +
                                "define stream inputStream (name string, age int, country string);"),
                @Example(description = "This example shows how to publish to an ActiveMQ queue. "
                        + "Note that we are not providing properties like connection factory type",
                        syntax = "@sink(type='jms', @map(type='xml'), "
                                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                                + "provider.url='vm://localhost',"
                                + "destination='DAS_JMS_OUTPUT_TEST')\n" +
                                "define stream inputStream (name string, age int, country string);")
        }
)
public class JMSSink extends Sink {
    private static final Logger log = Logger.getLogger(JMSSink.class);
    private OptionHolder optionHolder;
    private JMSClientConnector clientConnector;
    private Option destination;
    private Map<String, String> jmsStaticProperties;
    private ExecutorService executorService;

    @Override
    protected void init(StreamDefinition outputStreamDefinition, OptionHolder optionHolder,
                        ConfigReader sinkConfigReader, SiddhiAppContext executionPlanContext) {
        this.optionHolder = optionHolder;
        this.destination = optionHolder.getOrCreateOption(DESTINATION, null);
        this.jmsStaticProperties = initJMSProperties();
        this.executorService = executionPlanContext.getExecutorService();
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        try {
            this.clientConnector = new JMSConnectorFactoryImpl().createClientConnector(jmsStaticProperties);
        } catch (JMSConnectorException e) {
            log.error("Error while connecting to JMS provider at destination: " + destination);
            throw new ConnectionUnavailableException("Error while connecting to JMS provider at destination: "
                    + destination, e);
        }
    }

    @Override
    public void publish(Object payload, DynamicOptions transportOptions) {
        String topicQueueName = destination.getValue(transportOptions);
        executorService.execute(new JMSPublisher(topicQueueName, jmsStaticProperties,
                clientConnector, payload));
    }

    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class, Map.class, ByteBuffer.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
        return new String[]{DESTINATION};
    }

    @Override
    public void disconnect() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void destroy() {
        // disconnect() gets called before destroy() which does the cleanup destroy() needs
    }

    /**
     * Initializing JMS properties.
     * The properties in the required options list are mandatory.
     * Other JMS options can be passed in as key value pairs, key being in the JMS spec or the broker spec.
     *
     * @return all the options map.
     */
    private Map<String, String> initJMSProperties() {
        List<String> requiredOptions = JMSOptionsMapper.getRequiredOptions();
        Map<String, String> customPropertyMapping = JMSOptionsMapper.getCarbonPropertyMapping();
        // getting the required values
        Map<String, String> transportProperties = new HashMap<>();
        requiredOptions.forEach(requiredOption ->
                transportProperties.put(customPropertyMapping.get(requiredOption),
                        optionHolder.validateAndGetStaticValue(requiredOption)));
        // getting optional values
        optionHolder.getStaticOptionsKeys().stream()
                .filter(option -> !requiredOptions.contains(option) && !option.equals("type")).forEach(option ->
                transportProperties.put(customPropertyMapping.get(option), optionHolder.validateAndGetStaticValue
                        (option)));
        return transportProperties;
    }
}
