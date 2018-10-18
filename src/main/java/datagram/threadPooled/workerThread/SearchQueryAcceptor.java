package datagram.threadPooled.workerThread;

import datagram.threadPooled.domain.Node;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

/**
 * Created by dileka on 9/27/18.
 */
public class SearchQueryAcceptor extends Thread {
    
    private ExecutorService executorService;
    
    private DatagramSocket datagramSocket;
    
    private ArrayList<Node> routingTable;
    
    public SearchQueryAcceptor(DatagramSocket socket, ArrayList<Node> routingTable, ExecutorService executorService) {
        this.executorService = executorService;
        this.routingTable = routingTable;
        this.datagramSocket = socket;
        System.out.println("Search Query Acceptor : SearchQueryAcceptor started");
    }
    
    public void run() {
        System.out.println("Search Query Acceptor : inside startWork Method");
        while (true) {
            System.out.println("Search Query Acceptor : inside event loop");
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.println("Search Query Acceptor :Enter a search query: ");
            String query = reader.nextLine(); // Scans the next token of the input as a string.
            //once finished
            reader.close();
            System.out.println("Search Query Acceptor :finished reading line "+query);
            executorService.execute(new Searcher(this.datagramSocket, routingTable, query));
            System.out.println("Search Query Acceptor : created a Searcher thread");
        }
        
    }
    
}
