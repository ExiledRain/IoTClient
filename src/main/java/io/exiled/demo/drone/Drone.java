package io.exiled.demo.drone;

import io.exiled.demo.listener.MessageActionListener;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a drone that has a name and can send and receive messages
 * through the MQTT broker. This class not only encapsulates the data and logic
 * related to drones as well as the messages and the commands included in messages,
 * but it also implements the MqttCallback and ImqttActionListener interfaces defined
 * in org.eclipse.paho.client.mqttv3. There is no type in these interface names—the
 * naming convention for interfaces in the Java library is a bit confusing because
 * some interfaces start with I while others don’t. I use Drone instances as callbacks
 * for specific events.
 *
 * @author Vassili Moskaljov
 * @version 1.0
 */
public class Drone implements MqttCallback, IMqttActionListener {
    public static final String COMMAND_KEY = "COMMAND";
    public static final String COMMAND_SEPARATOR = ":";
    public static final String GET_ALTITUDE_COMMAND_KEY = "GET_ALTITUDE";
    // Replace with your own topic name
    public static final String TOPIC =
            "mqtt/drones/altitude";

    public static final String ENCODING = "UTF-8";

    // Quality of Service = Exactly once
    // I want to receive all messages exactly once
    public static final int QUALITY_OF_SERVICE = 2;
    protected String name;
    protected String clientId;
    public MqttAsyncClient client;
    protected MemoryPersistence memoryPersistence;
    protected IMqttToken connectToken;
    protected IMqttToken subscribeToken;

    public Drone(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void connect() {
        try {
            MqttConnectOptions options =
                    new MqttConnectOptions();
            // options.setUserName(
            //    "replace with your username");
            // options.setPassword(
            //    "replace with your password"
            //    .toCharArray());
            // Replace with ssl:// and work with TLS/SSL
            // best practices in a
            // production environment
            memoryPersistence =
                    new MemoryPersistence();
            String serverURI =
//                    "tcp://iot.eclipse.org:1883";
                    "tcp://localhost:1883";
            clientId = MqttAsyncClient.generateClientId();
            client = new MqttAsyncClient(
                    serverURI, clientId,
                    memoryPersistence);
            // I want to use this instance as the callback
            //It is very important to call the setCallback method before establishing the connection with the MQTT broker.
            client.setCallback(this);
            connectToken = client.connect(
                    options, null, this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return (client != null) &&
                (client.isConnected());
    }

    public MessageActionListener publishTextMessage(String messageText) {
        byte[] byteMessage;

        try {
            byteMessage = messageText.getBytes(ENCODING);
            MqttMessage message;
            message = new MqttMessage(byteMessage);
            String userContext = "ListeningMessage";
            MessageActionListener actionListener =
                    new MessageActionListener(
                            TOPIC, messageText, userContext
                    );
            client.publish(TOPIC, message);
            return actionListener;
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MessageActionListener publishCommand(String commandName, String destinationName) {
        String command = String.format(
                "%s%s%s%s%s",
                COMMAND_KEY, COMMAND_SEPARATOR,
                commandName, COMMAND_SEPARATOR,
                destinationName
        );
        return publishTextMessage(command);
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        if (iMqttToken.equals(connectToken)) {
            System.out.println(String.format(
                    "%s successfully connected.",
                    name
            ));
            try {
                subscribeToken = client.subscribe(
                        TOPIC, QUALITY_OF_SERVICE,
                        null, this
                );
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else if (iMqttToken.equals(subscribeToken)) {
            System.out.println(String.format(
                    "%s subscribed to the %s topic",
                    name, TOPIC
            ));
            publishTextMessage(String.format(
                    "%s is listening.",
                    name
            ));
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // A message has arrived from the MQTT broker
        // The MQTT broker doesn't send back
        // an acknowledgment to the server until
        // this method returns cleanly
        if (!topic.equals(TOPIC)) {
            return;
        }

        String messageText =
                new String(message.getPayload(), ENCODING);
        System.out.println(String.format(
                "%s received %s: %s", name, topic,
                messageText));
        String[] keyValue =
                messageText.split(COMMAND_SEPARATOR);
        if (keyValue.length != 3) {
            return;
        }
        if (keyValue[0].equals(COMMAND_KEY) &&
                keyValue[1].equals(
                        GET_ALTITUDE_COMMAND_KEY) &&
                keyValue[2].equals(name)) {
            // Process the "get altitude" command
            int altitudeInFeet = ThreadLocalRandom
                    .current().nextInt(1, 6001);
            System.out.println(String.format(
                    "%s altitude: %d feet",
                    name, altitudeInFeet));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // Delivery for a message has been completed
        // and all acknowledgments have been received
    }
}
