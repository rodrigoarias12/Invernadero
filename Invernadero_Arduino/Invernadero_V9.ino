#include <SoftwareSerial.h>
#include <ArduinoJson.h>
#include "DHT.h"          

#define DHTPIN 2          //Seleccionamos el pin en el que se //conectará el sensor
#define DHTTYPE DHT11     //Se selecciona el DHT11 (hay //otros DHT)
DHT dht(DHTPIN, DHTTYPE); //Se inicia una variable que será usada por Arduino para comunicarse con el sensor

/*-------------------------------------------------------------------------*/
/*                             Conexiones                                  */ 
/*-------------------------------------------------------------------------*/
SoftwareSerial BT1(10, 11); // RX | TX
const int ActReleLed         = 12;
const int ActReleVentilacion = 4;
const int ActReleRiego       = 7;
const int AnalogNivelPin     = A0;
const int AnalogHumPin       = A1;

/*-------------------------------------------------------------------------*/
/*                   Variables globales de SET/RESET                       */ 
/*-------------------------------------------------------------------------*/
//Modo de trabajo
int   SetModo        = 2;
//Actuadores
int   SetLed         = 0;
int   SetRiego       = 0;
int   SetVentilacion = 0;
//Sensores
float SetTempAmbMin  = 24; 
float SetTempAmbMax  = 26; 
float SetHumAmbMin   = 20; 
float SetHumAmbMax   = 80; 
int   SetHumTieMin   = 800; 
int   SetHumTieMax   = 1023;   
//PWM
boolean ActLed       = false;
int     pwm          = 255;

//Datos a recibir desde la App
String datosRecibidos;                // String que devuelve App

/*-------------------------------------------------------------------------*/
/*                             Ejecutable                                  */ 
/*-------------------------------------------------------------------------*/
void setup() {
  pinMode(8, OUTPUT);                          // Al poner en HIGH forzaremos el modo AT
  pinMode(9, OUTPUT);                          // cuando se alimente de aqui
  
  pinMode(ActReleLed, OUTPUT);
  digitalWrite(ActReleLed, LOW); 
  pinMode(ActReleVentilacion, OUTPUT);
  digitalWrite(ActReleVentilacion, LOW);
  pinMode(ActReleRiego, OUTPUT);
  digitalWrite(ActReleRiego, LOW); 
  
  digitalWrite(9, HIGH);
  delay (500) ;                                // Espera antes de encender el modulo
  Serial.begin(9600);
  Serial.println("Levantando el modulo HC-06");
  digitalWrite (8, HIGH);                      //Enciende el modulo
  Serial.println("Esperando comandos AT:");
  BT1.begin(9600);                             //Se inicia comunucacion BT    
  dht.begin();                                 //Se inicia el sensor Temperatura/Humedad del ambiente
}

void loop(){
  BT1.flush();
  ejecucion();
}//fin loop

/*-------------------------------------------------------------------------*/
/*                              Funciones                                  */ 
/*-------------------------------------------------------------------------*/
void ejecucion(){
// Recibir de Bluetooth constantemente
if (BT1.available())
  {
    Serial.println("Levantando el modulo HC-05");
    datosRecibidos = BT1.readString();
    Serial.println(datosRecibidos);
    BT1.flush();
/*//NO VA
String datosRecibidos = "{\"Led\":1,\"Riego\":9,\"Ventilacion\":9,\"Reset\":0,\"TemperaturaAmbienteMin\":99,\"TemperaturaAmbienteMax\":99,\"HumedadAmbienteMin\":99,\"HumedadAmbienteMax\":99,\"HumedadTierraMin\":99,\"HumedadTierraMax\":99}";
Serial.println(datosRecibidos);
*/
    const size_t bufferSize = JSON_OBJECT_SIZE(15) + 230;
    DynamicJsonBuffer jsonBuffer(bufferSize);
    JsonObject& root = jsonBuffer.parseObject(datosRecibidos);

    //Si no se logro el Parse
    if(!root.success()) {
      Serial.println("parseObject() failed");
      return;
    }
    else{
      //Setea datos globales
      setResetDatos(root);
      //Envia información de los sensores y actuadores a la App
      enviarDatos();
    }
   }

//Comportamiento de los actuadores   
  accionLed();
  accionVentilacion();
  accionRiego();
}

