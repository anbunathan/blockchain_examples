#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>
#include <ArduinoJson.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <WiFiClientSecure.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <TimeLib.h>               // https://github.com/bblanchon/ArduinoJson
#include "secret.h"                       // uncomment if using secret.h file with credentials
//#define TWI_TIMEOUT 3000                  // varies depending on network speed (msec), needs to be before TwitterWebAPI.h
#include <TwitterWebAPI.h>
#include "DHT.h"

// DHT11 sensor pins
#define DHTPIN 14
#define DHTTYPE DHT11
// Initialize DHT sensor
DHT dht(DHTPIN, DHTTYPE, 15);

const int R1  = 5;            // Output Relay 1 GPI0 05 (D1)
const int R2  = 4;            // Output Relay 2 GPI0 04 (D2)
const int REDLED  = 12;            // Output RED LED GPI0 12 (D6)

std::string search_str = "#dog";          // Default search word for twitter
const char *ntp_server = "pool.ntp.org";  // time1.google.com, time.nist.gov, pool.ntp.org
int timezone = -5;                        // US Eastern timezone -05:00 HRS
unsigned long twi_update_interval = 120;   // (seconds) minimum 5s (180 API calls/15 min). Any value less than 5 is ignored!

unsigned long api_mtbs = twi_update_interval * 1000; //mean time between api requests
unsigned long api_lasttime = 0; 

//Older OneWire libraries may cause errors
// ESP8266 ESP-01 GPIO2 make sure this is correct for your ESP8266 version
#define ONE_WIRE_BUS 2

// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature DS18B20(&oneWire);
int temperatureF;
float temperatureC;
int counter;
int currentTemperature;

#ifndef WIFICONFIG
const char* ssid = "WiFi SSID";           // WiFi SSID
const char* password = " WiFi Password";   // WiFi Password
#endif

void setup() {
  counter = 0;
  //ESP8266 ESP-01 GPIO0
  //green 5mm LED attached to GPIO0
  pinMode(0, OUTPUT);
  pinMode(R1,OUTPUT);
  pinMode(R2,OUTPUT);
  pinMode(REDLED,OUTPUT);
  Serial.begin(115200); //Serial connection
  DS18B20.begin();
//  WiFi.begin("blockchain", "ethereum");   //WiFi connection
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {  //Wait for the WiFI connection completion
 
    delay(500);
    Serial.println("Waiting for connection");
 
  }
    Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
}


void getTemperature() {
  float tempC;
  float tempF;
  if (millis() > api_lasttime + api_mtbs)  {
    
    digitalWrite(LED_BUILTIN, LOW);
    digitalWrite(R1, LOW);
    digitalWrite(R2, LOW);
//    digitalWrite(REDLED, LOW);
    // Read data
    float h ;
    float t ;
    int count=0;
    do{
      h = dht.readHumidity();
      t = dht.readTemperature();
      count++;
      if(count>=25){
        h = 0;
        t = 0;
        String twittermsg = "The temperature is " + String(t) + " and the humidity is " + String(h) + ".";
        break;
      }
      delay(5000);
    }while(isnan(h) || isnan(t));
    String temp = "#temperature " + String(t);
    Serial.println("temperature in C = ");
    Serial.println(temp); 
    temperatureC = t;
    api_lasttime = millis();
  }
  digitalWrite(LED_BUILTIN, HIGH);
  digitalWrite(R1, HIGH);
  digitalWrite(R2, HIGH);
//  digitalWrite(REDLED, HIGH);
//  do {
//    DS18B20.requestTemperatures(); 
//    tempC = DS18B20.getTempCByIndex(0);
//    temperatureC = tempC;
//    tempF = DS18B20.getTempFByIndex(0);
//    temperatureF = tempF;
//    delay(100);
//  } while (tempC == 85.0 || tempC == (-127.0));
}


