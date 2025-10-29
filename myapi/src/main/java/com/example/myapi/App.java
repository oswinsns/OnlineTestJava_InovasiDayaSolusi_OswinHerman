package com.example.myapi;

import static spark.Spark.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	//konfigurasi database
    	
//    	create tabel manual di mySQL, dengan database bernama teruntuk_ids (bisa sesuaikan dengan string url
    	//CREATE TABLE status (
//    	  id INT PRIMARY KEY,
//    	  name VARCHAR(50)
//    	);
//
//    	CREATE TABLE transactions (
//    	  id INT PRIMARY KEY,
//    	  productID VARCHAR(20),
//    	  productName VARCHAR(100),
//    	  amount DECIMAL(10,2),
//    	  customerName VARCHAR(100),
//    	  status INT,
//    	  transactionDate DATETIME,
//    	  createBy VARCHAR(50),
//    	  createOn DATETIME,
//    	  FOREIGN KEY (status) REFERENCES status(id)
//    	);
    	String url = "jdbc:mysql://localhost:3306/teruntuk_ids";
    	String user = "root";
    	String password = "";
    	
    	try(Connection con = DriverManager.getConnection(url, user, password)) {
    		
    		String kontenjson = Files.readString(Paths.get("viewData (4).json"));
    		JSONObject json = new JSONObject(kontenjson);
    		
    	// insert status array ke tabel status
    		
    		JSONArray statusArray = json.getJSONArray("status");
    		String insertStatus = "INSERT IGNORE INTO status (id, name) Values (?, ?)";
    		PreparedStatement psStatus = con.prepareStatement(insertStatus);
    		
    		for (int i =0; i < statusArray.length(); i++) {
    			//sytem.out.println("st.getInt("id)")
    			JSONObject st = statusArray.getJSONObject(i);
    			
    			  System.out.println("ID: " + st.getInt("id"));
    			  System.out.println("Name: " + st.getString("name"));
    			psStatus.setInt(1, st.getInt("id"));
    			psStatus.setString(2, st.getString("name"));
    			psStatus.addBatch();
    		}
    		
    		
    		psStatus.executeBatch();
    		psStatus.addBatch();
    		
    		// insert data array ke tabel status	
    		JSONArray dataArray = json.getJSONArray("data");
    		String insertTransaction = """
    				Insert IGNORE into transactions (id, productID, productName, amount, customerName, status, transactionDate,
    				 createBy, createOn)
    				VALUES (?,?,?,?,?,?,?,?,?)
    				""";
    		PreparedStatement psData = con.prepareStatement(insertTransaction);
    		
    		for(int i=0; i < dataArray.length(); i++) {
    			JSONObject dt = dataArray.getJSONObject(i);
    			psData.setInt(1,  dt.getInt("id"));
    			psData.setString(2, dt.getString("productID"));
    			psData.setString(3, dt.getString("productName"));
    			psData.setBigDecimal(4, new java.math.BigDecimal(dt.getString("amount")));
    			 psData.setString(5, dt.getString("customerName"));
                 psData.setInt(6, dt.getInt("status"));
                 psData.setTimestamp(7, Timestamp.valueOf(dt.getString("transactionDate")));
                 psData.setString(8, dt.getString("createBy"));
                 psData.setTimestamp(9, Timestamp.valueOf(dt.getString("createOn")));
                 psData.addBatch();
    		}
    		
    		psData.executeBatch();
    		psData.close();   
    		
    		System.out.println("✅ JSON data inserted successfully.");

    		
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	//cara 2: Insert manual
    	
//    	INSERT INTO transactions 
//    	(id, productID, productName, amount, customerName, status, transactionDate, createBy, createOn)
//    	VALUES
//    	(1372, '10001', 'Test 1', 1000, 'abc', 0, '2022-07-10 11:14:52', 'abc', '2022-07-10 11:14:52'),
//    	(1373, '10002', 'Test 2', 2000, 'abc', 0, '2022-07-11 13:14:52', 'abc', '2022-07-10 13:14:52'),
//    	(1374, '10001', 'Test 1', 1000, 'abc', 0, '2022-08-10 12:14:52', 'abc', '2022-07-10 12:14:52'),
//    	(1375, '10002', 'Test 2', 1000, 'abc', 1, '2022-08-10 13:10:52', 'abc', '2022-07-10 13:10:52'),
//    	(1376, '10001', 'Test 1', 1000, 'abc', 0, '2022-08-10 13:11:52', 'abc', '2022-07-10 13:11:52'),
//    	(1377, '10002', 'Test 2', 2000, 'abc', 0, '2022-08-12 13:14:52', 'abc', '2022-07-10 13:14:52'),
//    	(1378, '10001', 'Test 1', 1000, 'abc', 0, '2022-08-12 14:11:52', 'abc', '2022-07-10 14:11:52'),
//    	(1379, '10002', 'Test 2', 1000, 'abc', 1, '2022-09-13 11:14:52', 'abc', '2022-07-10 11:14:52'),
//    	(1380, '10001', 'Test 1', 1000, 'abc', 0, '2022-09-13 13:14:52', 'abc', '2022-07-10 13:14:52'),
//    	(1381, '10002', 'Test 2', 2000, 'abc', 0, '2022-09-14 09:11:52', 'abc', '2022-07-10 09:11:52'),
//    	(1382, '10001', 'Test 1', 1000, 'abc', 0, '2022-09-14 10:14:52', 'abc', '2022-07-10 10:14:52'),
//    	(1383, '10002', 'Test 2', 1000, 'abc', 1, '2022-08-15 13:14:52', 'abc', '2022-07-10 13:14:52');
    	
    	
    	port(8080);
    	
    	// GET /data → gabungkan status + transactions
          get("/data", (req, res) -> {
        	  res.type("application/json");
        	  
        	  JSONObject response = new JSONObject();
        	  JSONArray getStatusArray = new JSONArray();
        	  JSONArray getTransactionArray = new JSONArray();
        	  
        	  try (Connection con = DriverManager.getConnection(url, user, password)) {
                  // Ambil data status dari database
                  Statement stmtStatus = con.createStatement();
                  ResultSet rsStatus = stmtStatus.executeQuery("SELECT * FROM status");
                  while (rsStatus.next()) {
                      JSONObject st = new JSONObject();
                      st.put("id", rsStatus.getInt("id"));
                      st.put("name", rsStatus.getString("name"));
                      getStatusArray.put(st);
                  }
                  rsStatus.close();
                  stmtStatus.close();
                  
               // Ambil data transactions
                  Statement stmtTrans = con.createStatement();
                  ResultSet rsTrans = stmtTrans.executeQuery("SELECT * FROM transactions");
                  while (rsTrans.next()) {
                      JSONObject dt = new JSONObject();
                      dt.put("id", rsTrans.getInt("id"));
                      dt.put("productID", rsTrans.getString("productID"));
                      dt.put("productName", rsTrans.getString("productName"));
                      dt.put("amount", rsTrans.getBigDecimal("amount"));
                      dt.put("customerName", rsTrans.getString("customerName"));
                      dt.put("status", rsTrans.getInt("status"));
                      dt.put("transactionDate", rsTrans.getTimestamp("transactionDate"));
                      dt.put("createBy", rsTrans.getString("createBy"));
                      dt.put("createOn", rsTrans.getTimestamp("createOn"));
                      getTransactionArray.put(dt);
                  }
                  rsTrans.close();
                  stmtTrans.close();
                  
               // Gabungkan ke dalam satu objek JSON
                  response.put("status", getStatusArray);
                  response.put("data", getTransactionArray);
                  
        	  } catch (Exception e) {
        		  res.status(500);
                  return new JSONObject().put("error", e.getMessage()).toString();
        	  }
        	  
        	  return response.toString();
          });   	
          
          System.out.println("API running at http://localhost:8080/data");
    }
}
