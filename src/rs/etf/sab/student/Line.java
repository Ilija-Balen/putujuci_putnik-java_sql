/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ilija
 */
public class Line {

    int findFurthestCityShop(int najbliziGradProdavnica, List<Integer> shopCities) {
        int graph[][];
        graph = new int[mapa.size()][mapa.size()];
        
        for (int i = 0; i< mapa.size();i++)
        {
            List<Node> lista = mapa.get(i+1);
            for(int j=0;j < mapa.size();j++){
                graph[i][j]=0;
            }
            for(int j = 0; j < lista.size();j++){
                graph[i][lista.get(j).Grad-1] = lista.get(j).rastojanje;
            }
        }
        //u dist je udaljenost svih od najbliGradProdavnica cvora
        int dist[] = dijkstra(graph, najbliziGradProdavnica-1, mapa.size());
        List<Integer> allShopCities = new ArrayList<>();
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity from Shop")){
            
            ResultSet rs = ps.executeQuery();
            while(rs.next())allShopCities.add(rs.getInt(1));
        
        } catch (Exception e) {}
        
        int max = 0;
        int grad = -1;
        
        for(int i = 0; i < allShopCities.size(); i++){
            if(max < dist[allShopCities.get(i)-1]){
                max = dist[allShopCities.get(i)-1];
                grad = allShopCities.get(i);
            }
        }
        
