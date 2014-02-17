package com.analytics.JDO;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class EmailDetails {
    
	private String fromAddress;
	private String toAddress;
	private Long frequent;
	private Long subfreq;	
	private long queryId;
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public Long getfrequent() {
		return frequent;
	}
	public void setfrequent(Long frequent) {
		this.frequent = frequent;
	}
	
	public long getQueryId() {
		return queryId;
	}
	public void setQueryId(long queryId) {
		this.queryId = queryId;
	}
	public Long getSubfreq() {
		return subfreq;
	}
	public void setSubfreq(Long subfreq) {
		this.subfreq = subfreq;
	}
}
