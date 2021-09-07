package it.polimi.tiw.catalog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.catalog.beans.Category;

public class CategoryDAO {
	private Connection connection;

	public CategoryDAO(Connection c) {
		this.connection = c;
	}

	public int createCategory(String name, String code, int fatherId) throws SQLException {

		String query = "INSERT INTO categories (name, code, father) VALUES (?, ?, ?)";
		try (PreparedStatement pStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pStatement.setString(1, name);
			pStatement.setString(2, code);
			if(fatherId == -1) {
				pStatement.setNull(3, Types.BIGINT);
			} else {
				pStatement.setInt(3, fatherId);
			}
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

		String query = "SELECT MAX(code) AS code FROM categories WHERE father = ?";

		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, categoryId);
			ResultSet res = pStatement.executeQuery();
			if (res.next()) {
				code = res.getString("code");
			}
			if (code != null ) return code;
			else return "-1";
		}
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

	public int getNumOfRoots() throws SQLException {
		String query = "SELECT count(*) AS rootsNo FROM categories WHERE father IS NULL";
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

	public ArrayList<Integer> getCategorySubtree(int categoryId) throws SQLException {
		ArrayList<Integer> subtreeIndexes = new ArrayList<>();

		String query = "SELECT id FROM categories WHERE code LIKE CONCAT('', (SELECT code FROM categories WHERE id = ?), '%', '');";
		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, categoryId);
			try (ResultSet res = pStatement.executeQuery();) {
				while (res.next()) {
					subtreeIndexes.add(res.getInt("id"));
				}
			}	
		}
		return subtreeIndexes;
	}

	public String findCategoryCode(int categoryId) throws SQLException {
		String code = null;

		String query = "SELECT code FROM categories WHERE id = ?"; 

		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, categoryId);
			ResultSet res = pStatement.executeQuery();
			if (res.next()) {
				code = res.getString("code");
			}
			return code;
		}
	}

	public String findMaxRootCode() {
		String query = "SELECT MAX(code) AS code FROM categories WHERE ISNULL(father)";
		try(Statement statement = connection.createStatement();) {	
			ResultSet result = statement.executeQuery(query);
			if(result.next()) {
				return result.getString("code");
			} else {
				throw new SQLException("Something went wrong while retrieving the last code of root categories");
			}
		} catch (SQLException | NumberFormatException e) {
			e.printStackTrace();
			return "-1";
		}
	}

	public void updateCategory(int categoryId, int oldFatherId, int newFatherId, String oldCategoryCode, String newCategoryCode) throws SQLException {
		String update1 = "UPDATE categories SET code = ?, father = ? WHERE id = ?";
		String query = "SELECT code FROM categories WHERE code LIKE CONCAT(?, '%', '') AND code != ?";
		String update2 = "UPDATE categories SET code = ? WHERE code = ?";
		String queryFill = "SELECT code FROM categories WHERE code LIKE CONCAT(?, '%', '')";
		try {
			connection.setAutoCommit(false);
			
			PreparedStatement pStatement1 = connection.prepareStatement(update1);
			PreparedStatement pStatement2 = connection.prepareStatement(query);
			
			// update the moved category
			pStatement1.setString(1, newCategoryCode);
			pStatement1.setInt(2, newFatherId);
			pStatement1.setInt(3, categoryId);
			pStatement1.executeUpdate();
			
			// find all the moved category descendants and update them accordingly
			pStatement2.setString(1, oldCategoryCode);
			pStatement2.setString(2, newCategoryCode);
			ResultSet res2 = pStatement2.executeQuery();
			while (res2.next()) {
				String oldChildCode = res2.getString("code");
				PreparedStatement pStatement3 = connection.prepareStatement(update2);
				String newChildCode = newCategoryCode + oldChildCode.substring(oldCategoryCode.length());
				pStatement3.setString(1, newChildCode);
				pStatement3.setString(2, oldChildCode);
				pStatement3.executeUpdate();
			}
			
			// fill the hole
			String lastBrotherCode = null;
			if (oldFatherId == 0) {
				lastBrotherCode = this.findMaxRootCode();
			} else {
				lastBrotherCode = this.findLastChildCode(oldFatherId);
			}
			
			// if the moved category had at least one bigger brother
			if (!lastBrotherCode.equals("-1") && lastBrotherCode.compareTo(oldCategoryCode) > 0) {
				PreparedStatement pStatement4 = connection.prepareStatement(update2);
				PreparedStatement pStatement5 = connection.prepareStatement(queryFill);
				
				// move the biggest brother in its old place
				pStatement4.setString(1, oldCategoryCode);
				pStatement4.setString(2, lastBrotherCode);
				pStatement4.executeUpdate();
				
				// move all the biggest brother's descendants accordingly
				pStatement5.setString(1, lastBrotherCode);
				ResultSet res5 = pStatement5.executeQuery();
				while (res5.next()) {
					String oldChildCode = res5.getString("code");
					PreparedStatement pStatement6 = connection.prepareStatement(update2);
					String newChildCode = oldCategoryCode + oldChildCode.substring(lastBrotherCode.length());
					pStatement6.setString(1, newChildCode);
					pStatement6.setString(2, oldChildCode);
					pStatement6.executeUpdate();
				}
			}
			connection.commit();
		} catch (SQLException e) {
			if (connection != null) {
				try {
					System.err.print("Transaction is being rolled back");
					connection.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();;
				}
			}
		}
	}
	
	public boolean existsCategory(String name) throws SQLException {
		String query = "SELECT * FROM categories WHERE name = ?";
		try(PreparedStatement pStatement = connection.prepareStatement(query);) {	
			pStatement.setString(1, name);
			ResultSet result = pStatement.executeQuery();
			if(result.next()) {
				return true;
			} else {
				return false;
			}
		}
	}

}