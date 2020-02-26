package Pubsub;

import buses.Topic;
import buses.Value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Subscriber implements Node {public static void main(String[] args) {
    new Subscriber(1).init();//1.kalite i init
}
    private Map<Integer, ArrayList<Topic>> lines;
    private ArrayList<Broker> brokersList;
    Scanner keyboard = new Scanner(System.in);
    private String busline;
    private int id;
    Value bus;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public Subscriber(int id){
        this.id=id;
    }
    public void register(Broker brok, Topic topic) {

        disconect();//18.kanoume disconect apo ton master broker
        try {
            socket = new Socket(brok.getIp(), brok.getPort());//20.anigo sindesi me ton broker p xriazomaste
            out = new ObjectOutputStream(socket.getOutputStream());//21.anigo revma eksodou
            in = new ObjectInputStream(socket.getInputStream());//22.anigo revma isodou
            out.writeInt(3);//23.stelno to 3 gia na kseri o broker oti sindethike o subscriber ston broker p ton endiaferi
            out.flush();
            out.writeObject(topic);//24.stelno to topic p me endiaferi
            out.flush();
            while (true) {

                try {

                    bus = (Value) in.readObject();//28.diavazo to value p estile o broker
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                visualiseData(topic, bus);//29.tipono tis plirofories

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void visualiseData(Topic topic, Value value){

        System.out.println(value.getBus().getBuslineid());
        System.out.println(value.getBus().getLineNumber());
        System.out.println(value.getBus().getInfo());
        System.out.println(value.getBus().getLineName());
        System.out.println(value.getBus().getRouteCode());
        System.out.println(value.getVehicleID());
        System.out.println(value.getLongitude());
        System.out.println(value.getLatitude());
        System.out.println(value.getTimestampOfBusPosition()+"\n");
    }

    @Override
    public void init() {
        try {
            socket=new Socket(InetAddress.getLocalHost().getHostAddress(),5339);//2.anoigi sindesi me ton master broker
            out=new ObjectOutputStream(socket.getOutputStream());//3.anigo revma eksodou
            in=new ObjectInputStream(socket.getInputStream());//4.anigo revma isodou
            connect();//5.kalite i connect
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {
        try {
            out.writeInt(2);//6.grafi 2 gia na kseri o broker oti sindethike broker
            out.flush();
            System.out.println("Give me the busline:");//7.tipono minima g na dosi tin grammi p endiaferete o subscriber
            busline = keyboard.nextLine();//8.apothikevo tin timi p diavasa sto busline
            try {
                brokersList = (ArrayList<Broker>) in.readObject();//9.diavazi in lista me tous brokers p estile o master broker
                System.out.println(brokersList.size());
                lines = (Map<Integer, ArrayList<Topic>>) in.readObject();//10.diavazi ton map me ta id ton broker k tis grammes p ine ipefthini

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            find(busline);//11.kalite i find

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void find(String busline) {

        while (true) {
            if(lines.size()>0) {
                for (int i : lines.keySet()) {
                    for (int j = 0; j < lines.get(i).size(); j++) {
                        if (busline.equals(lines.get(i).get(j).getBusLine())) {
                            //System.out.println(brokersList.get(i).getPort() + " " + brokersList.get(i).getIp());
                            register(brokersList.get(i), lines.get(i).get(j));
                           System.out.println("Give num for busline:");
                            busline = keyboard.nextLine();
                            find(busline);
                        }
                    }
                }
            }
        }
    }
    @Override
    public void disconect() {
        try {
            in.close();//klino revma isodou
            out.close();//klino revma eksoou
            socket.close();//klino sindesi
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNodes() {

    }
}
