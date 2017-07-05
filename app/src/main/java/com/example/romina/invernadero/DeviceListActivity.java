package com.example.romina.invernadero;
import android.app.Activity;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Set;


public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    TextView textView1;

    // EXTRA es un string para enviar a  mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Bluetooth
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //para ver si esta activo el bluetooth
        checkBTState();

        textView1 = (TextView) findViewById(R.id.connecting);
        textView1.setTextSize(40);
        textView1.setText(" ");

        // Inicializo el Array adapter para dispositivos vinculados
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Muestro la lita de dispositivos
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
       //para cada elemento de la lista le voy a dar un onclick evento que se dispara
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Obtengo Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();


        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();


        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//Muestro el nombre
            for (BluetoothDevice device : pairedDevices) {
                //cargo en la lista el nombre de dispositivos vinculados y sus direcciones mac
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    // Estoy escuchando, en cualquiera que haga click para enlazar
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            textView1.setText("Conectando...");
            // obtengo la direccion mac del dispositivo, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            //hacer un intent a la actividad principal con la direccion mac del dispositivo que seleccione
            Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);


            i.putExtra("editTempAmbientemin", "12");
            i.putExtra("editTempAmbienteMax", "30");
            i.putExtra("editHumAmbMin", "40");
            i.putExtra("editHumAmbMax", "50");
            i.putExtra("edithummin",  "40");
            i.putExtra("editHumMax","50" );


            startActivity(i);
        }
    };
    //chekeo conexion con el bluetooth
    //Si esta apagado envio mensaje por pantalla para activar
    private void checkBTState() {
        // Chequeo si el dispositivo tiene Bluetooth y si esta prendido
        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {

                //Con esto hago que pregunte
                //Puedo activar el Bluetooth ? En caso de que este apagado
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}