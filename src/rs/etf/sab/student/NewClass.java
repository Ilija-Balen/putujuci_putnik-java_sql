/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import rs.etf.sab.operations.ArticleOperations;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.CityOperations;
import rs.etf.sab.operations.GeneralOperations;
import rs.etf.sab.operations.OrderOperations;
import rs.etf.sab.operations.ShopOperations;
import rs.etf.sab.operations.TransactionOperations;

/**
 *
 * @author Maca
 */
public class NewClass {
     public static void main(String[] args) {
         ArticleOperations articleOperations = new bi190502_ArticleOperations(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new bi190502_BuyerOperations();
        CityOperations cityOperations = new bi190502_CityOperations();
        GeneralOperations generalOperations = new bi190502_GeneralOperations();
        OrderOperations orderOperations = new bi190502_OrderOperations();
        ShopOperations shopOperations = new bi190502_ShopOperations();
        TransactionOperations transactionOperations =new bi190502_TransactionOperations();
         generalOperations.eraseAll();
        final Calendar initialTime = Calendar.getInstance();
        initialTime.clear();
        initialTime.set(2018, 0, 1);
        generalOperations.setInitialTime(initialTime);
        final Calendar receivedTime = Calendar.getInstance();
        receivedTime.clear();
        receivedTime.set(2018, 0, 22);
        final int cityB = cityOperations.createCity("B");
        final int cityC1 = cityOperations.createCity("C1");
        final int cityA = cityOperations.createCity("A");
        final int cityC2 = cityOperations.createCity("C2");
        final int cityC3 = cityOperations.createCity("C3");
        final int cityC4 = cityOperations.createCity("C4");
        final int cityC5 = cityOperations.createCity("C5");
        cityOperations.connectCities(cityB, cityC1, 8);
        cityOperations.connectCities(cityC1, cityA, 10);
        cityOperations.connectCities(cityA, cityC2, 3);
        cityOperations.connectCities(cityC2, cityC3, 2);
        cityOperations.connectCities(cityC3, cityC4, 1);
        cityOperations.connectCities(cityC4, cityA, 3);
        cityOperations.connectCities(cityA, cityC5, 15);
        cityOperations.connectCities(cityC5, cityB, 2);
        final int shopA = shopOperations.createShop("shopA", "A");
        final int shopC2 = shopOperations.createShop("shopC2", "C2");
        final int shopC3 = shopOperations.createShop("shopC3", "C3");
        shopOperations.setDiscount(shopA, 20);
        shopOperations.setDiscount(shopC2, 50);
        final int laptop = articleOperations.createArticle(shopA, "laptop", 1000);
        final int monitor = articleOperations.createArticle(shopC2, "monitor", 200);
        final int stolica = articleOperations.createArticle(shopC3, "stolica", 100);
        final int sto = articleOperations.createArticle(shopC3, "sto", 200);
        shopOperations.increaseArticleCount(laptop, 20);
        shopOperations.increaseArticleCount(monitor, 10);
        shopOperations.increaseArticleCount(stolica, 10);
        shopOperations.increaseArticleCount(sto, 10);
        final int buyer = buyerOperations.createBuyer("kupac", cityB);
        buyerOperations.increaseCredit(buyer, new BigDecimal("20000"));
        final int order = buyerOperations.createOrder(buyer);
        orderOperations.addArticle(order, laptop, 5);
        orderOperations.addArticle(order, monitor, 4);
        orderOperations.addArticle(order, stolica, 10);
        orderOperations.addArticle(order, sto, 4);
        orderOperations.getFinalPrice(order);
        System.out.println(orderOperations.getSentTime(order)==null);
        System.out.println("created".equals(orderOperations.getState(order)));
        orderOperations.completeOrder(order);
        System.out.println("sent".equals(orderOperations.getState(order)));
        final int buyerTransactionId = transactionOperations.getTransationsForBuyer(buyer).get(0);
        System.out.println(initialTime.get(Calendar.MONTH) + " " + initialTime.get(Calendar.DAY_OF_MONTH));
        Calendar c = transactionOperations.getTimeOfExecution(buyerTransactionId);
        System.out.println(c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH));
        System.out.println(transactionOperations.getTransationsForShop(shopA)==null);
        final BigDecimal shopAAmount = new BigDecimal("5").multiply(new BigDecimal("1000")).setScale(3);
        final BigDecimal shopAAmountWithDiscount = new BigDecimal("0.8").multiply(shopAAmount).setScale(3);
        final BigDecimal shopC2Amount = new BigDecimal("4").multiply(new BigDecimal("200")).setScale(3);
        final BigDecimal shopC2AmountWithDiscount = new BigDecimal("0.5").multiply(shopC2Amount).setScale(3);
        final BigDecimal shopC3AmountWithDiscount;
        final BigDecimal shopC3Amount = shopC3AmountWithDiscount = new BigDecimal("10").multiply(new BigDecimal("100")).add(new BigDecimal("4").multiply(new BigDecimal("200"))).setScale(3);
        final BigDecimal amountWithoutDiscounts = shopAAmount.add(shopC2Amount).add(shopC3Amount).setScale(3);
        final BigDecimal amountWithDiscounts = shopAAmountWithDiscount.add(shopC2AmountWithDiscount).add(shopC3AmountWithDiscount).setScale(3);
        final BigDecimal systemProfit = amountWithDiscounts.multiply(new BigDecimal("0.05")).setScale(3);
        final BigDecimal shopAAmountReal = shopAAmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        final BigDecimal shopC2AmountReal = shopC2AmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        final BigDecimal shopC3AmountReal = shopC3AmountWithDiscount.multiply(new BigDecimal("0.95")).setScale(3);
        System.out.println(amountWithDiscounts + " " +  orderOperations.getFinalPrice(order).setScale(3));
        System.out.println(amountWithoutDiscounts.subtract(amountWithDiscounts).equals(orderOperations.getDiscountSum(order)));
        System.out.println(amountWithDiscounts + " " +  transactionOperations.getBuyerTransactionsAmmount(buyer));
        System.out.println(transactionOperations.getShopTransactionsAmmount(shopA) + " " +  new BigDecimal("0").setScale(3));
        System.out.println(transactionOperations.getShopTransactionsAmmount(shopC2) + " " +  new BigDecimal("0").setScale(3));
        System.out.println(transactionOperations.getShopTransactionsAmmount(shopC3) + " " +  new BigDecimal("0").setScale(3));
        System.out.println(new BigDecimal("0").setScale(3) + " " +  transactionOperations.getSystemProfit());
        generalOperations.time(2);
        c = orderOperations.getSentTime(order);
        System.out.println(initialTime.get(Calendar.MONTH) + " " + initialTime.get(Calendar.DAY_OF_MONTH));
        System.out.println(c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH));
        System.out.println(orderOperations.getRecievedTime(order)==null);
        System.out.println(orderOperations.getLocation(order) + " " +  cityA);
        generalOperations.time(9);
        System.out.println(orderOperations.getLocation(order) + " " +  cityA);
        generalOperations.time(8);
        System.out.println(orderOperations.getLocation(order) + " " +  cityC5);
        generalOperations.time(5);
        System.out.println(orderOperations.getLocation(order) + " " + cityB);
        System.out.println(receivedTime.get(Calendar.MONTH) + " " + receivedTime.get(Calendar.DAY_OF_MONTH));
        c = orderOperations.getRecievedTime(order);
        System.out.println(c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH));
        System.out.println(shopAAmountReal + " " +  transactionOperations.getShopTransactionsAmmount(shopA));
        System.out.println(shopC2AmountReal + " " +  transactionOperations.getShopTransactionsAmmount(shopC2));
        System.out.println(shopC3AmountReal + " " +  transactionOperations.getShopTransactionsAmmount(shopC3));
        System.out.println(systemProfit + " " +  transactionOperations.getSystemProfit());
        final int shopATransactionId = transactionOperations.getTransactionForShopAndOrder(order, shopA);
        System.out.println(-1L !=  shopATransactionId);
        c = transactionOperations.getTimeOfExecution(shopATransactionId);
        System.out.println(receivedTime.get(Calendar.MONTH) + " " + receivedTime.get(Calendar.DAY_OF_MONTH));
        System.out.println(c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH));
     }
}