//Setea/Resetea los valores de los actuadores/sensores según info. App
void setResetDatos(JsonObject& root){

//Setea el modo de trabajo Automatico = 0 o Manual = 2
  if(root["Reset"] == 0){
    SetModo = root["Reset"];
  }

    SetLed = root["Led"];
    SetRiego = root["Riego"];
    SetVentilacion = root["Ventilacion"];
    
  if(root["Reset"] == 9){
    //Valor de TEMP del AMBIENTE MIN
      SetTempAmbMin = root["a"]; 

    //Valor de TEMP del AMBIENTE MAX
      SetTempAmbMax = root["b"]; 

    //Valor de HUM del AMBIENTE MIN
      SetHumAmbMin = root["c"]; 

    //Valor de HUM del AMBIENTE MAX
      SetHumAmbMax = root["d"]; 

    //Valor de HUM de la TIERRA MIN
      SetHumTieMin = root["e"]; 

    //Valor de HUM de la TIERRA MAX
      SetHumTieMax = root["f"]; 
  }
}

//Le dice al LED como comportarse
void accionLed(){ 
  if(SetModo == 0){                       //Modo Manual
    if(SetLed == 1){     
      digitalWrite(ActReleLed, HIGH);   
    }
    else{
      digitalWrite(ActReleLed, LOW);
    }
  }   
  else{                                   //Modo Automatico
    if(dht.readTemperature() < SetTempAmbMin){
      digitalWrite(ActReleLed, HIGH);
    }
    else{
      if(dht.readTemperature() >= SetTempAmbMin){
        digitalWrite(ActReleLed, LOW);
      }
    } 
  }
}

//Le dice a los ventiladores como comportarse  
void accionVentilacion(){
  if(SetModo == 0){                       //Modo Manual
    if(SetVentilacion == 1){
      digitalWrite(ActReleVentilacion, HIGH);
    }
    else{
      digitalWrite(ActReleVentilacion, LOW);
    }   
  }
  else{                                   //Modo Automatico
    if(dht.readTemperature() >= SetTempAmbMax){
      digitalWrite(ActReleVentilacion, HIGH);
    }
    else{
      if(dht.readTemperature() <= SetTempAmbMin){
        digitalWrite(ActReleVentilacion, LOW);
      }
    } 
  }
}

//Le dice al sist. de riego como comportarse 
void accionRiego(){
  //Solo analizo si se riega o no si el nivel de agua es correcto, de lo contrario se podria quemar el motor. 
   float porcentHumedad = 100-0.0245*analogRead(AnalogHumPin);
  
  if(analogRead(AnalogNivelPin) >= 300){  
    if(SetModo == 0){                       //Modo Manual
      if(SetRiego == 1){
        digitalWrite(ActReleRiego, HIGH);
        delay(3000);
        digitalWrite(ActReleRiego, LOW);
      }
      else{
        digitalWrite(ActReleRiego, LOW);
      }   
    }
    else{                                   //Modo Automatico
      if(porcentHumedad >= SetHumTieMin){
        digitalWrite(ActReleRiego, HIGH);
        delay(3000);
        digitalWrite(ActReleRiego, LOW);
      }
      else{
        if(porcentHumedad <= SetHumTieMin){
          digitalWrite(ActReleRiego, LOW);
        }
      } 
    }
    analogWrite(6,LOW);
  }  
  else{
    digitalWrite(ActReleRiego, LOW);  
    //PWM
    if(ActLed){ 
       pwm+=10;
       analogWrite(6,pwm);
       if(pwm>=250)
         ActLed=false; 
       delay(50);
     }
     else{ 
       analogWrite(6,pwm);
       pwm-=10;
       if(pwm<=0)
         ActLed=true; 
       delay(50);
     }
  } 
}

//Envia datos a Serial (debug) y a BT
void enviarDatos(){  

  float porcentHumedad = 100-0.0245*analogRead(AnalogHumPin);
// Serial.print("{\"TemperaturaAmbiente\":0,\"HumedadAmbente\":0,\"HumedadTierra\":12,\"NivelAgua\":12}");

  String c1="{\"TemperaturaAmbiente\":";
  c1.concat(dht.readTemperature());
  delay(1000);
  String c2=",\"HumedadAmbiente\":";
  c1.concat(c2);
  c1.concat(dht.readHumidity());
  String c3=",\"HumedadTierra\":";
  c1.concat(c3);
  c1.concat(porcentHumedad);
  String c4=",\"NivelAgua\":";
  c1.concat(c4);
  c1.concat(analogRead(AnalogNivelPin));
  String c5="}";
  c1.concat(c5);

  Serial.println(c1);

  BT1.print(c1);
}




