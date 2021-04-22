package io.exiled.demo.listener;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * Implements the @link{org.eclipse.paho.client.mqttv3}.IMqttActionListener interface.
 * I use instances of this class to specify the callback that will run code
 * whenever a message has been successfully published to a topic.
 *
 * @author Vassili Moskaljov
 * @version 1.0
 */
public class MessageActionListener implements IMqttActionListener {
    protected final String messageText;
    protected final String topic;
    protected final String userContext;

    public MessageActionListener(String messageText, String topic, String userContext) {
        this.messageText = messageText;
        this.topic = topic;
        this.userContext = userContext;
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        if ((iMqttToken != null) && iMqttToken.getUserContext().equals(userContext)) {
            System.out.println(String.format(
                    "Message '%s' published to topic '%s'",
                    messageText, topic
            ));
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        throwable.printStackTrace();
    }
}
