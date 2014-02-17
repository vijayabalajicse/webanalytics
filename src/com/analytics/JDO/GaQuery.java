package com.analytics.JDO;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GaQuery {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)	
	 @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
	String Id;
	
	@Persistent
	private String queryId;
	@Persistent
	private String gaDimension;
	@Persistent
	private String gaMetrics;
	@Persistent
	private String gaFilter;
	@Persistent
	private String gaStartDate;
	@Persistent
	private String gaEndDate;
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public String getGaDimension() {
		return gaDimension;
	}
	public void setGaDimension(String gaDimension) {
		this.gaDimension = gaDimension;
	}
	public String getGaMetrics() {
		return gaMetrics;
	}
	public void setGaMetrics(String gaMetrics) {
		this.gaMetrics = gaMetrics;
	}
	public String getGaFilter() {
		return gaFilter;
	}
	public void setGaFilter(String gaFilter) {
		this.gaFilter = gaFilter;
	}
	public String getGaStartDate() {
		return gaStartDate;
	}
	public void setGaStartDate(String gaStartDate) {
		this.gaStartDate = gaStartDate;
	}
	public String getGaEndDate() {
		return gaEndDate;
	}
	public void setGaEndDate(String gaEndDate) {
		this.gaEndDate = gaEndDate;
	}
}
