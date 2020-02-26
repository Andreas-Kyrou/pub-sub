package Pubsub;

import buses.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class readPos {
    static int j=1;
    public  static ArrayList<Value> getData(Value bus) {
        ArrayList<Value> value=new ArrayList<>();
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
            line = reader.readLine();
            while (line != null) {
                String[] info = line.split(",");
                if (info[1].equals(bus.getBus().getRouteCode())) {
                    value.add(new Value(bus.getBus(),Double.parseDouble(info[3]),Double.parseDouble(info[4]),info[5],info[2]));
                }
                line = reader.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }
}