String callGeth(String inputJSON)
{
  HTTPClient http;    //Declare object of class HTTPClient
 
    http.begin("http://192.168.2.1:8545/");      //TRAILING SLASH AT END REQUIRED!!!
//    http.begin("http://192.168.0.107:8545/");      //TRAILING SLASH AT END REQUIRED!!!
    http.addHeader("Content-Type", "application/json");  //Specify content-type header
 
    int httpCode = http.POST(inputJSON);   //Send the request and get http Code
    String JSONResult = http.getString();  //Get the response from Geth JSONRPC
    
    http.end();
    return JSONResult;
}

 
void loop() {
  counter++;
  if (WiFi.status() == WL_CONNECTED) { //Check WiFi connection status

    StaticJsonBuffer<1000> JSONbuffer;   //Declaring static JSON buffer and set high value maybe 500 per call
    JsonObject& gethQueryJSON = JSONbuffer.createObject(); 
    gethQueryJSON["jsonrpc"] = "2.0";
    gethQueryJSON["method"] = "eth_call";
    JsonArray&  gethQueryParams = gethQueryJSON.createNestedArray("params");
    JsonObject& gethCallParams = JSONbuffer.createObject();
    gethCallParams["to"] = "0x513c67ef8dd393A423900aaFCc78A6878e465aE5";
    gethCallParams["data"] = "0x9de4d683";
    gethQueryParams.add(gethCallParams);
    gethQueryParams.add("latest");
    gethQueryJSON["id"] = 1;
 
    String gethStringJSON;
    gethQueryJSON.printTo(gethStringJSON);
    Serial.println("First Geth query JSON message isLightTurnedOn function: ");
    Serial.println(gethStringJSON);
     
    String gethResult = callGeth(gethStringJSON);  //Get the response from Geth JSONRPC
    JsonObject& gethJSONRPC = JSONbuffer.parseObject(gethResult);
    String lightOnString = gethJSONRPC["result"];
    lightOnString.remove(0,2);
    
    
    //parsing & converting Geth JSON-RPC hex results is not easy
    long int lightOn = strtol(lightOnString.c_str(), NULL, 16);

    Serial.println("Geth JSON-RPC response: ");
    Serial.println(gethResult);    //Print request response payload
    //Serial.println("Hex function input: ");
    //Serial.println(lightOnString);
    //Serial.println("Function result: ");
    //Serial.println(lightOn);
    
    
    //***************************** second call to geth RPC function ****************************
    gethCallParams["data"] = "0x455f1a4c";
    String tempQuery;
    gethQueryJSON.printTo(tempQuery);
    Serial.println("Second Geth query JSON message isTempCurrent function: ");
    Serial.println(tempQuery);

    String tempStatus = callGeth(tempQuery);  //Get the response from Geth JSONRPC
    JsonObject& tempRPC = JSONbuffer.parseObject(tempStatus);
    String tempString = tempRPC["result"];
    tempString.remove(0,2);
    
    long int tempUpdate = strtol(tempString.c_str(), NULL, 16);

    Serial.println("Second Geth JSON-RPC response: ");
    Serial.println(tempStatus);    //Print request response payload
    //if the JSON buffer is too small, they'll be no or bunk output
    //hence the serial debugs
    //Serial.println("Hex Function input: ");
    //Serial.println(tempString);
    //Serial.println("Function result: ");
    //Serial.println(tempUpdate);
    

    //Turns on LED if blockchain state has been changed
    if( lightOn ){
//      digitalWrite(0, HIGH); 
      digitalWrite(REDLED, HIGH);  
    } else {
//      digitalWrite(0, LOW);
      digitalWrite(REDLED, LOW);
    }


    //***********************************third call to RPC to send Temperature*******************
    //sends temperature every 10min, or if smart contract activates, or if temp changes by 1 degree
    getTemperature();

    if( !tempUpdate || counter % 40 == 0 || currentTemperature != temperatureF ){
      if( !tempUpdate ){ Serial.println("**************** User requested temperature"); }
      if( counter % 40 == 0 ){ Serial.println("*********** 10 minute Temperature Update"); }
      if( currentTemperature != temperatureF )
      {
        Serial.println("*********** Temperature changed by at least one degreeF: ");
        Serial.println(temperatureF);
        currentTemperature = temperatureF;
      }
      
      if( counter == 4000 ){ counter = 0; } //keep counter in check

      //creating another JSON buffer to avoid confusion with the first 
      StaticJsonBuffer<1000> JSONbufferTwo;  
      JsonObject& uploadJSON = JSONbufferTwo.createObject(); 
      uploadJSON["jsonrpc"] = "2.0";
      uploadJSON["method"] = "personal_sendTransaction";
      
      JsonArray&  uploadQueryParams = uploadJSON.createNestedArray("params");
      
      JsonObject& callTxParams = JSONbufferTwo.createObject();
//      callTxParams["from"] = "0xbca66e58394E730b70593020c4D10819C613755f";
      callTxParams["from"] = "0x36e22b7111688aacac4171061bcb90d2738e8cb7";
      callTxParams["to"] = "0x513c67ef8dd393A423900aaFCc78A6878e465aE5";
      callTxParams["gas"] = "0x30D40"; //hex value for 200000 -high gas limit for good measure          
      callTxParams["gasPrice"] = "0x6FC23AC00"; //hex value 30 Gwei gasprice 21gwei is typical

      //convert temperature to hex and pad with zeroes
      String hexTemperature = String(temperatureF, HEX);
      String paddedZeroes;
      for(int x = 0; x < (64 - hexTemperature.length()); x++)
      {
        paddedZeroes = String(paddedZeroes + "0");
      }
      hexTemperature = String(paddedZeroes + hexTemperature);
      //sha3 hash of setTempDeviceOnly function name
      hexTemperature = String("0x0e56dbea" + hexTemperature);
      callTxParams["data"] = hexTemperature;
      
      uploadQueryParams.add(callTxParams);
//      uploadQueryParams.add("testpasswd");
      uploadQueryParams.add("blockchain8266");
      uploadJSON["id"] = 1;

      String uploadString;
      uploadJSON.printTo(uploadString);
      Serial.println("Temperature Geth CALL JSON message setTempDeviceOnly function: ");
      Serial.println(uploadString);

      //!!!! This RPC call is insecure plain text password over the wifi LAN !!!!!!
      //Secure firewall and secure environment for private key/geth node is expected
      //I am looking for better ways to push data off IoT devices with minimum resources and open ports
      //NodeJS unlocking the accounts via IPC on the wifiRouter node seems best for now
      //I am looking for microcontroller IoT way to remotely securely unlock sign transactions without NodeJS
      //Suggestions?
      String uploadResult = callGeth(uploadString);  //Get the response from Geth JSONRPC
      JsonObject& gethJSONRPC = JSONbuffer.parseObject(uploadResult);
      Serial.println("**** Set Temp Call result TX hash: ");
      Serial.println(uploadResult);      
    }

   
    Serial.println("Counter: ");
    Serial.println(counter);
    Serial.println("Temperature in F: ");
    Serial.println(temperatureF);
    Serial.println("Temperature in C: ");
    Serial.println(temperatureC);
        
  } else {
 
    Serial.println("Error in WiFi connection");
 
  }
 
  delay(15000);  //Send a request every 15ish seconds
 
}
