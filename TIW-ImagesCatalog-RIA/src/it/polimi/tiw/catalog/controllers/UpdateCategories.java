package it.polimi.tiw.catalog.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.catalog.beans.CategoryUpdate;
import it.polimi.tiw.catalog.dao.CategoryDAO;
import it.polimi.tiw.catalog.utils.ConnectionHandler;

@WebServlet("/UpdateCategories")
public class UpdateCategories extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    public UpdateCategories() {
        super();
    }
    
    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) {
			  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			  response.getWriter().println("Missing or incorrect parameters");
			  e.printStackTrace(); 
		  }
		  
		Gson gson = new Gson();
		Type categoryListType = new TypeToken<ArrayList<CategoryUpdate>>(){}.getType();
		ArrayList<CategoryUpdate> categoryUpdateArray = null;
		
		try {
			categoryUpdateArray = gson.fromJson(jb.toString(), categoryListType);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			e.printStackTrace();
		}
	    
	    CategoryDAO categoryDAO = new CategoryDAO(connection);
	    
	    try {
	    	String message = categoryDAO.updateCategories(categoryUpdateArray);
	    	if (message.equals("OK")) {
	    		response.setStatus(HttpServletResponse.SC_OK);
	    	} else {
	    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    		response.getWriter().println(message);
	    	}
	    } catch (SQLException e) {
	    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to update category");
			return;
	    }
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
