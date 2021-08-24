package it.polimi.tiw.catalog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.catalog.beans.Category;

public class CategoryDAO {
	private Connection connection;

	public CategoryDAO(Connection c) {
		this.connection = c;
	}

	public int createCategory(String name, String code, int fatherId) throws SQLException {

		String query = "INSERT into categories (name, code, father) VALUES (?, ?, ?)";
		try (PreparedStatement pStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pStatement.setString(1, name);
			pStatement.setString(2, code);
			pStatement.setInt(3, fatherId);
			pStatement.executeUpdate();

			ResultSet generatedKeys = pStatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Something went wrong while creating category: no ID has been obtained.");
			}
		}
	}

	public String findLastChildCode(int categoryId) throws SQLException {
		String code = null;

		String query = "SELECT MAX(code) FROM categories WHERE father = ?";

		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, categoryId);
			ResultSet res = pStatement.executeQuery();
			if (res.next()) {
				code = res.getString("code");
			}
		}

		return code;
	}

	public List<Category> findCategoriesByFather(int fatherId) throws SQLException {
		List<Category> categories = new ArrayList<Category>();

		String query = "SELECT * FROM categories WHERE father = ? ORDER BY code";
		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, fatherId);
			try (ResultSet res = pStatement.executeQuery();) {
				while (res.next()) {
					Category category = new Category();
					category.setId(res.getInt("id"));
					category.setName(res.getString("name"));
					category.setCode(res.getString("code"));
					category.setFatherId(fatherId);
				}
			}
		}
		return categories;
	}

	public List<Category> findAllCategories() throws SQLException {
		List<Category> categories = new ArrayList<Category>();

		String query = "SELECT * FROM categories ORDER BY code";
		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			try (ResultSet res = pStatement.executeQuery();) {
				while (res.next()) {
					Category category = new Category();
					category.setId(res.getInt("id"));
					category.setName(res.getString("name"));
					category.setCode(res.getString("code"));
					try { 
						category.setFatherId(res.getInt("father"));
					} catch (NullPointerException e) {
						category.setFatherId(-1);
					}
					categories.add(category);
				}
			}
		}
		return categories;
	}

	public int updateCategory(int categoryId, String code, int fatherId) throws SQLException {
		String query = "UPDATE categories SET code = ?, fatherId = ? WHERE categoryId = ? ";
		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setString(1, code);
			pStatement.setInt(2,  fatherId);
			pStatement.setInt(3, categoryId);

			pStatement.executeUpdate();

			ResultSet generatedKeys = pStatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Something went wrong while updating category: no ID has been obtained.");
			}
		}
	}

	public int getNumOfRoots() throws SQLException {
		String query = "SELECT count(*) AS rootsNo FROM categories WHERE fatherId = NULL";
		try(Statement statement = connection.createStatement();) {	
			ResultSet result = statement.executeQuery(query);
			if(result.next()) {
				return result.getInt("rootsNo");
			} else {
				throw new SQLException("Something went wrong while retrieving the number of root categories");
			}
		} catch (SQLException | NumberFormatException e) {
			e.printStackTrace();
			return -1;
		}
	}

	}
