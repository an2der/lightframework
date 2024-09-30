import com.lightframework.comm.mqtt.MqttClientManager;
import com.lightframework.comm.mqtt.MqttConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {
        MqttConfig mqttConfig = new MqttConfig();
        mqttConfig.setHost("tcp://192.168.82.178:1883");
        mqttConfig.setUserName("admin");
        mqttConfig.setPassword("admin");
        mqttConfig.setTopicFilters(new String[]{"123"});
        MqttClientManager mqttClientManager = new MqttClientManager(mqttConfig);
        try {
        mqttClientManager.connect();
            TimeUnit.SECONDS.sleep(10);

            System.in.read();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
