package it.polimi.tiw.catalog.beans;

import java.util.ArrayList;

public class Category {
	
	private int id;
	private String name;
	private String code;
	private int fatherId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getFatherId() {
		return fatherId;
	}
	public void setFatherId(int fatherId) {
		this.fatherId = fatherId;
	}
	
	public boolean belongsTo(ArrayList<Integer> subtreeIndexes) {
		return subtreeIndexes.contains(this.id);
	}

	public String indent() {
		String indent = "";
		for(int i = 0; i < this.code.length()-1; i++) {
			indent += ">";
		}
		return indent;
	}
}
