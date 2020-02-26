package Pubsub;

import java.util.ArrayList;

public interface Node {
    public void init();

    public void connect();

    public void disconect();

    public void updateNodes();//tha enimeroni gia nees egkrafes broker,sub,pub

}
