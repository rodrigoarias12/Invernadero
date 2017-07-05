package com.example.romina.invernadero;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import android.widget.Switch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageButton;
import android.widget.EditText;


import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    //shake
    private final static float ACC = 30;
    private SensorManager sensor;
    private static final int SENSOR_SENSITIVITY = 4;
    //finshake

    //objetos de la vista
    Button btnInfoSensores, btnSetear;
    Switch SWLed, SWVentilacion, SWRiego, SWModo;
    EditText edithummin, editHumMax, editHumAmbMin, editHumAmbMax, editTempAmbientemin, editTempAmbienteMax;

    Handler bluetoothIn;

    // String para enviar a mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    public static String Configurar = "Mensaje";
    private static String mensaje = null;

    final int handlerState = 0;                         //utilizado para identificar handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String para MAC address
    private static String address = null;
    public static int riego, led, ventilacion,reset;
    private static String sensorHumedadTierra, sensorHumedadAmbiente, sensorNivelAgua, sensorTemperaturaAmbiente;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sensor celular
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerSenser();

        //Link the Switch con sus valores en la vista
        SWLed = (Switch) findViewById(R.id.mySwitch);
        SWRiego = (Switch) findViewById(R.id.mySwitch1);
        SWVentilacion = (Switch) findViewById(R.id.mySwitch2);
        SWModo = (Switch) findViewById(R.id.mySwitchModo);
        //Link a los botones con la vista
        btnInfoSensores = (Button) findViewById(R.id.btnInfoSensores);
        btnSetear = (Button) findViewById(R.id.btnSetear);
        //link a la edicion de datos en la vista
        edithummin = (EditText) findViewById(R.id.edithumMin);
        editHumMax = (EditText) findViewById(R.id.edithumMax);
        editHumAmbMin = (EditText) findViewById(R.id.editHumAmbienteMin);
        editHumAmbMax = (EditText) findViewById(R.id.editHumAmbienteMax);
        editTempAmbienteMax = (EditText) findViewById(R.id.editTempAmbienteMax);
        editTempAmbientemin = (EditText) findViewById(R.id.editTempAmbientemin);



        //para obtener los mensajes del bluetooth
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String jsonStr = msg.obj.toString().replaceAll("\\\\", "");
                    ;
                    String readMessage = (String) msg.obj;

                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("}");                    // determina el fin de linea
                    if (endOfLineIndex > 0) {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex + 1);    // extraigo el string
                        try {
                            JSONObject obj = new JSONObject(dataInPrint);
                            sensorHumedadTierra = (obj.getString("HumedadTierra"));
                            sensorHumedadAmbiente = (obj.getString("HumedadAmbiente"));
                            sensorNivelAgua = (obj.getString("NivelAgua"));
                            sensorTemperaturaAmbiente = (obj.getString("TemperaturaAmbiente"));

                            if (!obj.isNull("Led")) {
                                led = obj.getInt("Led");
                                ventilacion = obj.getInt("Ventilacion");
                                riego = obj.getInt("Riego");
                            }

                            Log.d("My App", obj.toString());

                        } catch (Throwable t) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + jsonStr + "\"");
                        }

                        recDataString.delete(0, recDataString.length());                    //limpio el string

                    }

                }
            }

        };


        btAdapter = BluetoothAdapter.getDefaultAdapter();       // Bluetooth adapter
        checkBTState();

        SWModo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    reset=2;
                   SWLed.setEnabled(false);
                    SWVentilacion.setEnabled(false);
                    SWRiego.setEnabled(false);
                    mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":"+reset+"}");
                    Toast.makeText(getBaseContext(), "Modo automatico", Toast.LENGTH_SHORT).show();

                } else {
                    reset=0;
                    SWLed.setEnabled(true);
                    SWVentilacion.setEnabled(true);
                    SWRiego.setEnabled(true);
                    mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":"+reset+"}");
                    Toast.makeText(getBaseContext(), "Modo manual", Toast.LENGTH_SHORT).show();
                }

            }
        });
        SWLed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    led = 1;
                    mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":0}");
                    Toast.makeText(getBaseContext(), "Led prendido", Toast.LENGTH_SHORT).show();

                } else {
                    led = 0;
                    mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":0}");
                    Toast.makeText(getBaseContext(), "Led apagado", Toast.LENGTH_SHORT).show();

                }

            }
        });
        SWRiego.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    riego = 1;
                     mConnectedThread.write(  "{\"Led\":"+led+",\"Riego\":"+riego+",\"Ventilacion\":"+ventilacion+",\"Reset\":0}");
                    Toast.makeText(getBaseContext(), "Encender el riego", Toast.LENGTH_SHORT).show();
                } else {
                    riego = 0;
                      mConnectedThread.write(  "{\"Led\":"+led+",\"Riego\":"+riego+",\"Ventilacion\":"+ventilacion+",\"Reset\":0}");
                    Toast.makeText(getBaseContext(), "Apagar el riego", Toast.LENGTH_SHORT).show();
                }

            }
        });
        SWVentilacion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    ventilacion = 1;
                    mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":0}");
                    Toast.makeText(getBaseContext(), "Prender el ventilacion", Toast.LENGTH_SHORT).show();
                } else {
                    ventilacion = 0;
                    mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":0}");
                    Toast.makeText(getBaseContext(), "Apagar el ventilacion", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnInfoSensores.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                i.putExtra("sensorHumedadAmbiente", sensorHumedadAmbiente);
                i.putExtra("sensorHumedadTierra", sensorHumedadTierra);
                i.putExtra("sensorNivelAgua", sensorNivelAgua);
                i.putExtra("sensorTemperaturaAmbiente", sensorTemperaturaAmbiente);

                //datos a persistir entre activity
                i.putExtra("led", led);
                i.putExtra("riego", riego);
                i.putExtra("ventilacion", ventilacion);
                i.putExtra("reset", reset);
                i.putExtra("editTempAmbientemin", editTempAmbientemin.getText().toString());
                i.putExtra("editTempAmbienteMax", editTempAmbienteMax.getText().toString());
                i.putExtra("editHumAmbMin", editHumAmbMin.getText().toString());
                i.putExtra("editHumAmbMax", editHumAmbMax.getText().toString());
                i.putExtra("edithummin",  edithummin.getText().toString());
                i.putExtra("editHumMax",editHumMax.getText().toString() );

                startActivity(i);
            }
        });


        btnSetear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                String mensaje1 = "{\"a\":" + editTempAmbientemin.getText() + ",\"b\":" + editTempAmbienteMax.getText() + ",\"c\":" + editHumAmbMin.getText() + ",\"d\":" + editHumAmbMax.getText() + ",\"e\":" + edithummin.getText() + ",\"f\":" + editHumMax.getText() +"}";
                mConnectedThread.write(mensaje1);
                // Toast.makeText(getBaseContext(), "Set Configuracion", Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), mensaje1, Toast.LENGTH_SHORT).show();

            }
        });

        //Creamos el Timer para pedir informacion de los sensores cada dos minutos
        Timer timer = new Timer();
        //Que actue cada 3000 milisegundos
        //Empezando des de el segundo 0
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //La función que queremos ejecutar
                //ejecutarThread();
                FuncionParaEsteHilo();
            }
        }, 0, 120000);


    }

    private void FuncionParaEsteHilo() {
        //Esta función es llamada des de dentro del Timer
        //Para no provocar errores ejecutamos el Accion
        //Dentro del mismo Hilo
        this.runOnUiThread(Accion);
    }

    private Runnable Accion = new Runnable() {
        public void run() {
            //Aquí va lo que queramos que haga
            mConnectedThread.write("Conexion");
        }
    };


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);

    }

    //chequeo que en android este disponible el bluetooth , si no pregunto activar
    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Creo la clase para la conexion Bluetooth
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Creo el Hilo correspondiente a la conexion Bluetooth
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {

                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[556];
            int bytes;

            // Esperando la recepcion de mensajes
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //leo del buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Envio mensaje via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Metodo de Escritura
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //convierto string en bytes
            try {
                mmOutStream.write(msgBuffer);                //escribo los bytes a traves del BT
            } catch (IOException e) {

                finish();

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Obtengo la direccion MAC y se la envio a DeviceListActivity via intent
        Intent i = getIntent();

        //Obtengo la direccion MAC de DeviceListActivty via EXTRA
        address = i.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        mensaje = i.getStringExtra(Main2Activity.Configurar);

        ConexionBlue();

        Bundle datos = this.getIntent().getExtras();
        led  = datos.getInt("led");

        riego =  datos.getInt("riego");
        reset =  datos.getInt("reset");
        ventilacion = datos.getInt("ventilacion");

        if( reset ==2){//modo automatico
            SWLed.setEnabled(false);
            SWModo.setChecked(true);
            SWVentilacion.setEnabled(false);
            SWRiego.setEnabled(false);
}
        else
        {//modo manual
            if(led==1){ SWLed.setChecked(true);
              }
            else SWLed.setChecked(false);
            if(riego==0)
                SWRiego.setChecked(false);
            else
            {SWRiego.setChecked(true);  }
            if(ventilacion==0)
                SWVentilacion.setChecked(false);
            else { SWVentilacion.setChecked(true);}

            SWModo.setChecked(false);
        }

        editTempAmbientemin.setText( datos.getString("editTempAmbientemin"));
        editTempAmbienteMax.setText(datos.getString("editTempAmbienteMax"));
        editHumAmbMin.setText( datos.getString("editHumAmbMin"));
        editHumAmbMax.setText(datos.getString("editHumAmbMax"));
        edithummin.setText( datos.getString("edithummin"));
        editHumMax.setText( datos.getString("editHumMax" ));
    }
    //para hacer la conexion con el bluetooth con el dispositivo seleccionado en la pagina de lista de dispositivos
    public void ConexionBlue() {

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("Conexion");

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterSenser();
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }
    //****************************************************************************
    //El siguiente codigo se agrego para el manejo de los sensores de android
    //****************************************************************************

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        registerSenser();
        String txt = "";

        // Cada sensor puede lanzar un thread que pase por aqui
        // Para asegurarnos ante los accesos simult�neos sincronizamos esto

        synchronized (this) {
            Log.d("sensor", event.sensor.getName());

            switch (event.sensor.getType()) {
                case Sensor.TYPE_PROXIMITY:

                    if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                        //near
                        Toast.makeText(getApplicationContext(), "Cerca", Toast.LENGTH_SHORT).show();
                        led = 1;

                        mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":"+reset+"}");
                    } else {
                    }


                    break;

                case Sensor.TYPE_LIGHT:
                    float[] values1 = event.values;
                    if ((Math.abs(values1[0]) < 4)) {
                        txt += "Luminosidad\n";
                        txt += event.values[0] + " Lux \n";
                        Toast.makeText(getBaseContext(), txt, Toast.LENGTH_SHORT).show();
                        Log.i("sensor", "TYPE_LIGHT running");


                        led = 1;

                        mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":"+reset+"}");
                    }

                    break;
                case Sensor.TYPE_ACCELEROMETER:

                    float[] values = event.values;

                    if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC)) {
                        Log.i("sensor", "running");
                        led = 1;
                        mConnectedThread.write("{\"Led\":" + led + ",\"Riego\":" + riego + ",\"Ventilacion\":" + ventilacion + ",\"Reset\":"+reset+"}");
                        Toast.makeText(getBaseContext(), "shake prendo led", Toast.LENGTH_SHORT).show();
                    }


                    break;
            }
        }

    }

    private void registerSenser() {
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);

        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);

        Log.i("sensor", "register");
    }

    private void unregisterSenser() {
        sensor.unregisterListener(this);
        Log.i("sensor", "unregister");
    }

}

