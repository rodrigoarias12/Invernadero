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

    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // String for MAC address
    private static String address = null;

    public static String Configurar= "Mensaje";

    private static String mensaje = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btnVolver = (Button) findViewById(R.id.btnVolver);
        Intent intent = getIntent();


         sensorHumedadAmbiente = (TextView) findViewById(R.id.txtHumedadAmbiente);
         sensorHumedadTierra = (TextView) findViewById(R.id.txtHumedadTierra);
         sensorNivelAgua = (TextView) findViewById(R.id.txtNivelAgua);
         sensorTemperaturaAmbiente = (TextView) findViewById(R.id.txtTemperaturaAmbiente);

        //cargar los valores de los sensores
        Intent intent1 = getIntent();

        //Get the MAC address from the MainActivity via EXTRA
        sensorHumedadAmbiente.setText(intent1.getStringExtra("sensorHumedadAmbiente"));
        sensorHumedadTierra.setText(intent1.getStringExtra("sensorHumedadTierra"));
        sensorNivelAgua.setText(intent1.getStringExtra("sensorNivelAgua"));
        sensorTemperaturaAmbiente.setText(intent1.getStringExtra("sensorTemperaturaAmbiente"));
      /*  btnSetear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Main2Activity.this, MainActivity.class);

                mensaje= "envio";
                i.putExtra("editTempAmbienteMax",editTempAmbienteMax.getText().toString());
                i.putExtra("editTempAmbientemin",editTempAmbientemin.getText().toString());
                i.putExtra("editHumAmbMin",editHumAmbMin.getText().toString());
                i.putExtra("editHumAmbMax",editHumAmbMax.getText().toString());
                i.putExtra("edithummin",edithummin.getText().toString());
                i.putExtra("editHumMax",editHumMax.getText().toString());

                i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                i.putExtra(Configurar, mensaje);
                startActivity(i);
                Toast.makeText(getBaseContext(), "Set Configuracion", Toast.LENGTH_SHORT).show();
            }
        });*/

       btnVolver.setOnClickListener(new View.OnClickListener(){
           public void onClick(View v) {
                Intent i = new Intent(Main2Activity.this, MainActivity.class);
               i.putExtra(EXTRA_DEVICE_ADDRESS, address);
               startActivity(i);
           }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the MainActivity via EXTRA
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        Log.i("main2", "adress : " + address);
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

}
