package com.example.mymap;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import Pubsub.Broker;
import buses.Topic;
import buses.Value;


import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private boolean stoploop;
    private Sub sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button button2 = findViewById(R.id.button2);
        Button button = findViewById(R.id.button);
        Button button3=findViewById(R.id.button3);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
    }


    private void connect() {
        EditText busline = findViewById(R.id.editText);
        String bus = busline.getText().toString();
        stoploop=true;

        if (bus == null) {
            Toast.makeText(this, "Parakalo dose ton arithmo tis grammis pou se endiaferi!", Toast.LENGTH_SHORT).show();
        } else {
             sub = new Sub(mMap, this, stoploop);
            sub.execute(bus);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                connect();
                break;
            case R.id.button2:
                stoploop=false;
                sub.onPostExecute(null);
                break;
            case R.id.button3:
                sub.onPostExecute(null);
                sub.cancel(true);
                break;


        }
    }


    public class Sub extends AsyncTask<String, Value, Value> {
        GoogleMap map;
        Context mapsActivity;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket socket = null;
        Value bus;
        Map<Integer, ArrayList<Topic>> lines = null;
        ArrayList<Broker> brokersList = null;
        boolean loop;
        String busline;
        public Sub(GoogleMap mMap, Context mapsActivity, boolean loop) {
            map = mMap;
            this.mapsActivity = mapsActivity;

        }


        @Override
        protected Value doInBackground(String... strings) {
            System.out.println("dgfhjkl");
            busline=strings[0];
            System.out.println(busline);
            if(socket!=null)
                disconnect();
            try {
                socket = new Socket("192.168.2.8", 5339);//2.anoigi sindesi me ton master broker
                out = new ObjectOutputStream(socket.getOutputStream());//3.anigo revma eksodou
                in = new ObjectInputStream(socket.getInputStream());//4.anigo revma isodou
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println(socket.getInetAddress().getHostAddress());

            try {
                out.writeInt(2);//6.grafi 2 gia na kseri o broker oti sindethike broker
                out.flush();
                brokersList = (ArrayList<Broker>) in.readObject();//9.diavazi in lista me tous brokers p estile o master broker

                lines = (Map<Integer, ArrayList<Topic>>) in.readObject();//10.diavazi ton map me ta id ton broker k tis grammes p ine ipefthini

            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }


            disconnect();
            if (lines.size() > 0) {
                for (int i : lines.keySet()) {
                    for (int j = 0; j < lines.get(i).size(); j++) {
                        if (busline.equals(lines.get(i).get(j).getBusLine())) {
                            try {
                                socket = new Socket(brokersList.get(i).getIp(), brokersList.get(i).getPort());//20.anigo sindesi me ton broker p xriazomaste
                                out = new ObjectOutputStream(socket.getOutputStream());//21.anigo revma eksodou
                                in = new ObjectInputStream(socket.getInputStream());//22.anigo revma isodou
                                out.writeInt(3);//23.stelno to 3 gia na kseri o broker oti sindethike o subscriber ston broker p ton endiaferi
                                out.flush();
                                out.writeObject(lines.get(i).get(j));//24.stelno to topic p me endiaferi
                                out.flush();
                                while(stoploop) {
                                    if(socket!=null)
                                    bus = (Value) in.readObject();
                                    publishProgress(bus);
                                }
                                return bus;

                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Value... bus) {
            super.onProgressUpdate(bus);
            Value buss = bus[0];

                if (buss != null) {

                    double x = buss.getLatitude();
                    double y = buss.getLongitude();

                    map.clear();

                    LatLng latlong = new LatLng(x, y);
                    MarkerOptions options = new MarkerOptions()
                            .position(latlong)
                            .title(buss.getBus().getBuslineid() + buss.getBus().getInfo())
                            .snippet("LineNumber:" + buss.getBus().getLineName() + "\n" + buss.getBus().getLineName() + "\nRouteCode:" + buss.getBus().getRouteCode() + "\nVehicleId:" + buss.getVehicleID());

                    map.addMarker(options);
                    map.moveCamera(CameraUpdateFactory.newLatLng(latlong));
                    map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                } else {
                    Toast.makeText(mapsActivity, "Den iparxei akoma", Toast.LENGTH_SHORT).show();
                }

        }

        @Override
        protected void onPostExecute(Value result) {
            super.onPostExecute(result);
            disconnect();

        }

        private void disconnect() {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}




