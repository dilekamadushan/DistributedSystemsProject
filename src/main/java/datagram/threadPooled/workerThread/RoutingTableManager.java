package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by dileka on 9/27/18.
 */
public class RoutingTableManager extends Thread {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> routingTable;
    
    private DatagramPacket datagramPacket;
    
    public RoutingTableManager(DatagramSocket socket, ArrayList<Node> routingTable, DatagramPacket datagramPacket) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
        this.datagramPacket = datagramPacket;
    }
    
    public void run() {
        addToNodeList(datagramPacket);
    }
    
    public void addToNodeList(DatagramPacket datagramPacket) {
        InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        Node node = routingTable.stream().filter(s -> s.getIp() == datagramPacket.getAddress().getAddress()).findFirst()
                .orElse(null);
        System.out.println("A node responded to the Join message:" + address + " " + port);
        if(node != null){
            node.setStatus(true);
            System.out.println("The new Routing table ");
        }
        for(Node peer:routingTable){
            peer.toString();
            
        }
    }
}
