package nl.devoteam.prodeba.prodeba_invoices.model;

import java.util.ArrayList;
import java.util.List;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;

public class Invoice 
{
	private List<InvoiceLineGroup> invoiceLineGroups;
	
	private int invoice_invoice_id;
	private double invoice_amount;
	private double invoice_vat;
	private double invoice_amount_including_vat;
	private String company_company_code;
	private PeriodType invoice_period_type;
	private String invoice_invoice_range;
	private String invoice_finance_modality;

	public Invoice(InvoiceLineGroup invoice_line_group)
	{
		this.invoiceLineGroups = new ArrayList<InvoiceLineGroup>();
		this.invoiceLineGroups.add(invoice_line_group);
		this.invoice_period_type = invoice_line_group.getInvoice_line_group_period_type();
		this.invoice_invoice_range = invoice_line_group.getInvoice_line_group_range();
		this.company_company_code = invoice_line_group.getCompany_code();
		this.invoice_finance_modality = invoice_line_group.getInvoice_line_group_finance_modality();
	}
	
	public void addInvoice_line_group(InvoiceLineGroup invoice_line_group)
	{
		this.invoiceLineGroups.add(invoice_line_group);
	}
	
	public int getInvoice_invoice_id() {
		return invoice_invoice_id;
	}

	public List<InvoiceLineGroup> getInvoiceLineGroups() {
		return invoiceLineGroups;
	}

	public double getInvoice_amount() {
		return invoice_amount;
	}

	public double getInvoice_vat() {
		return invoice_vat;
	}

	public double getInvoice_amount_including_vat() {
		return invoice_amount_including_vat;
	}

	public String getCompany_company_code() {
		return company_company_code;
	}

	public PeriodType getInvoice_period_type() {
		return invoice_period_type;
	}

	public String getInvoice_invoice_range() {
		return invoice_invoice_range;
	}

	public String getInvoice_finance_modality() {
		return invoice_finance_modality;
	}

	public void setInvoice_invoice_id(int invoice_invoice_id) {
		this.invoice_invoice_id = invoice_invoice_id;
	}

	public void setInvoice_amount(double invoice_amount) {
		this.invoice_amount = invoice_amount;
	}

	public void setInvoice_vat(double invoice_vat) {
		this.invoice_vat = invoice_vat;
	}

	public void setInvoice_amount_including_vat(double invoice_amount_including_vat) {
		this.invoice_amount_including_vat = invoice_amount_including_vat;
	}
}
