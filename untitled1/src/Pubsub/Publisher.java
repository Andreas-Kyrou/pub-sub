package Pubsub;

import buses.Topic;
import buses.Value;
import buses.readData;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;

public class Publisher implements Node {

    public static void main(String[] args) {
        new Publisher(1).init();//1.dimiourgo ena antikeimeno k kalo tin init


    }

    public  Topic topic;
    private Socket socket=null;
    private ObjectInputStream in=null;
    private ObjectOutputStream out=null;
    public static ArrayList<Broker> brokers;
    private Broker broker;
    private int id;
    private static ArrayList<Value> bus_list=new ArrayList<Value>();
    private static Map<Topic,Value> b=new ConcurrentHashMap<>();
    private static Map<Integer, ArrayList<Topic>> responsibilityLine=new ConcurrentHashMap<>();//o xartis me to id tou broker k tis grames p ine ipefthinos

    public Publisher(int id){

        this.id=id;
    }

    public void getBrokerList(){
        try {
            socket=new Socket(InetAddress.getLocalHost().getHostAddress(),5339);//3.sindeome me ton master broker
            out=new ObjectOutputStream(socket.getOutputStream());//4.dimiourgo revma eksodou
            in=new ObjectInputStream(socket.getInputStream());//5.dimiourgo revma isodou
            out.writeInt(4);//6.stelno tin timi 4
            out.flush();

            brokers=(ArrayList<Broker>)in.readObject();//7.perno tin lista me tous energous broker
             bus_list= readData.getData();//8.kalo tin getData p diavazo ta arxia routecodes k buslinecode
            for(Value value:bus_list) {//9.diatrexo tin lista me ta leoforia
                topic=new Topic(value.getBus().getBuslineid());//10.gia kathe leoforio dimiourgo ena topic me tn arithmo grammis
                broker=hashTopic(topic);//11.kalite i hash topic p epistrefi ton broker ston opio tha prp na sindethi g to topic p perasame os parametro
                if (responsibilityLine.get(broker.getId())==null){//20.an o xartis stin thesi id ine kenos
                    responsibilityLine.put(broker.getId(),new ArrayList<>(Arrays.asList(topic)));//21.apothikevi to id san key k kani mia nea lista me to topic os value

                }else{
                    responsibilityLine.get(broker.getId()).add(topic);//22.diaforetika vazzi sto value me key to id stin lista totopic
                }
                b.put(topic,value);//23.apothikevoume k se ena xarti to topic me to antistixo t value
            }
            out.writeObject(responsibilityLine);//24.stelno ton xarti me tis grammes k tous broker p ine ipefthini
            out.flush();
            disconect();//kalite disconnect
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            disconect();//7.aposindeome ap ton default
        }
    }

    public Broker hashTopic(Topic topic){
        String busline = topic.getBusLine();//12.vazo to busline se mia metavliti

        String sha1 = "";
//13.kano hash to busline
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(busline.getBytes("utf8"));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e){
            e.printStackTrace();
        }
        int num= abs(new BigInteger(sha1, 16).intValue());//14.perno tin apoliti timi tis timis p pirame apo to hash
        for(int i=0;i<brokers.size();i++){//15.diatrexo tin lista me tous brokers
            if((abs(brokers.get(i).getKey())>=num)){//17.perno tin apoliti timi tou klidiou tou broker an einai megaliteri i isi apo tin timi touhashtopic

                return brokers.get(i);//18.epistrefo ton broker me to megalitero i iso key
            }
        }

        return brokers.get(num %brokers.size());//19.diaforetika kano mod

    }

    public void push(Topic topic, Value value){

        try {

            out.writeObject(topic);//28.stelno to topic
            out.writeObject(value);//29.stelno to value

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            disconect();//30.kalo tin disconect
        }

    }


    @Override
    public void init() {
        getBrokerList();//2.kalo tin methodo p tha m epistrepsi tin lista me tous brokers

        File f = null;
        BufferedReader reader = null;
        String line;

        try {// to try gia na vri to txt pou periexi ton arithmo ton epeksergaston kai tis diergasies
            f = new File(readPos.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"\\busPositionsNew.txt");
        } catch (NullPointerException e) {
            System.err.println("File not found");//tiponi ena minima sfalmatos oti den mporese na vri to arxio
        }
        try {
            reader = new BufferedReader(new FileReader(f));//dokimazi na aniksi to arxeio txt me onoma p dosame pio pano
        } catch (FileNotFoundException e) {
            System.err.println("Error opening file!");//kai an den mporesi na to aniksi tiponi minima sfalmatos
        }

        try {
            line = reader.readLine();//25.diavazo tinproti grammi tou txt
            while (line != null) {//26.oso den ine keno
                String[] info = line.split(",");

                for(Topic t:b.keySet()){//27.diatrexo ton xarti me ta topic k value
                    if(b.get(t).getBus().getRouteCode().equals(info[1])){//28.kathe fora p diavazo pliroforia p sindeete me to topic
                        b.get(t).setTimestampOfBusPosition(info[5]);//29.vazo sto leoforio to time stamp
                        b.get(t).setLongitude(Double.parseDouble(info[4]));//30.vaso to longitude
                        b.get(t).setLatitude(Double.parseDouble(info[3]));//31.vazoto latitud
                        b.get(t).setVehicleID(info[2]);//32.vazo to vehicle id
                    broker = hashTopic(t);//33.kalite i methodo hashtopic p tha kani hash to busline
                     disconect();
                    connect();//34.kalo tin connect g na sindetho ston sosto broker
                        push(t, b.get(t));//34.kalo tin push g na stili to topic me tin lista ton theseon ton leoforion
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                }
                line = reader.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void connect() {
        try {
                socket = new Socket(broker.ip, broker.port);//22.kano sindesi me ton sosto broker
            out=new ObjectOutputStream(socket.getOutputStream());//23.anigo revma eksodou
            in=new ObjectInputStream(socket.getInputStream());//24.anigok revma isodou
            out.writeInt(1);//25.stelno 1 g na kseri o broker oti ine publisher
            out.flush();//26.to stelno
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconect() {
        try {
            in.close();//31.klino to reuma isodou
            out.close();//32.klino to reuma eksodou
            socket.close();//33.klino tin sindesi
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNodes() {

    }
}
