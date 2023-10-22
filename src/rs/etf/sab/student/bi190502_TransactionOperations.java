/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.TransactionOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_TransactionOperations implements TransactionOperations{

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int buyerId) {
        BigDecimal ammount = new BigDecimal(0);
        BigDecimal ammount2 = new BigDecimal(0);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select sum(Amount) from [Transaction] where IdBuyer = ? group by IdBuyer")){
            
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())ammount2 = rs.getBigDecimal(1);
        } catch (Exception e) {
        }
        
        return ammount.add(ammount2);
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int shopId) { 
        BigDecimal ammount = new BigDecimal(0).setScale(3);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select sum(Amount) from [Transaction] where IdShop = ? and ExecutionTime is not null group by IdShop")){
            
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())ammount = rs.getBigDecimal(1);
        
        } catch (Exception e) {
        }
        
        return ammount;
    }

    @Override
    public List<Integer> getTransationsForBuyer(int buyerId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdTransaction from [Transaction] where IdBuyer = ?")){
            
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                lista.add(rs.getInt(1));
            }
        } catch (Exception e) {
        }
        if(!lista.isEmpty())return lista;
        else return null;
    }

    @Override
    public int getTransactionForBuyersOrder(int orderId) {
        int transactionId = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdTransaction from [Transaction] IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())transactionId = rs.getInt(1);
        } catch (Exception e) {
        }
        return transactionId;
    }

    @Override
    public int getTransactionForShopAndOrder(int orderId, int shopId) {
        int transactionId = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdTransaction from [Transaction] where IdOrder = ? and IdShop = ?")){
            
            ps.setInt(1, orderId);
            ps.setInt(2, shopId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())transactionId = rs.getInt(1);
        
        } catch (Exception e) {
        }
        
        return transactionId;
    }

    @Override
    public List<Integer> getTransationsForShop(int shopId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdTransaction from [Transaction] where IdShop = ? and ExecutionTime is not null")){
            
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                lista.add(rs.getInt(1));
            }
        } catch (Exception e) {
        }
        
        if(!lista.isEmpty())return lista;
        else return null;
    }

    @Override
    public Calendar getTimeOfExecution(int i) {
        Calendar calendar = Calendar.getInstance();
        Date time;
        Connection conn = DB.getInstance().getConnection();
        try(PreparedStatement ps = conn.prepareStatement("Select ExecutionTime from dbo.[Transaction] where IdTransaction = ?");) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
               time = rs.getDate(1);
               if(time!=null)calendar.setTime(time);
               else return null;
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_TransactionOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return calendar;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
        BigDecimal amount = new BigDecimal(-1);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Amount from [Transaction] where IdOrder = ? and IdBuyer is not null")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())amount = rs.getBigDecimal(1);
            
        } catch (Exception e) {
        }
        return amount;
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int shopId, int orderId) {
        BigDecimal amount = new BigDecimal(-1);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Amount from [Transaction] where IdOrder = ? and IdShop = ?")){
            
            ps.setInt(1, orderId);
            ps.setInt(2, shopId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())amount = rs.getBigDecimal(1);
            
        } catch (Exception e) {
        }
        return amount;
    }

    @Override
    public BigDecimal getTransactionAmount(int transactionId) {
        BigDecimal amount = new BigDecimal(-1);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Amount from [Transaction] where IdTransaction = ?")){
            
            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())amount = rs.getBigDecimal(1);
            
        } catch (Exception e) {
        }
        return amount;
    }

    @Override
    public BigDecimal getSystemProfit() {
        BigDecimal amount = new BigDecimal(0).setScale(3);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select sum(Amount)*0.05 from [Transaction] join [Order] on [Transaction].IdOrder = [Order].IdOrder where [Order].State = 'arrived' and [Transaction].IdShop is null")){
            
            ResultSet rs = ps.executeQuery();
            BigDecimal pom = null;
            if(rs.next()){
                pom = rs.getBigDecimal(1).setScale(3);
                amount = amount.add(pom);
            } 
            //amount = amount.multiply(new BigDecimal(0.05));
        } catch (Exception e) {
        }
        return amount;
    }
    
}
