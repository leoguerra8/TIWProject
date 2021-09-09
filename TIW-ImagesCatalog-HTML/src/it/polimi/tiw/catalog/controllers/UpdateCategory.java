package it.polimi.tiw.catalog.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
	    
	    CategoryDAO categoryDAO = new CategoryDAO(connection);
	    Boolean legitStartingCategory = true;
	    Boolean legitDestinationCategory = true;
	    try {
	    	legitStartingCategory = categoryDAO.legitCategory(Integer.parseInt(categoryId), oldCategoryCode, Integer.parseInt(oldFatherId));
	    	legitDestinationCategory = categoryDAO.legitDestinationCategory(Integer.parseInt(categoryId), Integer.parseInt(oldFatherId), Integer.parseInt(newFatherId));
	    } catch (SQLException e) {
	    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
	    }
	    
	    if (!(legitStartingCategory) || !(legitDestinationCategory)) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect destination!");
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
	    
	    String lastChildNewFatherCode;
	    String lastChildOldFatherCode;
		String newFatherCode;
		
		try {
			if(Integer.parseInt(oldFatherId) == 0) {
				lastChildOldFatherCode = categoryDAO.findMaxRootCode();
			} else {
				lastChildOldFatherCode = categoryDAO.findLastChildCode(Integer.parseInt(oldFatherId));
			}
			lastChildNewFatherCode = categoryDAO.findLastChildCode(Integer.parseInt(newFatherId));
			newFatherCode = categoryDAO.findCategoryCode(Integer.parseInt(newFatherId));

			int lastDigit = Character.getNumericValue(lastChildNewFatherCode.charAt(lastChildNewFatherCode.length()-1));
			if (lastDigit == MAX_CATEGORIES) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The number of sub-categories cannot be more that 9");
				return;
			} else {
				if (lastChildOldFatherCode.equals(newFatherCode)) {
					if(lastChildNewFatherCode.equals("-1")) newCategoryCode = oldCategoryCode + "1";
					else newCategoryCode = oldCategoryCode + String.valueOf(lastDigit+1);
				}
				if (lastChildNewFatherCode.equals("-1")) newCategoryCode = newFatherCode + "1";
				else newCategoryCode = lastChildNewFatherCode.substring(0, lastChildNewFatherCode.length()-1) + String.valueOf(lastDigit+1);
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