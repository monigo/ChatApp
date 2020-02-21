package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> groupName = new HashSet<String>();


    public ServerWorker(Server server , Socket clientSocket){
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line=reader.readLine())!=null){
            String tokens[] = line.trim().split(" ");
            if(tokens.length>0 && tokens!=null){
                String cmd = tokens[0];

                if("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)){
                    handleLogOff();
                    break;
                }
                else if("login".equalsIgnoreCase(cmd)){
                    handleLoginFun(outputStream , tokens);
                }
                else if("msg".equalsIgnoreCase(cmd)){
                    String tokensMsg[]  = line.trim().split(" ",3);
                    handleMessageFun(tokensMsg);
                }
                else if("join".equalsIgnoreCase(cmd)){
                    handleJoin(tokens);

                }
                else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }
                else{
                    String msg = "unknown "+cmd+"\n";
                    outputStream.write(msg.getBytes());
                }

            }

        }
// clientSocket.close();

    }

    private void handleLeave(String[] tokens) {
        if(tokens.length>1){
            String grp = tokens[1];
            groupName.remove(grp);
        }
    }

    public boolean isMemberOfGroup(String topic) {
        return groupName.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if(tokens.length>1){
            String grp = tokens[1];
            groupName.add(grp);
        }
    }
    
    // format msg <username> <msg body>
    // format msg <groupname> <msg body>
    private void handleMessageFun(String [] tokens) throws IOException {
        if(tokens.length<=2){
            return;
        }
        String receiver = tokens[1];
        String body = tokens[2];

        boolean isGroup = receiver.charAt(0)=='#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList){

            if(isGroup){
                if(worker.isMemberOfGroup(receiver)){
                    //String outMsg = "msg "+receiver+" : " +login+ "  "+body+"\n";
                    String outMsg = "msg "+receiver+":" +login+ "  "+body+"\n";

                    worker.send(outMsg);
                }
            }

            else{
                if(receiver.equalsIgnoreCase(worker.getLogin())) {
                    //String outMsg = "msg from " + login + " is : " + body + " \n";
                    String outMsg = "msg " + login + " " + body + " \n";

                    worker.send(outMsg);
                }
            }
        }
    }

    private void handleLogOff() throws IOException {
        server.removeWorker(this);
        // send other user current user's information
        List<ServerWorker> workerList= server.getWorkerList();
        String currentUer = "offline "+login + "\n";
        for(ServerWorker worker : workerList ){
            worker.send(currentUer);
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLoginFun(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length==3){
            String login = tokens[1];
            String pass = tokens[2];

            LoginHelper obj = new LoginHelper();
            if(obj.checkCredentials(login,pass)){
                //if((login.equals("guest") && pass.equals("guest")) ||(login.equals("neeraj") && pass.equals("neeraj")) || (login.equals("test") && pass.equals("test"))){
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully :"+ login);



                List<ServerWorker> workerList= server.getWorkerList();

                // just after login , send the user all other user's information
                for(ServerWorker worker :workerList ){
                    if(!login.equals(worker.getLogin()) && worker.getLogin() != null) {
                        String msg2 = "online "+worker.getLogin()+"\n";
                        send(msg2);
                    }

                }

                // send other user current user's information
                String currentUer = "online "+login + "\n";
                for(ServerWorker worker : workerList ){
                    if(!login.equals(worker.getLogin()))
                    worker.send(currentUer);
                }
            }
            else{
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("login failed for "+login);
            }
        }

    }

    private void send(String msg) throws IOException {
        if(login!=null)
        outputStream.write(msg.getBytes());
    }
}
