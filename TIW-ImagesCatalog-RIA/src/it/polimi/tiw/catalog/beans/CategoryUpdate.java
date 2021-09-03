package it.polimi.tiw.catalog.beans;

public class CategoryUpdate {
	
	private int categoryId;
	private String oldCategoryCode;
	private int oldFatherId;
	private int newFatherId;
	private String newFatherCode;
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getOldCategoryCode() {
		return oldCategoryCode;
	}
	public void setOldCategoryCode(String oldCategoryCode) {
		this.oldCategoryCode = oldCategoryCode;
	}
	public int getOldFatherId() {
		return oldFatherId;
	}
	public void setOldFatherId(int oldFatherId) {
		this.oldFatherId = oldFatherId;
	}
	public int getNewFatherId() {
		return newFatherId;
	}
	public void setNewFatherId(int newFatherId) {
		this.newFatherId = newFatherId;
	}
	public String getNewFatherCode() {
		return newFatherCode;
	}
	public void setNewFatherCode(String newFatherCode) {
		this.newFatherCode = newFatherCode;
	}
}
