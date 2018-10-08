package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class GossipSender implements Runnable {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> routingTable;
    
    public GossipSender(DatagramSocket socket, ArrayList<Node> routingTable) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
    }
    
    // A utility method to convert the byte array 
    // data into a string representation. 
    public static StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
    
    public void run() {
        
        for (Node node : routingTable) {
            try {
                sendGossip(node);
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
    
    public void sendGossip(Node node) throws IOException {
        System.out.println("Trying to send gossip message for node" + node.toString() + " " + node.getIp().toString());
        byte[] bufToSend = "0022 JOIN 0.0.0.0 1234".getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
}
