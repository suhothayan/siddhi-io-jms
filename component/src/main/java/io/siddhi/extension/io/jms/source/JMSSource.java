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
package io.siddhi.extension.io.jms.source;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.extension.io.jms.source.exception.JMSInputAdaptorRuntimeException;
import io.siddhi.extension.io.jms.util.JMSOptionsMapper;
import org.apache.log4j.Logger;
import org.wso2.transport.jms.contract.JMSServerConnector;
import org.wso2.transport.jms.exception.JMSConnectorException;
import org.wso2.transport.jms.receiver.JMSServerConnectorImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JMS Source implementation.
 */
@Extension(
        name = "jms",
        namespace = "source",
        description = "JMS Source allows users to subscribe to a JMS broker and receive JMS messages. It has the "
                + "ability to receive Map messages and Text messages.",
        parameters = {
                @Parameter(name = JMSOptionsMapper.DESTINATION,
                        description = "Queue/Topic name which JMS Source should subscribe to",
                        type = DataType.STRING
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
                @Parameter(name = JMSOptionsMapper.WORKER_COUNT,
                        description = "Number of worker threads listening on the given queue/topic.",
                        type = DataType.INT,
                        optional = true,
                        defaultValue = "1"),
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
                @Parameter(name = JMSOptionsMapper.RETRY_INTERVAL,
                        description = "Interval between each retry attempt in case of connection failure in "
                                + "milliseconds.",
                        type = DataType.INT,
                        optional = true,
                        defaultValue = "10000"),
                @Parameter(name = JMSOptionsMapper.MAX_RETRY_COUNT,
                        description = "Number of maximum reties that will be attempted in case of connection "
                                + "failure with broker.",
                        type = DataType.INT,
                        optional = true,
                        defaultValue = "5"),
                @Parameter(name = JMSOptionsMapper.USE_RECEIVER,
                        description = "Implementation to be used when consuming JMS messages. By default transport"
                                + " will use MessageListener and tweaking this property will make make use of "
                                + "MessageReceiver",
                        type = DataType.BOOL,
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = JMSOptionsMapper.PARAM_SUB_DURABLE,
                        description = "Property to enable durable subscription.",
                        type = DataType.BOOL,
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = JMSOptionsMapper.CONNECTION_FACTORY_NATURE,
                        description = "Connection factory nature for the broker.",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = "default")
        },
        examples = {
                @Example(description = "This example shows how to connect to an ActiveMQ topic and "
                        + "receive messages.",
                        syntax = "@source(type='jms', @map(type='json'), "
                                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                                + "provider.url='tcp://localhost:61616',"
                                + "destination='DAS_JMS_TEST', "
                                + "connection.factory.type='topic',"
                                + "connection.factory.jndi.name='TopicConnectionFactory'"
                                + ")\n" +
                                "define stream inputStream (name string, age int, country string);"),
                @Example(description = "This example shows how to connect to an ActiveMQ queue and "
                        + "receive messages. Note that we are not providing properties like connection factory type",
                        syntax = "@source(type='jms', @map(type='json'), "
                                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory', "
                                + "provider.url='tcp://localhost:61616',"
                                + "destination='DAS_JMS_TEST' "
                                + ")\n" +
                                "define stream inputStream (name string, age int, country string);")
        }
)
public class JMSSource extends Source {
    private static final Logger log = Logger.getLogger(JMSSource.class);
    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private JMSServerConnector jmsServerConnector;
    private JMSMessageProcessor jmsMessageProcessor;

    @Override
    public StateFactory init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                             String[] requestedTransportPropertyNames, ConfigReader configReader,
                             SiddhiAppContext siddhiAppContext) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
        Map<String, String> properties = initJMSProperties();

        jmsMessageProcessor = new JMSMessageProcessor(sourceEventListener, siddhiAppContext,
                requestedTransportPropertyNames);
        try {
            jmsServerConnector = new JMSServerConnectorImpl(null, properties, jmsMessageProcessor);
        } catch (JMSConnectorException e) {
            log.error("Error occurred in initializing the JMS receiver for stream: " +
                    sourceEventListener.getStreamDefinition().getId());
            throw new JMSInputAdaptorRuntimeException("Error occurred in initializing the JMS receiver for stream: " +
                    sourceEventListener.getStreamDefinition().getId(), e);
        }
        return null;
    }

    @Override
    public void connect(ConnectionCallback connectionCallback, State state) throws ConnectionUnavailableException {
        //ConnectionCallback is not used as re-connection is handled by carbon transport.
        try {
            jmsServerConnector.start();
        } catch (JMSConnectorException e) {
            //calling super class logs the exception and retry
            throw new ConnectionUnavailableException("Exception in starting the JMS receiver for stream: "
                    + sourceEventListener.getStreamDefinition().getId(), e);
        }
    }

    @Override
    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{String.class, Map.class};
    }

    @Override
    public void disconnect() {
        try {
            if (jmsServerConnector != null) {
                jmsServerConnector.stop();
            }
            if (jmsMessageProcessor != null) {
                jmsMessageProcessor.disconnect();
            }
        } catch (JMSConnectorException e) {
            log.error("Error disconnecting the JMS receiver", e);
        }
    }

    @Override
    public void destroy() {
        // disconnect() gets called before destroy() which does the cleanup destroy() needs
    }

    @Override
    public void pause() {
        jmsMessageProcessor.pause();
    }

    @Override
    public void resume() {
        jmsMessageProcessor.resume();
    }

    /**
     * Initializing JMS properties.
     * The properties in the required options list are mandatory.
     * Other JMS options can be passed in as key value pairs, key being in the JMS spec or the broker spec.
     *
     * @return all the options map.
     */
    private Map<String, String> initJMSProperties() {
        Map<String, String> carbonPropertyMapping = JMSOptionsMapper.getCarbonPropertyMapping();
        List<String> requiredOptions = JMSOptionsMapper.getRequiredOptions();
        // getting the required values
        Map<String, String> transportProperties = new HashMap<>();
        requiredOptions.forEach(requiredOption ->
                transportProperties.put(carbonPropertyMapping.get(requiredOption),
                        optionHolder.validateAndGetStaticValue(requiredOption)));
        // getting optional values
        optionHolder.getStaticOptionsKeys().stream()
                .filter(option -> !requiredOptions.contains(option) && !option.equals("type"))
                .forEach(option -> transportProperties.put(
                        carbonPropertyMapping.get(option) == null ? option : carbonPropertyMapping.get(option),
                        optionHolder.validateAndGetStaticValue(option)));
        return transportProperties;
    }
}
