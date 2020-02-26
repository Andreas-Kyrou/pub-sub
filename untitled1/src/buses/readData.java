package buses;

import java.io.*;
import java.util.ArrayList;

public class readData {
    private static Bus spl(String line_bus){
        Bus bus=new Bus();
        String[] info=line_bus.split(",");
        bus.setRouteCode(info[0]);
        bus.setLineNumber(info[1]);
        bus.setLineName(info[2]);
        bus.setInfo(info[3]);
        return bus;
    }
    public  static ArrayList<Value> getData() {
        File f = null;
        File f1=null;
        BufferedReader reader = null;
        BufferedReader reader1=null;
        String line,line1;
        ArrayList<Value> bus_list=new ArrayList<Value>();
        try {
            f = new File(readData.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"\\RouteCodesNew.txt");
            f1=new File(readData.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"\\busLinesNew.txt");
        } catch (NullPointerException e) {
            System.err.println("File not found");//tiponi ena minima sfalmatos oti den mporese na vri to arxio
        }
        try {
            reader = new BufferedReader(new FileReader(f));//dokimazi na aniksi to arxeio txt me onoma p dosame pio pano
            reader1=new BufferedReader(new FileReader(f1));
        } catch (FileNotFoundException e) {
            System.err.println("Error opening file!");//kai an den mporesi na to aniksi tiponi minima sfalmatos
        }
        try {
            line = reader.readLine();//diavazi tin proti grammi tou txt i opia ipodiloni tous epeksergastes
            while (line != null) {
                bus_list.add(new Value(spl(line),0.0,0.0,""));
                line = reader.readLine();
            }
            line1=reader1.readLine();
            while (line1!= null) {

                String[] info2=line1.split(",");
                for(int i=0;i<bus_list.size();i++){
                    if(bus_list.get(i).getBus().getLineNumber().equals(info2[0])){
                        bus_list.get(i).getBus().setBuslineid(info2[1]);
                    }
                }
                line1=reader1.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bus_list;
    }
}
