package cn.chinattclub.searcher.dto;

import java.util.List;

/**
 * TODO Put here a description of what this class does.
 *
 * @author zhangying.
 *         Created 2015-1-20.
 */
public class SearchParam {
	 private String database;
	  private String category;
	  private List<String> conditions;
	  private String text;
	  private int pageSize;
	  private int pageNo;
	  private String sortField;
	  private boolean descending;
	  private int rows;
	  private String ID;

	  public String getDatabase()
	  {
	    return this.database;
	  }
	  public void setDatabase(String database) {
	    this.database = database;
	  }
	  public String getCategory() {
	    return this.category;
	  }
	  public void setCategory(String category) {
	    this.category = category;
	  }
	  public List<String> getConditions() {
	    return this.conditions;
	  }
	  public void setConditions(List<String> conditions) {
	    this.conditions = conditions;
	  }
	  public String getText() {
	    return this.text;
	  }
	  public void setText(String text) {
	    this.text = text;
	  }
	  public int getPageSize() {
	    return this.pageSize;
	  }
	  public void setPageSize(int pageSize) {
	    this.pageSize = pageSize;
	  }
	  public int getPageNo() {
	    return this.pageNo;
	  }
	  public void setPageNo(int pageNo) {
	    this.pageNo = pageNo;
	  }
	  public String getSortField() {
	    return this.sortField;
	  }
	  public void setSortField(String sortField) {
	    this.sortField = sortField;
	  }
	  public boolean isDescending() {
	    return this.descending;
	  }
	  public void setDescending(boolean descending) {
	    this.descending = descending;
	  }
	  public int getRows() {
	    return this.rows;
	  }
	  public void setRows(int rows) {
	    this.rows = rows;
	  }
	  public String getID() {
	    return this.ID;
	  }
	  public void setID(String iD) {
	    this.ID = iD;
	  }
}
