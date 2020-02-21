package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class C1 implements UserStatusListener{
    @Override
    public void online(String login) {
        System.out.println("ONLINE "+login);
    }

    @Override
    public void offline(String login) {
        System.out.println("OFFLINE "+login);
    }
}
class C2 implements MessageListener{
    @Override
    public void onMessage(String fromLogin, String msgBody) {
        System.out.println("You have got a message from "+fromLogin+" ===> "+msgBody);
    }
}


public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener>userStatusListeners = new ArrayList<>();

    private ArrayList<MessageListener>messageListeners = new ArrayList<>();


    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {

        ChatClient client = new ChatClient("localhost",8818);



        //anonymous inner class-->
//        client.addUserStatusListener(new UserStatusListener() {
//            @Override
//            public void online(String login) {
//                System.out.println("ONLINE "+login);
//            }
//
//            @Override
//            public void offline(String login) {
//                System.out.println("OFFLINE "+login);
//            }
//        });
 /*
        ||
        ||
        ||
        ||
        ||
        ||
        ======>>>>>
*/
        client.addUserStatusListener(new C1());


        //anonymous inner class-->
//        client.addMessageListener(new MessageListener() {
//            @Override
//            public void onMessage(String fromLogin, String msgBody) {
//                System.out.println("You have got a message from "+fromLogin+" ===> "+msgBody);
//            }
//        });

/*
        ||
        ||
        ||
        ||
        ||
        ||
        ======>>>>>
*/
        client.addMessageListener(new C2());


        if(!client.connect()){
            System.err.println("connection failedd");
        }
        else{
            System.out.println("connection successful");
            if(client.login("guest", "guest")){
                System.out.println("Login Successful");
                client.msg("neeraj","Hello World"); // msg is defined downside
            }
            else{
                System.err.println("Login failed");
            }
            //client.logoff();
        }
    }



    public boolean connect() {
        try {
            this.socket = new Socket(serverName , serverPort);
            System.out.println("Client port is "+socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login "+login +" "+password+"\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line : "+response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return  true;
        }
        else {
            return false;
        }

    }

    public void msg(String receiver, String msgBody) throws IOException {
        String cmd = "msg "+receiver+" "+msgBody+"\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff() throws IOException{
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line ;
            while ((line=bufferedIn.readLine())!=null) {
                String tokens[] = line.trim().split(" ");
                if(tokens.length>0 && tokens!=null) {
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens); //defined downside
                    }
                    else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);  //defined downside
                    }
                    else if("msg".equalsIgnoreCase(cmd)){
                        String tokensMsg[]  = line.trim().split(" ",3);
                        handleMessage(tokensMsg);  //defined downside
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        System.out.println("User list size iso "+userStatusListeners.size());
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        System.out.println("User list size is "+userStatusListeners.size());
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    private void handleMessage(String[] tokenMsg) {
        String login =  tokenMsg[1];
        String msgBody = tokenMsg[2];
        System.out.println("Msg list size is "+messageListeners.size());
        for(MessageListener listener : messageListeners){
            listener.onMessage(login,msgBody);
        }
    }




    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
