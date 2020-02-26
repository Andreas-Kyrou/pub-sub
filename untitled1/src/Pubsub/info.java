package Pubsub;

import buses.Topic;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  class info implements Serializable {
    private static final long serialVersionUID=8527440485562111712L;
    private static  ArrayList<Broker> broker ;//lista me tous brokers
    private static Map<Integer, ArrayList<Topic>> responsibilityLine=new ConcurrentHashMap<>();//o xartis me to id tou broker k tis grames p ine ipefthinos
    public  info(ArrayList<Broker> br){
        super();
        broker=br;
    }
    public  info(ArrayList<Broker>br,Map<Integer, ArrayList<Topic>> r){
        this.broker=br;
        this.responsibilityLine=r;
    }

    public info() {
        super();
    }

   public  void setBroker(ArrayList<Broker> broker) {
        this.broker = broker;
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
