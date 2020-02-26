package Pubsub;

import buses.Topic;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class info implements Serializable {
    private static final long serialVersionUID=8527440485562111712L;
    private static  ArrayList<Broker> broker ;//lista me tous brokers
    private static Map<Integer, ArrayList<Topic>> responsibilityLine=new ConcurrentHashMap<>();//o xartis me to id tou broker k tis grames p ine ipefthinos
    public  info(){
        super();
    }
    public  info(ArrayList<Broker>br,Map<Integer, ArrayList<Topic>> r){
        this.broker=br;
        this.responsibilityLine=r;
    }
    public static void setTopics(int id,Topic topic){
        if (responsibilityLine.get(id)==null){//an o xartis stin thesi id
            responsibilityLine.put(id,new ArrayList<Topic>(Arrays.asList(topic)));//apothikevi to id san key k kani mia nea lista me to topic os value
        }else{
            responsibilityLine.get(id).add(topic);//diaforetika vazzi sto value me key to id stin lista totopic
        }
    }

    public void setBroker(ArrayList<Broker> broker) {
        this.broker = broker;
        for(Broker br:broker)
            if(br.getIp().equals("localhost")){
                try {

                    br.setIp(InetAddress.getLocalHost().getHostAddress());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

    }

    public static ArrayList<Broker> getBroker() {
        return broker;
    }

    public static Map<Integer, ArrayList<Topic>> getResponsibilityLine() {
        return responsibilityLine;
    }
    public static void setResponsibilityLine(Map<Integer, ArrayList<Topic>> res) {
        responsibilityLine=res;
    }}
