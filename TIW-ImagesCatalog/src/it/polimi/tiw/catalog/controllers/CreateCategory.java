package it.polimi.tiw.catalog.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
	private static final int MAX_CATEGORIES = 9;

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
		String code = null;

		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			fatherId = Integer.parseInt(request.getParameter("fatherId"));
		} catch (NumberFormatException | NullPointerException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing request parameters");
			return;
		}

		// Create Category
		int categoryId;
		boolean existsCategory;
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		
		try {
			existsCategory = categoryDAO.existsCategory(name);
			if(existsCategory) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("A category with this name already exists!");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if(fatherId == -1) { // The category is a root
			try {
			int rootsNo = categoryDAO.getNumOfRoots();
			if(rootsNo == MAX_CATEGORIES) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The number of root categories cannot be more that 9");
				return;
			} else { 
				code = String.valueOf(rootsNo+1);
			} 
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else { // The category is a sub-category
			String lastChildCode;
			String fatherCode;
			try {
				lastChildCode = categoryDAO.findLastChildCode(fatherId);
				fatherCode = categoryDAO.findCategoryCode(fatherId);

				int lastDigit = Character.getNumericValue(lastChildCode.charAt(lastChildCode.length()-1));
				if (lastDigit == MAX_CATEGORIES) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("The number of sub-categories cannot be more that 9");
					return;
				} else {
					if (lastChildCode.equals("-1")) code = fatherCode + "1";
					else code = lastChildCode.substring(0, lastChildCode.length()-1) + String.valueOf(lastDigit+1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			categoryId = categoryDAO.createCategory(name, code, fatherId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create category");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(categoryId);

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
