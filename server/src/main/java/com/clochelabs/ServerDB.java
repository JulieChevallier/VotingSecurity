package com.clochelabs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ServerDB {
    private static String user = "radulescut";
    //mettre son mdp sql de l'iut
    private static String password = "clochelabs19";
    //remplacer radulescut par son nom de l'iut
    private static String hostname ="jdbc:mariadb://webinfo.iutmontp.univ-montp2.fr:3306/radulescut?user="+user+"&password="+password;

    /**
     * Initiate a connection with the DB
     * @return null if there is an SQL error else return the Connection object
     */
    public static Connection getCon() {
        try{
            return DriverManager.getConnection(hostname);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}
