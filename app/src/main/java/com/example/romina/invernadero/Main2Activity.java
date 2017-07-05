package com.example.romina.invernadero;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main2Activity extends Activity {

    Button btnVolver;
    TextView  sensorNivelAgua,sensorHumedadTierra,sensorHumedadAmbiente,sensorTemperaturaAmbiente;

    // EXTRA es un string para enviar a mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // String que contiene la direccion MAC
    private static String address = null;

    public static String Configurar= "Mensaje";

    private static String mensaje = null;

    //datos e enviar
    public int led , riego , ventilacion , reset ;
    public String editTempAmbientemin ,editTempAmbienteMax,editHumAmbMin,editHumAmbMax,edithummin,editHumMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btnVolver = (Button) findViewById(R.id.btnVolver);



         sensorHumedadAmbiente = (TextView) findViewById(R.id.txtHumedadAmbiente);
         sensorHumedadTierra = (TextView) findViewById(R.id.txtHumedadTierra);
         sensorNivelAgua = (TextView) findViewById(R.id.txtNivelAgua);
         sensorTemperaturaAmbiente = (TextView) findViewById(R.id.txtTemperaturaAmbiente);

        //cargar los valores de los sensores
        Intent intent1 = getIntent();

        //Obtengo la direccion MAC de MainActivity via EXTRA
        sensorHumedadAmbiente.setText(intent1.getStringExtra("sensorHumedadAmbiente"));
        sensorHumedadTierra.setText(intent1.getStringExtra("sensorHumedadTierra"));
        sensorNivelAgua.setText(intent1.getStringExtra("sensorNivelAgua"));
        sensorTemperaturaAmbiente.setText(intent1.getStringExtra("sensorTemperaturaAmbiente"));

       btnVolver.setOnClickListener(new View.OnClickListener(){
           public void onClick(View v) {
                Intent i = new Intent(Main2Activity.this, MainActivity.class);
               i.putExtra(EXTRA_DEVICE_ADDRESS, address);


               i.putExtra("led", led);
               i.putExtra("riego", riego);
               i.putExtra("ventilacion", ventilacion);
               i.putExtra("reset", reset);
               i.putExtra("editTempAmbientemin", editTempAmbientemin);
               i.putExtra("editTempAmbienteMax", editTempAmbienteMax);
               i.putExtra("editHumAmbMin", editHumAmbMin);
               i.putExtra("editHumAmbMax", editHumAmbMax);
               i.putExtra("edithummin",  edithummin);
               i.putExtra("editHumMax",editHumMax );



               startActivity(i);
           }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //Obtengo la direccion MAC de DeviceListActivity via intent
        Intent i = getIntent();

        //Obtengo la direccion MAC de MainActivity via EXTRA
        address = i.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        led  = i.getIntExtra("led",0);
        riego =  i.getIntExtra("riego",0);
        ventilacion = i.getIntExtra("ventilacion",0);
        reset = i.getIntExtra("reset",0);
        editTempAmbientemin = i.getStringExtra("editTempAmbientemin");
        editTempAmbienteMax = i.getStringExtra("editTempAmbienteMax");
        editHumAmbMin =  i.getStringExtra("editHumAmbMin");
        editHumAmbMax = i.getStringExtra("editHumAmbMax");
        edithummin = i.getStringExtra("edithummin");
        editHumMax = i.getStringExtra("editHumMax" );

        Log.i("main2", "adress : " + editTempAmbientemin + editTempAmbienteMax+editHumAmbMin+editHumAmbMax);

        //Creo un dispositivo y seteo la direccion mac
        Log.i("main2", "adress : " + address);
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

}
