package com.example.monitoramento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static int REQUEST_ENABLE_BT = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;
    private boolean conexao =false;
    private static String MAC = null;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice device = null;
    BluetoothSocket socket = null;
    Button btnConexao;
    UUID uuid =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler mHandler;
    StringBuilder dadosBluetooth = new StringBuilder();

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
                    try{
                        socket.close();
                        conexao = false;
                        Log.e("REC", "Device Disconected");
                        Toast.makeText(getApplicationContext(), "Bluetooth Desconectado!" , Toast.LENGTH_LONG).show();
                        btnConexao.setText("Conectar");
                    }catch (IOException e){
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro: " + e, Toast.LENGTH_LONG).show();
                    }
                }else{
                    //Conectar
                    Intent abrelista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abrelista, SOLICITA_CONEXAO);

                }
            }
        });

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.e("REC", "ON HANDLE MESSAGE");
                if(msg.what == MESSAGE_READ) {
                    String recebido = (String) msg.obj;

                    dadosBluetooth.append(recebido);

                    Log.e("REC","Dados: "+ dadosBluetooth.toString());

                    if(dadosBluetooth.length() > 0){
                        Log.e("REC","Maior que 0");
                    }
                    else{
                        Log.e("REC","Menor que 0");
                    }



                }
                dadosBluetooth.delete(0,dadosBluetooth.length());
            }
        };

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

                        conexao = true;

                        ConnectThread connectThread = new ConnectThread(socket);
                        try{
                            connectThread.start();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Erro ao iniciar a thread", Toast.LENGTH_LONG).show();
                            Log.e("REC", "Error to start thread: " + e.toString());
                        }



                        Toast.makeText(getApplicationContext(), "Voce Foi Conectado!", Toast.LENGTH_LONG).show();
                        Log.e("REC", "Device Connected");

                        btnConexao.setText("Desconectar");

                    }catch (IOException  erro){
                        conexao = false;
                        Toast.makeText(getApplicationContext(), "Erro ao conectar! : " + erro, Toast.LENGTH_LONG).show();
                        Log.e("REC", "Error on Connect: " + erro);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o MAC ", Toast.LENGTH_LONG).show();
                    Log.e("REC", "Error on get MAC");
                }
        }

    }

    private class ConnectThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;


        public ConnectThread(BluetoothSocket socket1) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.

            Log.e("REC", "CREATING THREAD");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmpIn = socket1.getInputStream();
                tmpOut = socket1.getOutputStream();
            } catch (IOException e) {
                Log.e("TAG", "Socket's create() method failed", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            Log.e("REC", "RUN");
            byte[] buffer = new byte[1];
            int bytes;

            while (true) {
                Log.e("REC", "WHILE");
                try {
                    bytes = mmInStream.read(buffer);
                    Log.e("REC", "Bytes" + bytes);

                    String dadosbt = new String(buffer,0,bytes);
                    Log.e("REC", "DATA READ");
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, dadosbt).sendToTarget();


                } catch (IOException e) {
                    Log.e("REC", "FAIL TO RECIEVE DATA");
                    break;
                }
            }


        }

        // Closes the client socket and causes the thread to finish.

    }

}

