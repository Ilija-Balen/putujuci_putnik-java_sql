/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.OrderOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_OrderOperations implements OrderOperations{
    
    public class Node{
        public int Grad;
        public int Distance;
    }
    Map<Integer,List<Node>> putanjaOrdera = new HashMap<>();
    
    @Override
    public int addArticle(int orderId, int articleId, int count) {
        int itemId = -1;
        int countDB = 0;
        int countArticle = 0;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps1 = con.prepareStatement("select IdItem,Count from Item where IdOrder = ? and IdArticle = ?");
                PreparedStatement ps2 = con.prepareStatement("insert into Item(IdArticle,IdOrder,Count) values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps3 = con.prepareStatement("update Item set Count = ? where IdItem = ?");
                PreparedStatement ps4 = con.prepareStatement("select Count from Article where IdArticle = ?");
                PreparedStatement ps5 = con.prepareStatement("update Article set Count = ? where IdArticle = ?")){
            
            ps4.setInt(1, articleId);
            ResultSet rs4 = ps4.executeQuery();
            if(rs4.next()){
                countArticle = rs4.getInt(1);
                if((countArticle - count)< 0)return -1; // ukoliko je broj artikala manji od onog sto se trazi, neuspesno dodavanje
                //broj artikala je veci, setuj nov broj artikala
                ps5.setInt(1, countArticle - count);
                ps5.setInt(2, articleId);
                ps5.executeUpdate();
            }else return -1;
            
            
            ps1.setInt(1, orderId);
            ps1.setInt(2, articleId);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next()){
                itemId = rs1.getInt(1);
                countDB = rs1.getInt(2);
            }
            
            if(itemId==-1){
                //nema Itema
                ps2.setInt(1, articleId);
                ps2.setInt(2, orderId);
                ps2.setInt(3, count);
                ps2.executeUpdate();
                
                ResultSet prs = ps2.getGeneratedKeys();
                if(prs.next())itemId = prs.getInt(1);
            }else{
                //item postoji
                ps3.setInt(1, count);
                ps3.setInt(3, itemId);
            }
        } catch (Exception e) {
        }
        return itemId;
    }

    @Override
    public int removeArticle(int orderId, int articleId) {
        int count = 0;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("delete from Item where IdOrder = ? and IdArticle = ?", PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps1 = con.prepareStatement("select Count from Item where IdOrder = ? and IdArticle = ?");
                PreparedStatement ps2 = con.prepareStatement("update Article set Count = Count + ? where IdArticle = ?")){
            
            //get article count
            ps1.setInt(1, orderId);
            ps1.setInt(2, articleId);
            ResultSet rs1 = ps1.executeQuery();
            if(rs1.next())count = rs1.getInt(1);
            
            //update article count
            ps2.setInt(1, count);
            ps2.setInt(2, articleId);
            ps2.executeUpdate();
            
            //delete
            ps.setInt(1, orderId);
            ps.setInt(2, articleId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) return 1;
        } catch (Exception e) {
        }
        return -1;}

    @Override
    public List<Integer> getItems(int orderId) {
        List<Integer> lista = new ArrayList<>();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdItem from Item where IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                lista.add(rs.getInt(1));
            }
        } catch (Exception e) {
        }
        
        return lista;
    }

    @Override
    public int completeOrder(int orderId) {
        int itemId = -1;
        GeneralOperations GO = new bi190502_GeneralOperations();
        Date time;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("update [Order] set SendingTime = ?, State = 'sent' where IdOrder = ?");
                CallableStatement cs = con.prepareCall("execute SP_FINAL_PRICE @OrderId = ?");
                PreparedStatement ps1 = con.prepareStatement("Select distinct(dbo.[Shop].IdCity) from dbo.Item join dbo.[Article] on dbo.Item.IdArticle = dbo.[Article].IdArticle join dbo.[Shop] on dbo.[Article].IdShop = dbo.[Shop].IdShop where Item.IdOrder = ?");
                PreparedStatement ps2 = con.prepareStatement("select IdBuyer from [Order] where IdOrder = ?");
                PreparedStatement ps3 = con.prepareStatement("select IdCity from Buyer where IdBuyer = ?");
                PreparedStatement ps4 = con.prepareStatement("select IdShop from Shop where IdCity = ?");
                PreparedStatement ps5 = con.prepareStatement("insert into [Transaction](IdBuyer,IdOrder,ExecutionTime,Amount) values(?,?,?,?)");
                PreparedStatement ps6 = con.prepareStatement("select FinalPrice from [Order] where IdOrder = ?");
                PreparedStatement ps7 = con.prepareStatement("update [Order] set TransportDays = ? where IdOrder = ?");
                PreparedStatement ps8 = con.prepareStatement("update [Order] set ReceivedTime = ?, State = 'arrived' where IdOrder = ?");){
            
            //namesti vreme slanja na trenutno
            time = new Date(GO.getCurrentTime().getTimeInMillis());
            ps.setDate(1, time);
            ps.setInt(2, orderId);
            int k = ps.executeUpdate();
            if(k<=0)return -1;
            
            //namesti cenu
            cs.setInt(1, orderId);
            k = cs.executeUpdate();
            if(k<= 0)return -1;
            
            //za proveru jel u gradu kupca ima prodza
            int buyerId = -1;
            ps2.setInt(1, orderId);
            ResultSet rs2 = ps2.executeQuery();
            if(rs2.next())buyerId = rs2.getInt(1);
            else return -1;
            
            int buyerCity = -1;
            ps3.setInt(1, buyerId);
            ResultSet rs3 = ps3.executeQuery();
            if(rs3.next())buyerCity = rs3.getInt(1);
            
            //provera da li ima prodavnica u gradu kupca
            boolean imaProdavnica = false;
            ps4.setInt(1, buyerCity);
            ResultSet rs4 = ps4.executeQuery();
            if(rs4.next())imaProdavnica = true;
            
            //postavlja se grad u koji stize porudzbina
            List<Integer> shopCities = new ArrayList<>();
            ps1.setInt(1, orderId);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                int grad = rs1.getInt(1);
                shopCities.add(grad);
            }
             
            Line allLines = new Line();
            allLines.popuni();
            int najbliziGradProdavnica = -1;
            if(imaProdavnica){
                najbliziGradProdavnica = buyerCity;
            }else{
                //nema prodza u gradu, nadji najblizi u kom ima prodza
                najbliziGradProdavnica = allLines.findNearestCityShop(buyerCity);
            }
            
            //postavi putanju kojom se krece ORDER
            //prvo nadji najdalji grad iz porudzbine
            int najdalji = -1;
            najdalji = allLines.findFurthestCityShop(najbliziGradProdavnica,shopCities);
            //napravi putanju
            List<Node> putanja = allLines.napraviPutanju(najdalji, najbliziGradProdavnica);
            
            int ukupnoDana = 0;
            //iz putanje izvuci koliko dana se putuje do kupca
            if(imaProdavnica){
                //ukoliko ima prodavnica u gradu, ne treba da se dodaje na putovanje 
                for (Node node : putanja) {
                    ukupnoDana += node.Distance;
                }
                ps7.setInt(1, ukupnoDana);
                ps7.setInt(2, orderId);
                ps7.executeUpdate();
            }else{
                //posto nema prodavnica u gradu, treba da dodamo putovanje od prodze do kupca
                for (Node node : putanja) {
                    ukupnoDana += node.Distance;
                }
                //sad imamo ukupno dana do najblize prodze u ukupnoDana       
                putanja.clear();
                Node pomNode = new Node();
                pomNode.Distance = ukupnoDana;
                pomNode.Grad = najbliziGradProdavnica;
                putanja.add(pomNode);
                
                List<Node> putanja2 = allLines.napraviPutanju(najbliziGradProdavnica, buyerCity);
                for (int i = 0; i< putanja2.size(); i++){
                    ukupnoDana+= putanja2.get(i).Distance;
                    putanja.add(putanja2.get(i));
                }
                
                
                ps7.setInt(1, ukupnoDana);
                ps7.setInt(2, orderId);
                ps7.executeUpdate();
            }
            
            
            //smesti putanju u mapu putanja
            putanjaOrdera.put(orderId, putanja);
           
            
            
            //dohv FinalPrice
            BigDecimal FinalPrice=null;
            ps6.setInt(1, orderId);
            ResultSet rs6 = ps6.executeQuery();
            if(rs6.next())FinalPrice = rs6.getBigDecimal(1);
                    
            //Napravi transakciju od kupca ka sistemu
            ps5.setInt(1, buyerId);
            ps5.setInt(2, orderId);
            ps5.setDate(3, new Date(GO.getCurrentTime().getTimeInMillis()));
            ps5.setBigDecimal(4, FinalPrice);
            k = ps5.executeUpdate();
           
            
            //skini pare sa racuna kupca
            bi190502_BuyerOperations BO = new bi190502_BuyerOperations();
            k = BO.skini(buyerId, FinalPrice);
            if (k==-1){
                deleteOrder(orderId);
                System.out.println("Buyer nema dovoljno para");
            }
            
            
            //proveravamo da li je kupac porucio iz istog mesta iz kog je, ako jeste postavljamo ReceiveTime na trenutno
            //i State na arrived
            if(ukupnoDana == 0){
                ps8.setDate(1, new Date(GO.getCurrentTime().getTimeInMillis()));
                ps8.setInt(2, orderId);
                ps8.executeUpdate();
            }
        } catch (Exception e) {
        }
        return 1;
    }

    @Override
    public BigDecimal getFinalPrice(int orderId) {
        BigDecimal FinalPrice = new BigDecimal(-1);
        String state = null;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select State, FinalPrice from [Order] where IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next()){
                state = rs.getString(1);
                FinalPrice = rs.getBigDecimal(2);
            }
            if(state == null || state.equals("created"))return new BigDecimal(-1);
        } catch (Exception e) {
        }
        return FinalPrice;
    }

    @Override
    public BigDecimal getDiscountSum(int orderId) {
        BigDecimal discountSum = new BigDecimal(0);
        BigDecimal finalPriceNoDiscount = new BigDecimal(0);
        
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps1 = con.prepareStatement("select IdArticle, Count from Item where IdOrder = ?");
                PreparedStatement ps2 = con.prepareStatement("select Price from Article where IdArticle = ?")){
            
            ps1.setInt(1, orderId);
            ResultSet rs1 = ps1.executeQuery();
            
            while(rs1.next()){
                int articleId = rs1.getInt(1);
                ps2.setInt(1, articleId);
                ResultSet rs2 = ps2.executeQuery();
                
                int count = rs1.getInt(2);
                BigDecimal Count = new BigDecimal(count);
                BigDecimal price = null;
                if(rs2.next())price = rs2.getBigDecimal(1);
                BigDecimal multi = price.multiply(Count);
                finalPriceNoDiscount = multi.add(finalPriceNoDiscount);
            }
            
            //u finalPriceNoDiscount je cena bez Discounta
            BigDecimal finalPrice = getFinalPrice(orderId);
            if(finalPrice.equals(new BigDecimal(-1))) return finalPrice;
            
            discountSum = finalPriceNoDiscount.subtract(finalPrice);
        } catch (Exception e) {
        }
        return discountSum;
    }

    @Override
    public String getState(int orderId) {
        String state = null;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select State from [Order] where IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())state = rs.getString(1);
            
        } catch (Exception e) {
        }
        return state.strip();
    }

    @Override
    public Calendar getSentTime(int orderId) {
        Calendar time = Calendar.getInstance();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select SendingTime from [Order] where IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                Date sendingTime = rs.getDate(1);
                if(sendingTime != null)time.setTime(sendingTime);
                else return null; 
            }
        } catch (Exception e) {
        }
        
        return time;
    }

    @Override
    public Calendar getRecievedTime(int orderId) {
    Calendar time = Calendar.getInstance();
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select ReceivedTime from [Order] where IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                Date sendingTime = rs.getDate(1);
                if(sendingTime != null)time.setTime(sendingTime);
                else return null; 
            }
        } catch (Exception e) {
        }
        
        return time;
    }

    @Override
    public int getBuyer(int orderId) {
     int buyerId = -1;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdBuyer from [Order] where IdOrder = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if(rs.next())buyerId = rs.getInt(1);
            
        } catch (Exception e) {
        }
        
        return buyerId;}

    @Override
    public int getLocation(int orderId) {
        int success = -1;
        String State = null;
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select State from [Order] where IdOrder = ?");
                PreparedStatement ps1 = con.prepareStatement("select IdCity from Buyer where IdBuyer = ?");
                PreparedStatement ps2 = con.prepareStatement("select IdBuyer from [Order] where IdOrder = ?");
                PreparedStatement ps3 = con.prepareStatement("select IdShop from Shop where IdCity = ?")){
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            ps2.setInt(1, orderId);
            ResultSet rs2 = ps2.executeQuery();
            
            int buyerId = -1;
            if(rs2.next())buyerId = rs2.getInt(1);
            
            ps1.setInt(1, buyerId);
            ResultSet rs1 = ps1.executeQuery();
            int idCity = -1;
            if(rs1.next())idCity = rs1.getInt(1);
            
            //proveri jel imaprodavnicu kod sebe
            boolean ImaProdavnicu = false;
            int idShop = -1;
            ps3.setInt(1, idCity);
            ResultSet rs3 = ps3.executeQuery();
            if(rs3.next()) ImaProdavnicu = true;
            
            if(rs.next()){
                State = rs.getString(1).trim();
                if(State.equals("arrived"))return idCity;
                if(State.equals("created"))return -1;
            }
            
            bi190502_GeneralOperations GO = new bi190502_GeneralOperations();
            
            List<Node> lista =  putanjaOrdera.get(orderId);
            int prosloDana = (int) ((GO.getCurrentTime().getTimeInMillis() - getSentTime(orderId).getTimeInMillis()) / (24*60*60*1000));
            int pomDani = 0;
            for (int i = 0; i< lista.size(); i++) {
                pomDani+= lista.get(i).Distance;
                if(prosloDana < pomDani) return lista.get(i).Grad;
                else if(prosloDana == pomDani) return lista.get(i+1).Grad;
            }
            
        } catch (Exception e) {
        }
        return success;
    }
    
    
    public int deleteOrder(int id){
        Connection conn = DB.getInstance().getConnection();
        int res = -1;
        String s1 = "delete from dbo.[Order] where IdOrder =?";
         try(PreparedStatement ps = conn.prepareStatement(s1);){
             ps.setInt(1, id);
             int cnt = ps.executeUpdate();
             if(cnt > 0)res= 1;
         } catch (Exception ex) {
             Logger.getLogger(bi190502_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
         return res;
    }
}
