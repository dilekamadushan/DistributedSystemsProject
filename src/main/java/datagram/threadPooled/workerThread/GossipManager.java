package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class GossipManager extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> routingTable;
    
    private DatagramPacket datagramPacket;
    
    private String gossipMessage;
    
    public GossipManager( ArrayList<Node> routingTable, DatagramPacket datagramPacket, String gossipMessage) {
        
        this.routingTable = routingTable;
        this.datagramPacket = datagramPacket;
        this.gossipMessage = gossipMessage;
    }
    
    public void run() {
        
        addToRoutingTable(datagramPacket);
    }
    
    public void addToRoutingTable(DatagramPacket datagramPacket) {
     /*   InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        Node node = routingTable.stream().filter(s -> s.getIp() == datagramPacket.getAddress().getAddress()).findFirst()
                .orElse(null);
        System.out.println("A node responded to the Join message:" + address + " " + port);
        if(node != null){
            node.setStatus(true);
            System.out.println("The new Routing table ");
        }
        for(Node peer:routingTable){
            peer.toString();*/
     //Logic to decode the gossip message and add new nodes
            
        }
}
