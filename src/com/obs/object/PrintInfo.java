package com.obs.object;

public class PrintInfo {

	private String name;
	private String receiptId;
	private String clientId;
	private String clientName;
	private String hardwareDetails;
	private String paymentDate;
	private String paymentCode;
	private String amountPaid;
	private String remarks;

	public String getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getHardwareDetails() {
		return hardwareDetails;
	}

	public void setHardwareDetails(String hardwareDetails) {
		this.hardwareDetails = hardwareDetails;
	}
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}


	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPaymentCode() {
		return paymentCode;
	}

	public void setPaymentCode(String paymentCode) {
		this.paymentCode = paymentCode;
	}

	public String getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(String amountPaid) {
		this.amountPaid = amountPaid;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getName() {
		return name;
	}

	public String setName(String name) {
		return this.name = name;
	}

}