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

@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public GoToHomePage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		this.templateEngine.setMessageResolver(new SharedPropertyMessageResolver(servletContext, "i18n", "home"));
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		List<Category> categories;
		try {
			categories = categoryDAO.findAllCategories();
			String path = "/WEB-INF/templates/home.html";
			final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
			ctx.setVariable("categories", categories);
			ctx.setVariable("onSelection", false);
			ctx.setVariable("subtreeIndexes", new ArrayList<Integer>());
			templateEngine.process(path, ctx, response.getWriter());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(500, "Database access failed: GET GoToHomePage");
			response.setCharacterEncoding("ISO-8859-1");
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
