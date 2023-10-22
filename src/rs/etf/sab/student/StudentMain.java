package rs.etf.sab.student;



import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import rs.etf.sab.operations.*;
import org.junit.Test;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

import java.util.Calendar;

public class StudentMain {

    public static void main(String[] args) {
        
        
        
        ArticleOperations articleOperations = new bi190502_ArticleOperations(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new bi190502_BuyerOperations();
        CityOperations cityOperations = new bi190502_CityOperations();
        GeneralOperations generalOperations = new bi190502_GeneralOperations();
        OrderOperations orderOperations = new bi190502_OrderOperations();
        ShopOperations shopOperations = new bi190502_ShopOperations();
        TransactionOperations transactionOperations = new bi190502_TransactionOperations();
        
        
       
       
        
        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();
    }
}