        return grad;
    }

    List<bi190502_OrderOperations.Node> napraviPutanju(int najdalji, int najbliziGradProdavnica) {
        List<bi190502_OrderOperations.Node> listaPutanja = new ArrayList<>();
        
        int graph[][];
        graph = new int[mapa.size()][mapa.size()];
        
        for (int i = 0; i< mapa.size();i++)
        {
            List<Node> lista = mapa.get(i+1);
            for(int j=0;j < mapa.size();j++){
                graph[i][j]=0;
            }
            for(int j = 0; j < lista.size();j++){
                graph[i][lista.get(j).Grad-1] = lista.get(j).rastojanje;
            }
        }
        bi190502_OrderOperations ord = new bi190502_OrderOperations();
        List<Integer> pomlista = findShortestPath(graph, najdalji-1, najbliziGradProdavnica-1);
        
        for(int i= 0; i<pomlista.size();i++){
            bi190502_OrderOperations.Node n = ord.new Node();
            if(i < pomlista.size()-1)
                n.Distance = graph[pomlista.get(i)][pomlista.get(i+1)];
            else n.Distance = 0;
            
            n.Grad = pomlista.get(i)+1;
            
            listaPutanja.add(n);
        }
        
        return listaPutanja;
    }
    
    public class Node{
       public int Grad;
       public int rastojanje; 
    }
    
    Map<Integer, List<Node>> mapa = new HashMap<>();
    
    public void popuni(){
        
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity, IdC2, Distance from Line");
                PreparedStatement ps1 = con.prepareStatement("select max(IdCity) from City");
               ){
            
            ResultSet rs1 = ps1.executeQuery();
            int max = -1;
            if(rs1.next())max = rs1.getInt(1);
            
            for(int i = 0; i<max;i++){
                List<Node> lista = new ArrayList<>();
                mapa.put(i+1,lista);
            }
            
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                int IdCity = rs.getInt(1);
                int IdC2 = rs.getInt(2);
                int Distance= rs.getInt(3);
                
                List<Node> lista = mapa.get(IdCity);
                Node n = new Node();
                n.Grad = IdC2;
                n.rastojanje = Distance;
                lista.add(n);
                mapa.put(IdCity, lista);
                
                lista = mapa.get(IdC2);
                Node n1 = new Node();
                n1.Grad = IdCity;
                n1.rastojanje = Distance;
                lista.add(n1);
                mapa.put(IdC2, lista);
            }
        } catch (Exception e) {
        }
    }
    
    public int findNearestCityShop(int cityId){
        
        int graph[][];
        graph = new int[mapa.size()][mapa.size()];
        
        for (int i = 0; i< mapa.size();i++)
        {
            List<Node> lista = mapa.get(i+1);
            for(int j=0;j < mapa.size();j++){
                graph[i][j]=0;
            }
            for(int j = 0; j < lista.size();j++){
                graph[i][lista.get(j).Grad-1] = lista.get(j).rastojanje;
            }
        }
        //u dist je udaljenost svih od cityIdCvora cvora
        int dist[] = dijkstra(graph, cityId-1, mapa.size());
        
        List<Integer> allShopCities = new ArrayList<>();
        Connection con = DB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("select IdCity from Shop")){
            
            ResultSet rs = ps.executeQuery();
            while(rs.next())allShopCities.add(rs.getInt(1));
        
        } catch (Exception e) {}
        
        int min = Integer.MAX_VALUE;
        int grad = -1;
        
        for(int i = 0; i < allShopCities.size(); i++){
            if(min > dist[allShopCities.get(i)-1]){
                min = dist[allShopCities.get(i)-1];
                grad = allShopCities.get(i);
            }
        }
        
        return grad;
    }
    
    int[] dijkstra(int graph[][], int src, int V)
    {
        int dist[] = new int[V]; // The output array.
                                 // dist[i] will hold
        // the shortest distance from src to i
 
        // sptSet[i] will true if vertex i is included in
        // shortest path tree or shortest distance from src
        // to i is finalized
        Boolean sptSet[] = new Boolean[V];
 
        // Initialize all distances as INFINITE and stpSet[]
        // as false
        for (int i = 0; i < V; i++) {
            dist[i] = Integer.MAX_VALUE;
            sptSet[i] = false;
        }
 
        // Distance of source vertex from itself is always 0
        dist[src] = 0;
 
        // Find shortest path for all vertices
        for (int count = 0; count < V - 1; count++) {
            // Pick the minimum distance vertex from the set
            // of vertices not yet processed. u is always
            // equal to src in first iteration.
            int u = minDistance(dist, sptSet, V);
 
            // Mark the picked vertex as processed
            sptSet[u] = true;
 
            // Update dist value of the adjacent vertices of
            // the picked vertex.
            for (int v = 0; v < V; v++)
 
                // Update dist[v] only if is not in sptSet,
                // there is an edge from u to v, and total
                // weight of path from src to v through u is
                // smaller than current value of dist[v]
                if (!sptSet[v] && graph[u][v] != 0
                    && dist[u] != Integer.MAX_VALUE
                    && dist[u] + graph[u][v] < dist[v])
                    dist[v] = dist[u] + graph[u][v];
        }
        
        //U DIST je NIZ DISTANCI OD IZVORA koji je ovde obelezen sa 0
        return dist;
    }
    
     int minDistance(int dist[], Boolean sptSet[], int V)
    {
        // Initialize min value
        int min = Integer.MAX_VALUE, min_index = -1;
 
        for (int v = 0; v < V; v++)
            if (sptSet[v] == false && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }
 
        return min_index;
    }
     
     
     public static List<Integer> findShortestPath(int[][] nodes, int startNode, int endNode) {
        int numNodes = nodes.length;
        boolean[] visited = new boolean[numNodes];
        int[] distances = new int[numNodes];
        int[] previous = new int[numNodes];
        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(previous, -1);

        distances[startNode] = 0;

        for (int i = 0; i < numNodes - 1; i++) {
            int currentNode = minDistance(distances, visited);
            visited[currentNode] = true;

            for (int neighbor = 0; neighbor < numNodes; neighbor++) {
                if (!visited[neighbor] && nodes[currentNode][neighbor] != 0 &&
                        distances[currentNode] != Integer.MAX_VALUE &&
                        distances[currentNode] + nodes[currentNode][neighbor] < distances[neighbor]) {
                    distances[neighbor] = distances[currentNode] + nodes[currentNode][neighbor];
                    previous[neighbor] = currentNode;
                }
            }
        }

        List<Integer> shortestPath = new ArrayList<>();
        int current = endNode;
        
        while (current != -1) {
            shortestPath.add(0, current);
            current = previous[current];
        }

        return shortestPath;
    }

    public static int minDistance(int[] distances, boolean[] visited) {
        int minDist = Integer.MAX_VALUE;
        int minIndex = -1;
        int numNodes = distances.length;

        for (int node = 0; node < numNodes; node++) {
            if (!visited[node] && distances[node] <= minDist) {
                minDist = distances[node];
                minIndex = node;
            }
        }

        return minIndex;
    }
}
