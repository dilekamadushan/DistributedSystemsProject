package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dileka on 9/27/18.
 */
public class Searcher implements Runnable {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> routingTable;
    
    private String searchString;
    
    public Searcher(DatagramSocket socket, ArrayList<Node> routingTable, String searchString) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.searchString = searchString;
    }
    
    public void run() {
        
        for (Node node : routingTable) {
            try {
                sendSearchRequest(node, this.searchString);
            }
            catch (UnknownHostException e) {
                System.out.println("Node unreachable");
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error in socket");
            }
        }
    }
    
    public void sendSearchRequest(Node node, String searchString) throws IOException {
        System.out.println("Trying to send search query for node" + node.toString() + " " + Arrays.toString(node.getIp()));
        byte[] bufToSend = "0043 SEARCH king,the 2 0.0.0.0 1234".getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
}
