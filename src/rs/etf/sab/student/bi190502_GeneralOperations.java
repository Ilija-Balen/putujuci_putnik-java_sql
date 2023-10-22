/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import rs.etf.sab.operations.GeneralOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_GeneralOperations implements GeneralOperations{
    
    private static Calendar time = Calendar.getInstance();
    
    @Override
    public void setInitialTime(Calendar clndr) {
        time.setTimeInMillis(clndr.getTimeInMillis());
    }

    @Override
    public Calendar time(int days) {
        //TREBA PROVERITI DA LI JE STIGLA PORUDZBINA, AKO JESTE
        //('SENT') DA LI JE PROSLO time + dani koji su potrebni za transport
        //ako jeste proglasiti za arrived i postaviti vreme stizanja na SendingTime + dan za transport
        Connection con = DB.getInstance().getConnection();
        
        time.add(Calendar.DATE, days);
        
        try (PreparedStatement ps = con.prepareStatement("select IdOrder, TransportDays, SendingTime from [Order] where State = 'sent'");
                PreparedStatement ps1 = con.prepareStatement("update [Order] set ReceivedTime = ?, State = 'arrived' where IdOrder = ?")){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int orderId = rs.getInt(1);
                int TransportDays = rs.getInt(2);
                Date sendingTime = rs.getDate(3);
                Date receivedTime;
                
                int prosloDana = (int) ((time.getTimeInMillis() - sendingTime.getTime())/ (24*60*60*1000));
                if(prosloDana >= TransportDays){
                    long p = TransportDays * (24*60*60*1000);
                    receivedTime = new Date(sendingTime.getTime() + p);
                    ps1.setDate(1, receivedTime);
                    ps1.setInt(2, orderId);
                    ps1.executeUpdate();
                }
            }
        } catch (Exception e) {
        }
        
        return time;
    }

    @Override
    public Calendar getCurrentTime() {
        return time;
    }

    @Override
    public void eraseAll() {
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM [Transaction]");
             PreparedStatement ps1 = con.prepareStatement("DELETE FROM [Item]");
                PreparedStatement ps2 = con.prepareStatement("DELETE FROM [Discount]");
                PreparedStatement ps3 = con.prepareStatement("DELETE FROM [Line]");
                PreparedStatement ps4 = con.prepareStatement("DELETE FROM [Order]");
                PreparedStatement ps5 = con.prepareStatement("DELETE FROM [Article]");
                PreparedStatement ps6 = con.prepareStatement("DELETE FROM [Buyer]");
                PreparedStatement ps7 = con.prepareStatement("DELETE FROM [Shop]");
                PreparedStatement ps8 = con.prepareStatement("DELETE FROM [City]");
                PreparedStatement statement = con.prepareStatement("DBCC CHECKIDENT (?, RESEED, 0)")){
                
            ps.executeUpdate();
            ps1.executeUpdate();
            ps2.executeUpdate();
            ps3.executeUpdate();
            ps4.executeUpdate();
            ps5.executeUpdate();
            ps6.executeUpdate();
            ps7.executeUpdate();
            ps8.executeUpdate();
            
            
            statement.setString(1, "Article");
            statement.execute();

            statement.setString(1, "Buyer");
            statement.execute();

            statement.setString(1, "City");
            statement.execute();

            statement.setString(1, "Discount");
            statement.execute();

            statement.setString(1, "Item");
            statement.execute();

            statement.setString(1, "Line");
            statement.execute();

            statement.setString(1, "[Order]");
            statement.execute();

            statement.setString(1, "Shop");
            statement.execute();

            statement.setString(1, "[Transaction]");
            statement.execute();
        
        } catch (Exception e) {
        }
    }
    
}
