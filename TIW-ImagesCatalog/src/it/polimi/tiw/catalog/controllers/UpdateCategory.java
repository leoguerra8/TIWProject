package it.polimi.tiw.catalog.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.catalog.beans.Category;
import it.polimi.tiw.catalog.dao.CategoryDAO;
import it.polimi.tiw.catalog.utils.ConnectionHandler;

@WebServlet("/UpdateCategory")
public class UpdateCategory extends HttpServlet {
	private static final int MAX_CATEGORIES = 9;
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
    public UpdateCategory() {
        super();
    }
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	    String categoryId = null;
	    String oldFatherId = null;
	    String newFatherId = null;
	    String oldCategoryCode = null;
		String newCategoryCode = null;
		
	    categoryId = request.getParameter("categoryId");
	    oldFatherId = request.getParameter("oldFatherId");
	    newFatherId = request.getParameter("newFatherId");
	    oldCategoryCode = request.getParameter("oldCategoryCode");
	    
	    if (categoryId == null || categoryId.isEmpty() || 
	    		oldFatherId == null || oldFatherId.isEmpty() ||
	    		newFatherId == null || newFatherId.isEmpty()) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			return;
	    }
		
	    try {
	    	if (Integer.parseInt(categoryId) < 0 || Integer.parseInt(newFatherId) < 0) {
	    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Missing or incorrect parameters");
				return;
	    	}
	    } catch (NumberFormatException e) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			return;
	    }
	    
	    CategoryDAO categoryDAO = new CategoryDAO(connection);
	    
	    String lastChildCode;
		String fatherCode;
		
		try {
			lastChildCode = categoryDAO.findLastChildCode(Integer.parseInt(newFatherId));
			fatherCode = categoryDAO.findCategoryCode(Integer.parseInt(newFatherId));

			int lastDigit = Character.getNumericValue(lastChildCode.charAt(lastChildCode.length()-1));
			if (lastDigit == MAX_CATEGORIES) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The number of sub-categories cannot be more that 9");
				return;
			} else {
				if (lastChildCode.equals("-1")) newCategoryCode = fatherCode + "1";
				else newCategoryCode = lastChildCode.substring(0, lastChildCode.length()-1) + String.valueOf(lastDigit+1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
	    try {
	    	categoryDAO.updateCategory(Integer.parseInt(categoryId), Integer.parseInt(oldFatherId), Integer.parseInt(newFatherId), oldCategoryCode, newCategoryCode);
	    } catch (SQLException e) {
	    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to update category");
			return;
	    }
	    
	    String path = "/WEB-INF/templates/update.html";

	    response.sendRedirect("GoToHomePage");
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
