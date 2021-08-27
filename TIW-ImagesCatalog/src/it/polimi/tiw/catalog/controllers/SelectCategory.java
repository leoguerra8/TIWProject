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
import it.polimi.tiw.catalog.utils.SharedPropertyMessageResolver;

@WebServlet("/SelectCategory")
public class SelectCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
    public SelectCategory() {
        super();
    }
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		this.templateEngine.setMessageResolver(new SharedPropertyMessageResolver(servletContext, "i18n", "select"));
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	    String categoryId = null;
	    String fatherId = null;
	    String categoryCode = null;
		
	    categoryId = request.getParameter("categoryId");
	    fatherId = request.getParameter("fatherId");
	    categoryCode = request.getParameter("categoryCode");
	    if (categoryId == null || categoryId.isEmpty() || 
	    		fatherId == null || fatherId.isEmpty() ||
	    		categoryCode == null || categoryCode.isEmpty()) {
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
	    try {
	    	categories = categoryDAO.findAllCategories();
	    } catch (SQLException e) {
	    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
	    }
	    
	    ArrayList<Integer> subtreeIndexes = null;
	    try {
	    	subtreeIndexes = categoryDAO.getCategorySubtree(Integer.parseInt(categoryId));
	    } catch (SQLException e) {
	    	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
	    }
	    
	    String path = "/WEB-INF/templates/update.html";
	   
	    final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
	    ctx.setVariable("categories", categories);
	    ctx.setVariable("categoryId", categoryId);
	    ctx.setVariable("fatherId", fatherId);
	    ctx.setVariable("subtreeIndexes", subtreeIndexes);
	    ctx.setVariable("categoryCode", categoryCode);
	    response.setCharacterEncoding("UTF-8");
	    templateEngine.process(path, ctx, response.getWriter());
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
