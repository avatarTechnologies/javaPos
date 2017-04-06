package com.systemonenoc.avatar.client;

import java.math.BigDecimal;

public class InvoiceLine {
	
	private String ean;
	private BigDecimal total;
	private String name;
	private BigDecimal quantity;
	private BigDecimal discount;
	private BigDecimal base;
	private String code;
	private String currentHash;
	
	public String getEan() {
		return ean;
	}
	
	public void setEan(String ean) {
		this.ean = ean;
	}
	
	public BigDecimal getTotal() {
		return total;
	}
	
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public BigDecimal getQuantity() {
		return quantity;
	}
	
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	
	public BigDecimal getDiscount() {
		return discount;
	}
	
	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}
	
	public BigDecimal getBase() {
		return base;
	}
	
	public void setBase(BigDecimal base) {
		this.base = base;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCurrentHash() {
		return currentHash;
	}
	
	public void setCurrentHash(String currentHash) {
		this.currentHash = currentHash;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InvoiceLine [ean=");
		builder.append(ean);
		builder.append(", total=");
		builder.append(total);
		builder.append(", name=");
		builder.append(name);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", discount=");
		builder.append(discount);
		builder.append(", base=");
		builder.append(base);
		builder.append(", code=");
		builder.append(code);
		builder.append(", currentHash=");
		builder.append(currentHash);
		builder.append("]");
		return builder.toString();
	}
	
}
