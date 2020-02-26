package Pubsub;
import buses.Topic;
import buses.Value;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Broker implements Node,Runnable, Serializable {
    private static final long serialVersionUID= -7289435099363431489L;

    public static void main(String[] args)  {
      ArrayList<Broker> br=readBrokers.getData();//1.kalite i readbrokers kai epistrefi ena pinaka me tous brokers
          br.get(0).updateNodes();//2.kalo tin update nodes gia ton broker p vriskete stin proti thesi tis listas p pirame
                                    //o broker pou vriskete stin proti thesi tou arraylist einai o broker pou tha treksi sto ekastote mixanima
          br.get(0).init();//6.kaleite i init tou broker

    }
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




    public void setConnection(Socket conn){
        this.connection=conn;
    }//11.vazoume tin sindesi p kaname ston broker

    public void pull(Topic topic){
        Value tmp=null;//42.arxikopio mia metavliti value null;
        try {
            while (true) {
                    for (Topic br : map.keySet()) {//44.diatrexo ton map
                        if ((br.getBusLine().equals(topic.getBusLine())) && (tmp != map.get(br)))//44.an to busline tou value isoute me to busline tou topic kai to tmp den isoute me to value me key br
                        {
                            if(out==null)disconect();//45.an i sindesi eklise kalite i disconect
                            out.writeObject(map.get(br));//46.stelno ta stoixeia tou leoforiou me tin topothesia
                            out.flush();
                            tmp = map.get(br);//47.kano to tmp iso me to value p estila p to soste na min stelni sinexia to idio leoforio

                        }
                    }
                }

        } catch(IOException e){
            e.printStackTrace();
        }finally {
            disconect();
        }

    }
    public void sendBrokers(){
        try {

            out.writeObject(brokers);//19.stelno tin lista me tous brokers
            out.flush();
            try {
                brokers_info.setResponsibilityLine((Map<Integer, ArrayList<Topic>>)in.readObject());//20.o publisher mas stelni tin tis grammes g tis opies kathe broker ine ipefthinos
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
            disconect();//21.kalite i disconect
        }

    }
    @Override
    public void init() {
        try {
            server=new ServerSocket(getPort());//7.anigo to server socket tou broker

            while(true){
                connect();//8.kalo tin connect
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void connect() {

        try {

            while(true){
                connection=server.accept();//9.apodexomaste tis sindesiz
                setConnection(connection);//10.kathe fora p tha sindeete kapios me ton broker kalite i setconnection
                Thread thr=new Thread(new Broker(getId(),getPort(),getIp(),connection));//12.dimiourgoume nima gia kath ena sindesi me ton broker
                thr.start();//13.ksekinai to nima tis sindesis
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public synchronized void run() {
        try {

            out=new ObjectOutputStream(connection.getOutputStream());//14.anigo revma eksodou
            in=new ObjectInputStream(connection.getInputStream());//15.anigo revma eisodou

            int id=in.readInt();//16..diavazo ton arithmo g na mporo na ksexoriso tous publishers apo tous subscirbers

            if((id==4)&&(getPort()==5339)){//17.an o broker ine o master diladi o broker me port 5339 kai diavasi ton arithmo 4
                sendBrokers();//18.an elave tin timi 4 simeni oti enas publisher theli na lavi tin lista me tous broker
            }
            if (id == 1) {//22.an estile 1 simeni oti ine publisher pou tha mas stili tis grammes
                Boolean flag=false;//23.arxikopioume ena flag iso me false

                try {
                    this.topic = (Topic) in.readObject();//24.diavazo topic
                    Value value = (Value) in.readObject();//25.diavazo value

                    for(Topic t:map.keySet()){//26.diatrexo ton xarti me ta topic k value
                        if((t.getBusLine().equals(topic.getBusLine()))) {//27.vrisko to topic p exi to idio arithmo busline me afto p mas estile o publisher
                            map.replace(t,value);//28.antikathisto tin palia timi me tin kenourgia g na exoume panta tin teleftea
                            visualiseData(t,map.get(t));//29.tiponoume kathe fora afta p pernoume
                            flag=true;//30.kanoume tin metavliti true
                        }
                        if(flag)//31.a gini true vgeni
                            break;

                    }
                    if(!flag) {//32.an dn iparxi idi timi g auto topic
                        map.put(topic, value);//33.to prosthetoume
                        visualiseData(topic,map.get(topic));//34.tiponoume to value
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            else if(id==2) {//35.an diavasame 2 simeni oti ine subscriber

                out.writeObject(brokers_info.getBroker());//36.stelno tin lista ton brokers
                out.flush();
                out.writeObject(brokers_info.getResponsibilityLine());//37.stelno ton xarti g to pios broker einai ipefthinos g kathe grammi
                out.flush();
            }else if(id==3){//39.an diavaso 3 simeni oti o subscriber sindethike pleon ston sosto broker

                try {
                    topic=(Topic)in.readObject();//40.diavazo to topic p endiafereteo subscriber

                    pull(topic);//41.kalite i pull me orisma to topic p estile o subscriber

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

    @Override
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
    @Override
    public void updateNodes() {
        brokers_info=new info();//3.arxikopio ena antikimeno tipou info gia ton ekastote broker
        brokers=readBrokers.getData();//4.arxikopio ton arraylist tou broker me vasi to txt me tous broker
        brokers_info.setBroker(brokers);
    }

}
