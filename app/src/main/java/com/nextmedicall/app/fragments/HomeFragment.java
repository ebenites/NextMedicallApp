package com.nextmedicall.app.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.nextmedicall.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 100;

    private static final int REQUEST_PERMISSION_BT = 100;

    private static final UUID UDDI_MODULE_BT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;

    private Button discoverDevicesButton;

    private Button queryPairedButton;

    private View dashboardContainer;

    private ProgressBar progressBar;

    private TextView tempValue;

    private TextView bmpValue;

    private TextView spo2Value;

    private Button closeButton;

    private Button sendDataButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(getString(R.string.app_name) + " | " + getString(R.string.title_home));
        View view  = inflater.inflate(R.layout.fragment_home, container, false);

        discoverDevicesButton = view.findViewById(R.id.discover_devices_button);
        discoverDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
            }
        });

        queryPairedButton = view.findViewById(R.id.query_paired_devices_button);
        queryPairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryPairedDevices();
            }
        });

        dashboardContainer = view.findViewById(R.id.dashboard_container);

        progressBar = view.findViewById(R.id.progressbar);

        tempValue = view.findViewById(R.id.temp_value);

        bmpValue = view.findViewById(R.id.bmp_value);

        spo2Value = view.findViewById(R.id.sp_value);

        closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeConecction();
            }
        });

        sendDataButton = view.findViewById(R.id.send_data_button);
        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        startBluetoothAdapter();

        registerBroadcastReceiver();

        return view;
    }

    private void startBluetoothAdapter() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toasty.error(getContext(), "El dispositivo no soporta Bluetooth", Toasty.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        queryPairedButton.setEnabled(true);

    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        getContext().registerReceiver(broadcastReceiver, intentFilter);

        discoverDevicesButton.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if(socket != null) socket.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        getContext().unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
                    discoverDevicesButton.setEnabled(false);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
                    discoverDevicesButton.setEnabled(true);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    Log.d(TAG, "onReceive: ACTION_FOUND");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d(TAG, "Name: " + device.getName() + ", HardwareAddress:  " + device.getAddress());

                    boolean exists = false;
                    for (BluetoothDevice d : devicesList) {
                        if(d.getAddress() != null && d.getAddress().equals(device.getAddress())) {
                            exists = true;
                        }
                    }

                    if(!exists) {
                        devicesList.add(device);
                        devicesLabels.add("\t" + (device.getName() != null ? device.getName() : device.getAddress()));
                        if (devicesAdapter != null) devicesAdapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    private final List<BluetoothDevice> devicesList = new ArrayList<>();

    private final List<String> devicesLabels = new ArrayList<>();

    private ArrayAdapter<String> devicesAdapter;

    private void discoverDevices() {

        if (!bluetoothAdapter.isEnabled()) {
            Toasty.error(getContext(), "El dispositivo Bluetooth no se encuentra activo", Toasty.LENGTH_LONG).show();
            return;
        }
        if(bluetoothAdapter.isDiscovering()) {
            Toasty.warning(getContext(), "Ya se está buscando dispositivos", Toasty.LENGTH_LONG).show();
            return;
        }
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_PERMISSION_BT);
            return;
        }
        if(!bluetoothAdapter.startDiscovery()) {
            Toasty.error(getContext(), "Error al iniciar la detección de dispositivos", Toasty.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Buscando dispositivos");

        devicesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, devicesLabels);
        builder.setAdapter(devicesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!bluetoothAdapter.cancelDiscovery()) {
                    Toasty.error(getContext(), "Error al cancelar la detección de dispositivos", Toasty.LENGTH_LONG).show();
                    return;
                }

                Toasty.success(getContext(),"Dispositivo seleccionado " + devicesLabels.get(which), Toast.LENGTH_LONG).show();

                connect(devicesList.get(which));    // Connect

            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(!bluetoothAdapter.cancelDiscovery()) {
                    Toasty.error(getContext(), "Error al cancelar la detección de dispositivos", Toasty.LENGTH_LONG).show();
                    return;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void queryPairedDevices() {

        if (!bluetoothAdapter.isEnabled()) {
            Toasty.error(getContext(), "El dispositivo Bluetooth no se encuentra activo", Toasty.LENGTH_LONG).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (!pairedDevices.isEmpty()) {

            final List<BluetoothDevice> devices = new ArrayList<>();

            final List<String> labels = new ArrayList<>();

            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.d(TAG, "Name: " + device.getName() + ", HardwareAddress:  " + device.getAddress());
                devices.add(device);
                labels.add("\t" + (device.getName()!=null?device.getName():device.getAddress()));
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Mis dispositivos");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, labels);
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Toasty.success(getContext(),"Dispositivo seleccionado " + labels.get(which), Toast.LENGTH_LONG).show();

                    connect(devices.get(which));    // Connect

                }
            });

            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if(!bluetoothAdapter.cancelDiscovery()) {
                        Toasty.error(getContext(), "Error al cancelar la detección de dispositivos", Toasty.LENGTH_LONG).show();
                        return;
                    }
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            Toasty.warning(getContext(), "No tienes dispositivos emparejados", Toasty.LENGTH_LONG).show();
        }

    }

    private BluetoothSocket socket;

    private void connect(@NonNull BluetoothDevice device) {
        try {

            progressBar.setVisibility(View.VISIBLE);

            dashboardContainer.setVisibility(View.GONE);

            if (!bluetoothAdapter.isEnabled()) {
                Toasty.error(getContext(), "El dispositivo Bluetooth no se encuentra activo", Toasty.LENGTH_LONG).show();
                return;
            }

            // Si ya está conectado a un dispositivo la cerramos e iniciamos otra nueva conexión
            if(socket != null && socket.isConnected()) {
                socket.close();
                Toasty.warning(getContext(), "Reconectando...", Toasty.LENGTH_LONG).show();
            }

//            BluetoothDevice device = btAdapter.getRemoteDevice("98:D3:32:21:02:F9");  // Get by MAC Address

            socket = device.createRfcommSocketToServiceRecord(UDDI_MODULE_BT);

            socket.connect();

            Toasty.success(getContext(), "Conexion realizada exitosamente", Toasty.LENGTH_SHORT).show();

            // Ahora puedes leer o escribir en el socket ...

            progressBar.setVisibility(View.GONE);

            dashboardContainer.setVisibility(View.VISIBLE);

            closeButton.setEnabled(true);

            new ConexionThread(socket).start();

        } catch (Exception e) {
            Toasty.error(getContext(), "Error en la creación del Socket: " + e.getMessage(), Toasty.LENGTH_LONG).show();
            try {
                if(socket != null) socket.close();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
        }

    }

    final int handlerState = 0; //Estado del manejador

    private final Handler messagegHandler = new Handler(){
        public void handleMessage(Message msg) {
            if (msg.what == handlerState) {
                String message = (String) msg.obj;
                Log.d(TAG, "Message: " + message);
                try {

//                    double temp = (new Random().nextDouble() * 20) + 10;
//                    double bmp = (new Random().nextDouble() * 100) + 1;
//                    double spo2 = (new Random().nextDouble() * 100) + 1;

                    String[] measures = message.split("\n");

                    for (int i = 0; i < measures.length; i++) {
                        String values[] = measures[i].split(",");
                        if(measures[i].startsWith("Temp")) {
                            double temp = Double.parseDouble(values[2].split("=")[1]);
                            tempValue.setText(new DecimalFormat("#.0").format(temp));
                        } else if(measures[i].startsWith("BPM")) {
                            double bpm = Double.parseDouble(values[2].split("=")[1]);
                            bmpValue.setText(new DecimalFormat("#.0").format(bpm));
                        } else if(measures[i].startsWith("SPO2")) {
                            double spo2 = Double.parseDouble(values[2].split("=")[1]);
                            spo2Value.setText(new DecimalFormat("#.0").format(spo2));
                        }
                    }

                    sendDataButton.setEnabled(true);

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toasty.error(getContext(), "Error en lectura de datos: " + e.getMessage(), Toasty.LENGTH_LONG).show();
                }
            }
        }

    };    //Handler es un control para mensajes

    private class ConexionThread extends Thread {

        private InputStream input;

        public ConexionThread(BluetoothSocket socket) {
            try {
                input = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                // Se mantiene en modo escucha para determinar el ingreso de datos
                try {
                    bytes = input.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    messagegHandler.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

    }

    private void closeConecction() {
        try {

            if (!bluetoothAdapter.isEnabled()) {
                Toasty.error(getContext(), "El dispositivo Bluetooth no se encuentra activo", Toasty.LENGTH_LONG).show();
//                return;
            }

            if(socket == null || !socket.isConnected()) {
                Toasty.error(getContext(), "No se encuentra conectado a ningún dispositivo", Toasty.LENGTH_LONG).show();
//                return;
            }

            if(socket != null) {
                socket.close();
            }

            closeButton.setEnabled(false);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private void sendData() {

        sendDataButton.setEnabled(false);

        final ProgressDialog dialog = ProgressDialog.show(getContext(), "", "Enviando información...", true);

//        final ProgressBar progressBar = new ProgressBar(getContext());
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        progressBar.setLayoutParams(lp);
//
//        final AlertDialog dialog = new AlertDialog.Builder(getContext())
//                .setView(progressBar)
//                .create();

        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                Toasty.success(getContext(), "Los datos enviados correctamente", Toasty.LENGTH_LONG).show();
            }
        }, 2000);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                startBluetoothAdapter();
            } else {
                Toasty.error(getContext(), "La app requiere el uso del Bluetooth", Toasty.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_BT) {
            if(permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    discoverDevices();
                } else {
                    Toasty.error(getContext(), "La app requiere el acceso a la ubicación para buscar dispositivos", Toasty.LENGTH_LONG).show();
                }
            }
        }
    }
}