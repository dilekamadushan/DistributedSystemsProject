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
public class RegisterAndJoinMessenger {
    
    private byte[] dataBuffer = null;
    
    private DatagramPacket threadDatagramPacket = null;
    
    private DatagramSocket threadDatagramSocket = null;
    
    private ArrayList<Node> toJoinNodes;
    
    private ArrayList<Node> triedToJoinNodes;
    
    private ArrayList<Node> routingTable;
    
    private DatagramSocket UDPsocket;
    
    private Socket TCPSocket;
    
    private PrintWriter out;
    
    private BufferedReader in;
    
    public RegisterAndJoinMessenger(DatagramSocket socket, ArrayList<Node> toJoinNodes, ArrayList<Node> triedToJoinNodes,
            ArrayList<Node> routingTable) {
        
        this.threadDatagramSocket = socket;
        this.toJoinNodes = toJoinNodes;
        this.triedToJoinNodes = triedToJoinNodes;
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
    
    public boolean start() throws IOException {
        boolean isRegistered = Register("0.0.0.0", 1111, "0030 REG 0.0.0.0 4453 dileka60");
        return isRegistered && sendJoin();
    }
    
    public boolean Register(String ip, int port, String msg) throws IOException {
        
        TCPSocket = new Socket(ip, port);
        out = new PrintWriter(TCPSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
        
        out.println(msg);
        char[] chars = new char[8192];
        int read = in.read(chars);
        String inMesssage = String.valueOf(chars, 0, read);
        System.out.println("Reply from BS server:" + inMesssage);
        String REGOK = inMesssage.substring(5, 10);
        System.out.println("REGOK message:" + REGOK);
        int code = Integer.parseInt(inMesssage.substring(11, 13).trim());
        System.out.println("Code" + code);
        
        if ("REGOK".equals(REGOK) && 98 > code) {
            System.out.println("No of Hosts connected already- " + inMesssage.substring(13));
            System.out.println("Connected to Bootstrap server successfully");
            String[] hostList;
            if (code < 10) {
                hostList = inMesssage.substring(13).trim().split("\n");
            } else {
                hostList = inMesssage.substring(14).trim().split("\n");
            }
            
            System.out.println("host string [0]:" + hostList[0]);
            for (int i = 0; i < hostList.length; i++) {
                String[] data = hostList[i].trim().split(" ");
                
                String[] ips = data[0].replace(".", " ").split(" ");
                System.out.println(
                        Integer.parseInt(ips[0]) + " " + Integer.parseInt(ips[1]) + " " + Integer.parseInt(ips[2]) + " "
                                + Integer.parseInt(ips[3]));
                toJoinNodes.add(new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                        (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, data[2],
                        Integer.parseInt(data[1])));
                System.out.println(toJoinNodes.get(i).toString());
            }
            for (Node node : toJoinNodes) {
                System.out.println("Node created" + node.toString());
            }
            this.routingTable = toJoinNodes;
            return true;
        } else {
            String errorCode = inMesssage.substring(11);
            switch (errorCode) {
                case "9999":
                    System.out.println("failed, there is some error in the command");
                    break;
                case "9998":
                    System.out.println("failed, already registered to you, unregister first");
                    break;
                case "9997":
                    System.out.println(" failed, registered to another user, try a different IP and port");
                    break;
                case "9996":
                    System.out.println("failed, can’t register. BS full.");
                default:
                    System.out.println("Invalid command");
            }
        }
        System.out.println("Returned String :" + inMesssage + " " + code);
        
        in.close();
        out.close();
        TCPSocket.close();
        return false;
        
    }
    
    public boolean sendJoin() throws IOException {
        if (toJoinNodes.size() <= 2) {
            Node node;
            for (int i = 0; i < 2; i++) {
                node = toJoinNodes.get(ThreadLocalRandom.current().nextInt(0, toJoinNodes.size()));
                toJoinNodes.remove(node);
                toJoinNodes.add(node);
                try {
                    System.out.println(
                            "Trying to send join message for node" + node.toString() + " " + node.getIp().toString());
                    byte[] bufToSend = "0022 JOIN 0.0.0.0 1234".getBytes();
                    DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                            InetAddress.getByAddress(node.getIp()), node.getPort());
                    node.setRetries(1);
                    threadDatagramSocket.send(nodeDatagramPacket);
                    toJoinNodes.add(node);
                    
                }
                catch (UnknownHostException e) {
                    System.out.println("Node unreachable");
                    e.printStackTrace();
                    if (toJoinNodes.size() < (2 - i)) {
                        return false;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error in socket");
                    if (toJoinNodes.size() < (2 - i)) {
                        return false;
                    }
                }
            }
            return true;
            
        }
        
        return false;
        
    }
}
