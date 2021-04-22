package io.exiled.demo.mqtt;

import io.exiled.demo.drone.Drone;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates three instances of the @link{io.exiled.demo.drone.Drone} class,
 * makes them connect to the MQTT broker, and sends messages and commands.
 * This class declares the main static method for the example application.
 *
 * @author Vassili Moskaljov
 * @version 1.0
 */
public class MqttSample01 {
    public static void main(String... args) {
        Drone drone1 = new Drone("[Drone #1]");
        drone1.connect();
        Drone drone2 = new Drone("[Drone #2]");
        drone2.connect();
        Drone masterDrone = new Drone("*Master Drone*");
        masterDrone.connect();

        try {
            while (true) {
                try {
                    Thread.sleep(5000);
                    int r =
                            ThreadLocalRandom.current()
                                    .nextInt(1, 11);
                    if ((r < 5) && drone1.isConnected()) {
                        masterDrone.publishCommand(
                                Drone.GET_ALTITUDE_COMMAND_KEY,
                                drone1.getName());
                    } else if (drone2.isConnected()) {
                        masterDrone.publishCommand(
                                Drone.GET_ALTITUDE_COMMAND_KEY,
                                drone2.getName());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (drone1.isConnected()) {
                try {
                    drone1.client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            if (drone1.isConnected()) {
                try {
                    drone2.client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            if (drone1.isConnected()) {
                try {
                    masterDrone.client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            //...similarly for drone2 and masterDrone…
        }
    }
}
