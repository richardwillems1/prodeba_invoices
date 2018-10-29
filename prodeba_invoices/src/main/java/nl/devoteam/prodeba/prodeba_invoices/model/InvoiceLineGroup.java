package nl.devoteam.prodeba.prodeba_invoices.model;

import java.sql.ResultSet;
import java.sql.SQLException;
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
	private String invoice_line_group_unit; 
	private String invoice_line_group_invoice_range;
	private double product_price_per_unit;
	private String invoice_line_vat_tariff;
	private PeriodType invoice_line_group_period_type;
	private String invoice_line_group_finance_modality;
	private double invoice_line_group_amount;
	private double invoice_line_group_volume;
	private double invoice_line_group_vat_amount;
	private String invoice_line_group_law;
	
	private boolean valuesCalculated = false;
	
	public InvoiceLineGroup(String company_code, String client_code, String product_law, String product_code, String assessment_unit, InvoiceLine invoiceLine, PeriodType periodType, String invoiceRange, double product_price_per_unit, String invoice_line_vat_tariff, String invoice_line_group_finance_modality)
	{
		invoiceLines = new ArrayList<InvoiceLine>();
		this.company_code = company_code;
		this.client_code = client_code;
		this.product_law = product_law;
		this.product_code = product_code;
		this.invoice_line_group_unit = assessment_unit;
		invoiceLines.add(invoiceLine);
		this.invoice_line_group_period_type = periodType;
		this.invoice_line_group_invoice_range = invoiceRange;
		this.product_price_per_unit = product_price_per_unit;
		this.invoice_line_vat_tariff = invoice_line_vat_tariff;
		this.invoice_line_group_finance_modality = invoice_line_group_finance_modality;
	}
	
	public InvoiceLineGroup(ResultSet invoiceLineGroupRs)
	{
		try
		{
			this.invoice_line_group_id = invoiceLineGroupRs.getInt(1);
			this.invoice_line_group_volume = invoiceLineGroupRs.getDouble(2);
			this.invoice_line_group_amount = invoiceLineGroupRs.getDouble(3);
			this.invoice_line_group_vat_amount = invoiceLineGroupRs.getDouble(4);
			this.invoice_line_group_period_type = PeriodType.valueOf(invoiceLineGroupRs.getString(5).toUpperCase());
			this.invoice_line_group_invoice_range = invoiceLineGroupRs.getString(6);
			this.invoice_line_group_unit = invoiceLineGroupRs.getString(7);
			this.invoice_line_group_finance_modality = invoiceLineGroupRs.getString(8);
			this.invoice_line_group_law = invoiceLineGroupRs.getString(9);
			this.company_code = invoiceLineGroupRs.getString(10);
			this.product_code = invoiceLineGroupRs.getString(11);
			this.client_code = invoiceLineGroupRs.getString(12);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
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

	public String getInvoice_line_group_unit() {
		return invoice_line_group_unit;
	}
	
	public PeriodType invoice_line_group_period_type() {
		return invoice_line_group_period_type;
	}
	
	public String getInvoice_line_group_range() {
		return invoice_line_group_invoice_range;
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
	
	public PeriodType getInvoice_line_group_period_type() {
		return invoice_line_group_period_type;
	}

	public int getInvoice_line_group_id() {
		return invoice_line_group_id;
	}

	public void setInvoice_line_group_id(int invoice_line_group_id) {
		this.invoice_line_group_id = invoice_line_group_id;
	}

	public String getInvoice_line_group_invoice_range() {
		return invoice_line_group_invoice_range;
	}

	public String getInvoice_line_vat_tariff() {
		return invoice_line_vat_tariff;
	}

	public String getInvoice_line_group_finance_modality() {
		return invoice_line_group_finance_modality;
	}

	public String getInvoice_line_group_law() {
		return invoice_line_group_law;
	}

	public boolean isValuesCalculated() {
		return valuesCalculated;
	}
}
