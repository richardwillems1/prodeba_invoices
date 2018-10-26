package nl.devoteam.prodeba.prodeba_invoices.model;

import java.util.ArrayList;
import java.util.List;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;

public class InvoiceLineGroup 
{
	private int invoice_line_group_id;
	private List<InvoiceLine> invoiceLines;
	private String company_code;
	private String client_code;
	private String product_law;
	private String product_code;
	private String assessment_unit; 
	private PeriodType periodType;
	private String invoice_range;
	private double product_price_per_unit;
	private String invoice_line_vat_tariff;
	
	private double invoice_line_group_amount;
	private double invoice_line_group_volume;
	private double invoice_line_group_vat_amount;
	
	private boolean valuesCalculated = false;
	
	public InvoiceLineGroup(String company_code, String client_code, String product_law, String product_code, String assessment_unit, InvoiceLine invoiceLine, PeriodType periodType, String invoiceRange, double product_price_per_unit, String invoice_line_vat_tariff)
	{
		invoiceLines = new ArrayList<InvoiceLine>();
		this.company_code = company_code;
		this.client_code = client_code;
		this.product_law = product_law;
		this.product_code = product_code;
		this.assessment_unit = assessment_unit;
		invoiceLines.add(invoiceLine);
		this.periodType = periodType;
		this.invoice_range = invoiceRange;
		this.product_price_per_unit = product_price_per_unit;
		this.invoice_line_vat_tariff = invoice_line_vat_tariff;
	}
	
	public void addInvoiceLine(InvoiceLine invoiceLine)
	{
		invoiceLines.add(invoiceLine);
	}
	
	public void setCalculatedValues(double invoice_line_group_amount, double invoice_line_group_volume, double invoice_line_group_vat_amount)
	{
		this.invoice_line_group_amount = invoice_line_group_amount;
		this.invoice_line_group_volume = invoice_line_group_volume;
		this.invoice_line_group_vat_amount = invoice_line_group_vat_amount;
		this.valuesCalculated = true; 
	}
	
	public List<InvoiceLine> getInvoiceLines() {
		return invoiceLines;
	}

	public String getCompany_code() {
		return company_code;
	}

	public String getClient_code() {
		return client_code;
	}

	public String getProduct_law() {
		return product_law;
	}
	
	public String getProduct_code() {
		return product_code;
	}

	public String getAssessment_unit() {
		return assessment_unit;
	}
	
	public PeriodType periodType() {
		return periodType;
	}
	
	public String getInvoice_range() {
		return invoice_range;
	}
	
	public double getProduct_price_per_unit() {
		return product_price_per_unit;
	}

	public double getInvoice_line_group_vat_amount() {
		return invoice_line_group_vat_amount;
	}
	
	public double getInvoice_line_group_amount() {
		return invoice_line_group_amount;
	}
	
	public double getInvoice_line_group_volume() {
		return invoice_line_group_volume;
	}
	
	public String getInvoice_line_group_vat_tariff() {
		return invoice_line_vat_tariff;
	}
	
	public boolean valuesCalculated() {
		return valuesCalculated;
	}

	public int getInvoice_line_group_id() {
		return invoice_line_group_id;
	}

	public void setInvoice_line_group_id(int invoice_line_group_id) {
		this.invoice_line_group_id = invoice_line_group_id;
	}
}
