package nl.devoteam.prodeba.prodeba_invoices.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceLine 
{
	private Date invoice_line_date;
	private String client_code;
	private int assessment_id;
	private int hour_id;
	private String product_code;
	private double invoice_line_amount;
	private String invoice_line_amount_calculation;
	private double invoice_line_volume;
	private String assessment_unit;
	private String invoiceLine_volume_calculation;
	private double invoice_line_vat_amount;
	private String invoice_line_vat_tariff;
	private String invoice_line_vat_calculation;
	private int invoice_line_id;
	private String company_code;
	private String assessment_finance_modality;
	private String product_law;
	private double product_price_per_unit;
	
	public InvoiceLine(
			Date invoiceLine_date,
			String client_code,
			int assessment_id,
			int hour_id,
			String product_code,
			double invoiceLine_amount,
			String invoiceLine_amount_calculation,
			double invoiceLine_volume,
			String assessment_unit,
			String invoiceLine_volume_calculation,
			double vat_amount,
			String vat_tariff,
			String vat_calculation)
	{
		this.invoice_line_date = invoiceLine_date;
		this.client_code = client_code;
		this.assessment_id = assessment_id;
		this.hour_id = hour_id;
		this.product_code = product_code;
		this.invoice_line_amount = invoiceLine_amount;
		this.invoice_line_amount_calculation = invoiceLine_amount_calculation;
		this.invoice_line_volume = invoiceLine_volume;
		this.assessment_unit = assessment_unit;
		this.invoiceLine_volume_calculation = invoiceLine_volume_calculation;	
		this.invoice_line_vat_amount = vat_amount;
		this.invoice_line_vat_tariff = vat_tariff;
		this.invoice_line_vat_calculation = vat_calculation;
	}
	
	public InvoiceLine(ResultSet invoiceRs)
	{
		try
		{
			invoice_line_id = invoiceRs.getInt(1);
			assessment_id = invoiceRs.getInt(2);
			hour_id = invoiceRs.getInt(3);
			client_code = invoiceRs.getString(4);
			company_code = invoiceRs.getString(5);
			assessment_finance_modality = invoiceRs.getString(6);
			assessment_unit = invoiceRs.getString(7);
			product_code = invoiceRs.getString(8);
			product_law = invoiceRs.getString(9);
			product_price_per_unit = invoiceRs.getDouble(10);
			invoice_line_date = invoiceRs.getDate(11);
			invoiceLine_volume_calculation = invoiceRs.getString(12);
			invoice_line_vat_tariff = invoiceRs.getString(13);			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Date getInvoice_line_date() {
		return invoice_line_date;
	}

	public String getClient_code() {
		return client_code;
	}

	public int getAssessment_id() {
		return assessment_id;
	}
	
	public int getHour_id() {
		return hour_id;
	}

	public String getProduct_code() {
		return product_code;
	}

	public double getInvoice_line_amount() {
		return invoice_line_amount;
	}

	public String getInvoice_line_amount_calculation() {
		return invoice_line_amount_calculation;
	}

	public double getInvoice_line_volume() {
		return invoice_line_volume;
	}
	
	public String getAssessment_unit() {
		return assessment_unit;
	}

	public String getInvoice_line_volume_calculation() {
		return invoiceLine_volume_calculation;
	}

	public double getInvoice_line_vat_amount() {
		return invoice_line_vat_amount;
	}

	public String getInvoice_line_vat_tariff() {
		return invoice_line_vat_tariff;
	}

	public String getInvoice_line_vat_calculation() {
		return invoice_line_vat_calculation;
	}

	public int getInvoice_line_id() {
		return invoice_line_id;
	}
	
	public String getCompany_code() {
		return company_code;
	}
	
	public String getAssessment_finance_modality() {
		return assessment_finance_modality;
	}
	
	public String getProduct_law() {
		return product_law;
	}
	
	public double getProduct_price_per_unit() {
		return product_price_per_unit;
	}
}
