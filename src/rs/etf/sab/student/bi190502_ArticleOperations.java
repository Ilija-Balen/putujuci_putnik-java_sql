/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.ArticleOperations;

/**
 *
 * @author Ilija
 */
public class bi190502_ArticleOperations implements ArticleOperations{

    public bi190502_ArticleOperations() {
    }

    @Override
    public int createArticle(int i, String string, int i1) {
       int IdArticle = -1;
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("insert into Article(IdShop,Name,Price) values(?, ?, ?)",PreparedStatement.RETURN_GENERATED_KEYS)){
            ps.setInt(1, i);
            ps.setString(2, string);
            ps.setInt(3, i1);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                IdArticle= rs.getInt(1);
            }
        } catch (Exception e) {
            Logger.getLogger(bi190502_ArticleOperations.class.getName()).log(Level.SEVERE, null, e);
        }
        return IdArticle;
    }
    
}
