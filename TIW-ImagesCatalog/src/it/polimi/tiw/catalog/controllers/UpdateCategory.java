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
		
	    categoryId = request.getParameter("categoryId");
	    oldFatherId = request.getParameter("oldFatherId");
	    newFatherId = request.getParameter("newFatherId");
	    if (categoryId == null || categoryId.isEmpty() || 
	    		oldFatherId == null || oldFatherId.isEmpty() ||
	    		newFatherId == null || newFatherId.isEmpty()) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			return;
	    }
		
	    try {
	    	if (Integer.parseInt(categoryId) < 0) {
	    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Missing or incorrect parameters");
				return;
	    	}
	    } catch (NumberFormatException e) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or incorrect parameters");
			return;
	    }
	    
	    List<Category> categories = null;
	    CategoryDAO categoryDAO = new CategoryDAO(connection);
	    
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
