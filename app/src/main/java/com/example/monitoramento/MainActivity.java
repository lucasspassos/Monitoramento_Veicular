package com.example.monitoramento;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static int REQUEST_ENABLE_BT = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private boolean conexao =false;
    private static String MAC = null;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice device = null;
    BluetoothSocket socket = null;
    Button btnConexao;
    UUID uuid = device.getUuids()[0].getUuid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConexao = (Button)findViewById(R.id.btnConexao);


        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    //Desconectar
                }else{
                    //Conectar
                    Intent abrelista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abrelista, SOLICITA_CONEXAO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SOLICITA_CONEXAO:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.MEC_ADDRESS);
                    //Toast.makeText(getApplicationContext(), "MAC: " + MAC, Toast.LENGTH_LONG).show();
                    device = bluetoothAdapter.getRemoteDevice(MAC);

                    try{

                        socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                        bluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        socket.connect();

                        Toast.makeText(getApplicationContext(), "Voce Foi Conectado!", Toast.LENGTH_LONG).show();

                    }catch (IOException  erro){
                        Toast.makeText(getApplicationContext(), "Erro ao conectar! : " + erro, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o MAC ", Toast.LENGTH_LONG).show();
                }
        }

    }
}