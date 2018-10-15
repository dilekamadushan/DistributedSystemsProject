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
    
    private byte[] bufToSend;
    
    public GossipSender(DatagramSocket socket, ArrayList<Node> routingTable) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        System.out.println("Gossip Sender:started");
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
        System.out.println("Gossip Sender:Entering the event loop");
        while (true) {
            System.out.println("Gossip Sender:Starting to send the gossip message to all nodes");
            for (Node node : routingTable) {
                try {
                    sendGossip(node);
                    System.out.println("Gossip Sender:Gossip message sent to " + node.toString());
                }
                catch (UnknownHostException e) {
                    System.out.println("Node unreachable");
                    e.printStackTrace();
                    System.out.println("Gossip Sender:Gossip message failed to " + node.toString());
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error in socket");
                    System.out.println("Gossip Sender:Gossip message failed to " + node.toString());
                }
            }
            try {
                System.out.println("Gossip Sender:Gossip thread sleep for 10 seconds ");
                Thread.sleep(10000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Gossip Sender:Gossip thread wakes up");
        }
        
    }
    
    private void sendGossip(Node node) throws IOException {
        System.out.println("Gossip Sender:Gossip thread wakes up");
        bufToSend = String.format("0022 GOSSIP %s %s", new String(node.getIp()), node.getPort()).getBytes();
        DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                InetAddress.getByAddress(node.getIp()), node.getPort());
        threadDatagramSocket.send(nodeDatagramPacket);
    }
}
