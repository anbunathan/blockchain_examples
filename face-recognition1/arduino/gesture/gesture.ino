//Board used = Roboindia blink board
#include <Servo.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>


const char* ssid = "TP-Link_8700";
const char* password = "iotapp8266";

const char* mqtt_server = "sphinx-mqtt.tk";

//// DHT11 Sensor Data Pin
//#define DHTPIN    D5
//#define DHTTYPE DHT11   // DHT 11
//#define dht_apin D5
//
//// Initialize DHT Sensor
//DHT dht( DHTPIN, DHTTYPE);

Servo servo;

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
int value = 0;


void setup_wifi() {
  delay(100);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  // Relay1 (Load1) Topic
  if ( strcmp(topic, "gesture") == 0 )
  {
    
    String msgIN = "";
    for (int i = 0; i < length; i++)
    {
      msgIN += (char)payload[i];
    }
    String msgString = msgIN;
    Serial.println(msgString);
    
    if ( msgString == "open" || msgString == "OPEN")
    {
      servo.write(90);
      delay(1000);
      Serial.print("gesture: ");
      Serial.println("open");
    }
    else if ( msgString == "close" || msgString == "CLOSE")
    {
      servo.write(0);
      delay(1000);
      Serial.print("gesture: ");
      Serial.println("close");
    }
  }
}

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Create a random client ID
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    //    if (client.connect(clientId.c_str(), mqtt_user, mqtt_pass)) {
    if (client.connect(clientId.c_str())) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      client.publish("outTopic", "hello world");
      // ... and resubscribe
      //      client.subscribe("inTopic");
      client.subscribe("gesture");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void setup() {

  Serial.begin(115200);
  servo.attach(2);  //D4
  servo.write(0);
  delay(2000);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
  reconnect();
}

void loop() {

  if (!client.connected()) {
    reconnect();
  }

  client.loop();
//  float h = dht.readHumidity();
//  // Read temperature as Celsius (the default)
//  float t = dht.readTemperature();
//
//  if ( isnan(h) || isnan(t) || t > 100.00)
//  {
//    // Don't do anything, if data is invalid
//    //    Serial.println("DHT11 data in invalid");
//  }
//  else
//  {
//    uint8_t temp = (uint8_t)(t);
//    uint8_t humid = (uint8_t)(h);
//
//    delay(1000);
//    String hh = String(humid);
//    String msg = String(temp);
//
//    Serial.print("Publish message: ");
//    Serial.println(msg);
//
//    uint8_t numt = temp;
//    char cstr[16];
//    itoa(numt, cstr, 10);
//
//    uint8_t numh = humid;
//    char cshr[16];
//    itoa(numh, cshr, 10);
//
//    delay(1500);
//    client.publish("dht", cstr);
//    client.publish("bmp", cshr);
//  }


}
