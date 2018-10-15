package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class SearchManager extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> routingTable;
    
    private DatagramPacket datagramPacket;
    
    private String gossipMessage;
    
    public SearchManager( ArrayList<Node> routingTable, String gossipMessage) {
        
        this.routingTable = routingTable;
        this.datagramPacket = datagramPacket;
        this.gossipMessage = gossipMessage;
    }
    
    public void run() {
        
        addToRoutingTable(gossipMessage);
    }
    
    public void addToRoutingTable(String gossipMessage) {
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
