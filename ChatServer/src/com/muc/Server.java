package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{
    private final int serverPort;

    private ArrayList<ServerWorker> workerList= new ArrayList<ServerWorker>();

    public List<ServerWorker> getWorkerList (){
        return workerList;
    }

    public Server(int serverPort){
        this.serverPort = serverPort;
    }

    @Override
    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true){
                System.out.println("Ready to connect...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("connected from "+ clientSocket);
                ServerWorker serverWorker= new ServerWorker(this, clientSocket);
                workerList.add(serverWorker);
                serverWorker.start();

            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
