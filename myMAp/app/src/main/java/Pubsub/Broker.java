package Pubsub;

import android.os.Build;
import android.support.annotation.RequiresApi;

import buses.Topic;
import buses.Value;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Broker implements Runnable, Serializable {
    private static final long serialVersionUID= -7289435099363431489L;

    public Broker() {
        updateNodes();//2.kaleitai i updanodes
    }

   /* public static void main(String[] args) {
        String def_ip=null;
        int def_port=0;
        Broker.brokers_info=new info();//1.dimiourgite ena indo kino gia olous
        for(Broker br:readBrokers.getData()){//2.diatrexo tin lista p epistrefi i getData dld tin lista me ts brokers
            if(!br.getIp().equals("localhost")){
                def_ip=br.getIp();
                def_port=br.getPort();
            }else if(br.getPort()!=5339){

                br.acceptBroker(def_ip,def_port);
                br.updateNodes();//3.kalo tin updateNodes
                br.init();
            }else {
                br.updateNodes();//3.kalo tin updateNodes
                br.init();
            }
        }

    }*/
    private static Topic topic;

    private static ArrayList <Broker> brokers;

    private static Map<Topic, Value> map=new ConcurrentHashMap<>();
    private ServerSocket server=null;
    private Socket connection=null;
    private ObjectInputStream in=null;
    private ObjectOutputStream out=null;
    private static int key;
    private static info brokers_info;
    int id;
    int port;
    String ip;
    public Broker(int id,int port,String ip,Socket so){
        this.id=id;
        this.port=port;
        this.ip=ip;
        this.connection=so;
    }
    public Broker(int id,int port,String ip){
        this.id=id;
        this.port=port;
        this.ip=ip;

    }
    public void setKey(int key){
        this.key=key;
    }
    public int getKey() {
        calculateKeys();
        return this.key;
    }

    public void calculateKeys(){
        String broker_key = this.ip.concat(Integer.toString(this.port));

        String sha1 = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(broker_key.getBytes("utf8"));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e){
            e.printStackTrace();
        }
        setKey(new BigInteger(sha1, 16).intValue());

    }
    public int getPort(){
        return port;
    }
    public String getIp(){
        return ip;
    }

    public int getId(){
        return this.id;
    }



    public void notifyPublisher(String message){

    }
    public void setConnection(Socket conn){
        this.connection=conn;
    }

    public void pull(Topic topic){
        Value tmp=null;
        try {
            while (true) {
                if(map.size()>0) {
                    for (Topic br : map.keySet()) {//33.diatrexo ton map
                        if ((br.getBusLine().equals(topic.getBusLine())) && (tmp != map.get(br)))//35.an to busline tou value isoute me to busline tou topic
                        {
                      /*  out.writeUTF("");//36.stelno keno
                        out.flush();*/
                            out.writeObject(map.get(br));//37.stelno ta stoixeia ton leoforion me tin topothesia
                            out.flush();
                            tmp = map.get(br);

                        }
                    }
                }
            }
        /*
                out.writeUTF("end");//38.otan teliosi stelni end g na kseri oti teliosan i plirofories
                out.flush();*/
        } catch(IOException e){
            e.printStackTrace();
        }

    }
    public void sendBrokers(){
        try {

            //  connection=server.accept();//11.sindeome me ton broker i subscriber

            System.out.println(brokers);
            out.writeObject(brokers);//14.stelno tin lista me tous brokers
            out.flush();
            try {
                brokers_info.setResponsibilityLine((Map<Integer, ArrayList<Topic>>)in.readObject());
                System.out.println(brokers_info.getResponsibilityLine().get(0));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            disconect();//15.kalite i disconect
        }

    }

    public void init() {
        try {
            server=new ServerSocket(getPort());//8.anigo to server socket tou broker
           /* if(getPort()==5339){//9.an o broker ine o default diladi aftos me port 5339
                connect();
                sendBrokers();//10.kalitei sendbroker
            }*/
            while(true){
                connect();//16.kalo tin connect
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void connect() {

        try {

            while(true){
                connection=server.accept();//17.apodexomaste tis sindesiz
                setConnection(connection);
                Thread thr=new Thread(new Broker(getId(),getPort(),getIp(),connection));

                thr.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized void run() {
        try {

            out=new ObjectOutputStream(connection.getOutputStream());//19.anigo revma eksodou
            in=new ObjectInputStream(connection.getInputStream());//20.anigo revma eisodou

            int id=in.readInt();//21.diavazo ton arithmo g na mporo na ksexoriso tous brokers apo tous subscirbers

            if((id==0)&&(getPort()==5339)){

                String n_ip=in.readUTF();
                int n_po=in.readInt();
                int n_id=in.readInt();
                System.out.println(getPort()+" "+getIp());
                System.out.println(n_ip+" "+n_po);
                System.out.println(brokers);

                brokers.add(new Broker(n_id,n_po,n_ip));
                brokers_info.setBroker(brokers);
                disconect();
            }
            if((id==4)&&(getPort()==5339)){
                sendBrokers();
            }
            if (id == 1) {//22.an estile 1 simeni oti ine publisher
                Boolean flag=false;

                try {
                    this.topic = (Topic) in.readObject();//23.diavazo topic
                    Value value = (Value) in.readObject();//24.diavazo tin lista fnjme tis thesis ton leoforion

                    //  brokers_info.setTopics(this.id,this.topic);//25.stelno stin info to id k to topic g na to apothikefsi ston map
                    for(Topic t:map.keySet()){
                        if((t.getBusLine().equals(topic.getBusLine()))) {
                            map.replace(t,value);
                            visualiseData(t,map.get(t));
                            flag=true;
                        }
                        if(flag)
                            break;

                    }
                    if(!flag) {
                        map.put(topic, value);
                        visualiseData(topic,map.get(topic));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            else if(id==2) {//27.an diavasame 2 simeni oti ine subscriber
                System.out.println("kavlaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                out.writeObject(brokers_info.getBroker());//28.stelno tin lista ton brokers
                out.flush();

                out.writeObject(brokers_info.getResponsibilityLine());//29.stelno ton xarti g to pios broker einai ipefthinos g kathe grammi
                out.flush();
                //  disconect();//30.kalite i disconect
            }else if(id==3){//31.an diavaso 3 simeni oti o subscriber sindethike pleon ston sosto broker

                try {
                    topic=(Topic)in.readObject();

                    pull(topic);//32.kalite i pull

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            disconect();
        }
    }
    public void acceptBroker(String ip,int port){
        try {
            Socket socket=new Socket(ip,port);
            out=new ObjectOutputStream(socket.getOutputStream());//4.dimiourgo revma eksodou
            in=new ObjectInputStream(socket.getInputStream());//5.dimiourgo revma isodou

            out.writeInt(0);
            out.flush();
            out.writeUTF(InetAddress.getLocalHost().getHostAddress());
            out.writeInt(getPort());
            out.writeInt(getId());
            out.flush();
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disconect() {
        try {
            in.close();//33.klino revma isodou
            out.close();//34.klino revma eksodou
            connection.close();//35.klino tin sindesi

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
        System.out.println(value.getTimestampOfBusPosition());
        System.out.println();
    }
    public void updateNodes() {
        brokers=readBrokers.getData();//4.diavazi tous brokers p theloume apo to txt
        try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        brokers_info.setBroker(brokers);//5.ta stelno k ta apothikevo stin info
    }
    public void setIp(String ip){
        this.ip=ip;
    }
}
