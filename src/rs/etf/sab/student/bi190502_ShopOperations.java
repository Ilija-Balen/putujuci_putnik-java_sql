/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import rs.etf.sab.operations.ShopOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_ShopOperations implements ShopOperations{

    @Override
    public int createShop(String name, String cityName) {
        int shopId = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("insert into Shop(IdCity, Name, Credit) values(?,?, null)", PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps1 = con.prepareStatement("select IdCity from City where Name = ?");
                PreparedStatement ps2 = con.prepareStatement("insert into Discount(IdShop, Percentage) values(?, 0)")){
            
                ps1.setString(1, cityName);
                ResultSet rs1 = ps1.executeQuery();
                int cityId  = -1;
                if(rs1.next()) {
                    cityId = rs1.getInt(1);
                }
                
                ps.setInt(1, cityId);
                ps.setString(2, name);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if(rs.next()){
                    shopId=rs.getInt(1);
                }
                
                ps2.setInt(1, shopId);
                ps2.executeUpdate();
                
        } catch (Exception e) {
            System.err.println("Error ovde gde sam stavio");
        }
        return shopId;
    }

    @Override
    public int setCity(int shopId, String cityName) {
        int success = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity where Name = ?");
                PreparedStatement ps2 = con.prepareStatement("insert into Shop(IdCity) values (?) where IdShop= ?", PreparedStatement.RETURN_GENERATED_KEYS)){
            
            int cityId = -1;
            ps.setString(1, cityName);
            ResultSet rs = ps.executeQuery();
            if(rs.next())cityId = rs.getInt(1);
            
            if(cityId==-1) return cityId;
            
            ps2.setInt(1, cityId);
            ps2.setInt(2, shopId);
            ps2.executeUpdate();
            ResultSet rs2 = ps2.getGeneratedKeys();
            if(rs2.next())success= 1;
        } catch (Exception e) {
        }
        
        return success;
    }

    @Override
    public int getCity(int shopId) {
        int cityId= -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity from Shop where IdShop = ?")){
        
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            if(rs.next())cityId = rs.getInt(1);
        
        } catch (Exception e) {
        }
        
        return cityId;
    }

    @Override
    public int setDiscount(int shopId, int discountPercentage) {
        int success=-1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("update Discount set Percentage = ? where IdShop = ?", PreparedStatement.RETURN_GENERATED_KEYS)){
            
            ps.setInt(1, discountPercentage);
            ps.setInt(2, shopId);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next())success=1;
            
        } catch (Exception e) {
        }
        
        return success;
    }

    @Override
    public int increaseArticleCount(int articleId, int increment) {
        int count = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Count from Article where IdArticle = ?");
                PreparedStatement ps1 = con.prepareStatement("update Article set Count = ? where IdArticle = ?", PreparedStatement.RETURN_GENERATED_KEYS);){
            
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            if(rs.next())count = rs.getInt(1);
            else return count;
            
            count+=increment;
            ps1.setInt(1, count);
            ps1.setInt(2, articleId);
            ps1.executeUpdate();
            
            ResultSet rs1 = ps1.getGeneratedKeys();
            if(!rs1.next())return -1;
        } catch (Exception e) {
        }
        return count;
    }

    @Override
    public int getArticleCount(int articleId) {
        int count = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Count from Article where IdArticle = ?")){
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())count = rs.getInt(1);
        } catch (Exception e) {
        }
        return count;
    }

    @Override
    public List<Integer> getArticles(int shopId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdArticle from Article where IdShop = ?")){
            
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                lista.add(rs.getInt(1));
            }
        } catch (Exception e) {
        }
        return lista;
    }

    @Override
    public int getDiscount(int shopId) {
        int discount = 0;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select Percentage from Discount where IdShop = ?")){
            
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())discount = rs.getInt(1);
        
        } catch (Exception e) {
        }
        return discount;
    }
    
}
