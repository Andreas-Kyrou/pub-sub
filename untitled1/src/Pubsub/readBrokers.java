package Pubsub;



import java.io.*;
import java.util.ArrayList;
public class readBrokers {
    public  static ArrayList<Broker> getData() {
        File f = null;
        BufferedReader reader = null;
        String line;
        ArrayList<Broker> brokers=new ArrayList<Broker>();

        try {// to try gia na vri to txt pou periexi ton arithmo ton epeksergaston kai tis diergasies
            f = new File(readBrokers.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"\\brokers.txt");
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
                brokers.add(new Broker(Integer.parseInt(info[0]), Integer.parseInt(info[1]), info[2],null));
                line = reader.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return brokers;

    }
}
