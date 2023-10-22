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
import rs.etf.sab.operations.CityOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_CityOperations implements CityOperations{

    @Override
    public int createCity(String name) {
        int cityId = -1;
        
        Connection con  =DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("insert into City(Name) values(?)", PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setString(1, name);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next())cityId= rs.getInt(1);
        } catch (Exception e) {
        }
        return cityId;
    }   

    @Override
    public List<Integer> getCities() {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity from City")){
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
    public int connectCities(int cityId1, int cityId2, int distance) {
        int lineId=-1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("insert into Line(IdCity, IdC2, Distance) values(?,?,?)",PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement psl = con.prepareStatement("select * from Line where IdCity = ? and IdC = ?")){
            
//            psl.setInt(1, cityId1);
//            psl.setInt(2, cityId2);
//            ResultSet rs = psl.executeQuery();
//            if(rs.next()){
//                if(rs.getInt("IdLine") != 0)return lineId;
//            }
                
            ps.setInt(1, cityId1);
            ps.setInt(2, cityId2);
            ps.setInt(3, distance);
            ps.executeUpdate();
            ResultSet rs2 = ps.getGeneratedKeys();
            
            if(rs2.next())lineId=rs2.getInt(1);
        } catch (Exception e) {
        }
        return lineId;
    }

    @Override
    public List<Integer> getConnectedCities(int cityId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity from Line where IdC2 = ?");
                PreparedStatement psl = con.prepareStatement("select IdC2 from Line where IdCity = ?")){
            
            ps.setInt(1, cityId);
            psl.setInt(1, cityId);
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                lista.add(rs.getInt(1));
            }
            
            ResultSet rs2 = psl.executeQuery();
            while(rs2.next()){
                lista.add(rs.getInt(1));
            }
        } catch (Exception e) {
        }
        return lista;
    }

    @Override
    public List<Integer> getShops(int CityId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdShop from Shop where IdCity = ?")){
            ps.setInt(1, CityId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next())lista.add(rs.getInt(1));
        } catch (Exception e) {
        }
        return lista;
    }
    
}
