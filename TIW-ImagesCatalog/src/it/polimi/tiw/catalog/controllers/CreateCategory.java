package it.polimi.tiw.catalog.controllers;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.catalog.dao.CategoryDAO;
import it.polimi.tiw.catalog.utils.ConnectionHandler;

@WebServlet("/CreateCategory")
@MultipartConfig
public class CreateCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection = null;
	
	public CreateCategory() {
		super();
	}
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		boolean isBadRequest = false;
		String name = null;
		Integer fatherId = null;
		
		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			fatherId = Integer.parseInt(request.getParameter("fatherId"));
		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing request parameters");
			return;
		}
		
		// Create Category
		int categoryId;
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		// TODO
	}
}
