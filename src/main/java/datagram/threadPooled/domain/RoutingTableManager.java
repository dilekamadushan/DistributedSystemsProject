package datagram.threadPooled.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by dileka on 9/27/18.
 */
public class RoutingTableManager {
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> routingTable;
    
    private DatagramSocket UDPsocket;
    
    public RoutingTableManager(DatagramSocket socket, ArrayList<Node> routingTable) {
        
        this.threadDatagramSocket = socket;
        this.routingTable = routingTable;
    }
    public boolean start() throws IOException {
       return true;
    }
    
  
    
    public void addToNodeList(DatagramPacket datagramPacket) {
        InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        Node node = routingTable.stream().filter(s -> s.getIp() == datagramPacket.getAddress().getAddress()).findFirst().orElse(null);
        System.out.println("A node responded to the Join message:"+address+" "+port);
        node.setStatus(true);
    }
}
