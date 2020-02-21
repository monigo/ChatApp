package com.muc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class LoginHelper {

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/ChatApp";
    static final String USER = "root";
    static final String PASS = "";
    public boolean  checkCredentials(String uname , String pass) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            //System.out.println("Creating statement...");
            String sql = "SELECT * from LoginInfo where Name= ? and Password= ? ";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1,uname);
            stmt.setString(2,pass);
            
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return true;
            }
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        //System.out.println("Finish!");
        return false;
    }
}