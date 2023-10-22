/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.BuyerOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_BuyerOperations implements BuyerOperations{

    @Override
    public int createBuyer(String name, int cityId) {
        int IdBuyer=-1;
        Connection con= DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("insert into Buyer(Name, IdCity, Credit) values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setString(1, name);
            ps.setInt(2,cityId);
            ps.setBigDecimal(3, new BigDecimal(0));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                IdBuyer= rs.getInt(1);
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return IdBuyer;
    }

    @Override
    public int setCity(int buyerId, int cityId) {
        int success= -1;
        Connection con= DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("update Buyer set IdCity = ? where IdBuyer = ?")){
            ps.setInt(1, cityId);
            ps.setInt(2,buyerId);
            success=ps.executeUpdate();
            if(success>0)success=1;
        } catch (Exception e) {
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return success;
    }

    @Override
    public int getCity(int buyerId) {
        int IdCity=-1;
        Connection con = DB.getInstance().getConnection();
        try(PreparedStatement ps = con.prepareStatement("select IdCity from Buyer where IdBuyer = ?")){
            ps.setInt(1, buyerId);
            
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                IdCity= rs.getInt(1);
            }
        }catch (Exception e){
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return IdCity;
    }

    @Override
    public BigDecimal increaseCredit(int buyerId, BigDecimal credit) {
        BigDecimal increased = new BigDecimal(0);
        Connection con = DB.getInstance().getConnection();
        
        try (PreparedStatement ps = con.prepareStatement("select Credit from Buyer where IdBuyer = ?");
            PreparedStatement psl = con.prepareStatement("Update Buyer set Credit = ? where IdBuyer = ?")){
            
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            BigDecimal pom;
            
            if(rs.next()){
               pom = rs.getBigDecimal(1);
               BigDecimal sum = pom.add(credit);
               psl.setBigDecimal(1, sum);
               psl.setInt(2, buyerId);
               
               psl.executeUpdate();         
            }
   
            rs = ps.executeQuery();
            if(rs.next()){
                increased = rs.getBigDecimal(1);
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return increased;
    }

    @Override
    public int createOrder(int buyerId) {
        int IdOrder = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("insert into [Order](IdBuyer, State, ReceivedTime, SendingTime) values(?, ?, null, null)", PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, buyerId);
            ps.setString(2, "created");
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                IdOrder = rs.getInt(1);
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return IdOrder;
    }

    @Override
    public List<Integer> getOrders(int buyerId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdOrder from dbo.[Order] where IdBuyer = ?")){
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                lista.add(rs.getInt(1));
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return lista;
    }

    @Override
    public BigDecimal getCredit(int buyerId) {
        BigDecimal credit = new BigDecimal(-1);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Credit from Buyer where IdBuyer= ?")){
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                credit = rs.getBigDecimal(1);
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_BuyerOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return credit;
    }
    
    public int skini(int buyerId, BigDecimal FinalPrice){
        BigDecimal Credit = new BigDecimal(0);
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Credit from Buyer where IdBuyer = ?");
                PreparedStatement ps1 = con.prepareStatement("update Buyer set Credit = Credit - ? where IdBuyer = ?");){
            
            ps.setInt(1, buyerId);
            ResultSet rs = ps.executeQuery();
            if(rs.next())Credit = rs.getBigDecimal(1);
            
            if(Credit.compareTo(FinalPrice)==1){
                //Credit je Veci od FinalPrice
                ps1.setBigDecimal(1, FinalPrice);
                ps1.setInt(2, buyerId);
                ps1.executeUpdate();
            }else return -1;
        } catch (Exception e) {
        }
        
        return 1;
    }
}
