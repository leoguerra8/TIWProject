package it.polimi.tiw.catalog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.catalog.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection c) {
		this.connection = c;
	}
	
	public User checkCredentials(String username, String password) throws SQLException {
		String query = "SELECT id, username, name, surname FROM users WHERE username = ? AND password = ?";
		try (PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setString(1, username);
			pStatement.setString(2, password);
			try (ResultSet res = pStatement.executeQuery();) {
				if(!res.isBeforeFirst()) // there is no result
					return null;
				else {
					res.next();
					User user = new User();
					user.setId(res.getInt("id"));
					user.setUsername(res.getString("username"));
					user.setName(res.getString("name"));
					user.setSurname(res.getString("surname"));
					return user;
				}
			}
		}
	}

}
