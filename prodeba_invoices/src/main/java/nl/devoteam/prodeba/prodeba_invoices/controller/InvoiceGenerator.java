package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;
import nl.devoteam.prodeba.prodeba_invoices.model.Invoice;
import nl.devoteam.prodeba.prodeba_invoices.model.InvoiceLineGroup;

public class InvoiceGenerator 
{
	private List<InvoiceLineGroup> invoiceLineGroups;
	private List<Invoice> invoices;
	private Connection con;
	
	public InvoiceGenerator(Connection con)
	{
		this.con = con;
		this.invoiceLineGroups = new ArrayList<InvoiceLineGroup>();
		this.invoices = new ArrayList<Invoice>();
		
		try 
		{
			fillInvoiceLineGroupArray(invoiceLineGroupRs());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void generateInvoices(PeriodType periodType, String invoiceRange) throws SQLException
	{
		for(InvoiceLineGroup invoiceLineGroup : invoiceLineGroups)
		{
			if(invoiceLineGroup.getInvoice_line_group_period_type().equals(periodType) && 
					invoiceLineGroup.getInvoice_line_group_range().equals(invoiceRange.toLowerCase()))
				if(!addToInvoice(invoiceLineGroup))
					invoices.add(new Invoice(invoiceLineGroup));
		}
		
		writeInvoices();
	}
	
	private boolean addToInvoice(InvoiceLineGroup invoice_line_group)
	{
		for(Invoice invoice : invoices)
		{
			if(invoice.getInvoice_period_type().equals(invoice_line_group.getInvoice_line_group_period_type()) &&
					invoice.getInvoice_invoice_range().equals(invoice_line_group.getInvoice_line_group_range()) &&
					invoice.getCompany_company_code().equals(invoice_line_group.getCompany_code()) &&
					invoice.getInvoice_finance_modality().equals(invoice_line_group.getInvoice_line_group_finance_modality()))
			{
				invoice.addInvoice_line_group(invoice_line_group);
				return true;
			}
		}
		return false;
	}

	private void writeInvoices() throws SQLException 
	{
		for(Invoice invoice: invoices)
		{
			calculateValues(invoice);
			Statement stmnt = con.createStatement();
			stmnt.execute(
					"INSERT INTO invoices ("
					+ "invoice_amount,"
					+ "invoice_vat,"
					+ "invoice_amount_including_vat,"
					+ "invoice_period_type,"
					+ "invoice_invoice_range,"
					+ "invoice_financing_modality,"
					+ "company_company_code)"
					+ "VALUES ("
					+ invoice.getInvoice_amount() + ","
					+ invoice.getInvoice_vat() + ","
					+ invoice.getInvoice_amount_including_vat() + ","
					+ "'" + invoice.getInvoice_period_type().toString().toLowerCase() + "',"
					+ "'" + invoice.getInvoice_invoice_range() + "',"
					+ "'" + invoice.getInvoice_finance_modality() + "',"
					+ "'" + invoice.getCompany_company_code() + "')");
			
			stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			invoice.setInvoice_invoice_id(rs.getInt(1));
			
			for(InvoiceLineGroup invoice_line_group : invoice.getInvoiceLineGroups())
			{
				stmnt = con.createStatement();
				stmnt.execute("INSERT INTO ilg_to_i (ilg_to_i_ilg, ilg_to_i_i)"
					+ "VALUES ("
					+ invoice_line_group.getInvoice_line_group_id() +","
					+ invoice.getInvoice_invoice_id() + ")");
			}
		}
	}

	private void calculateValues(Invoice invoice) 
	{
		double invoice_amount = 0.0;
		double invoice_vat = 0.0;
		
		for(InvoiceLineGroup invoice_line_group : invoice.getInvoiceLineGroups())
		{
			invoice_amount = invoice_amount + invoice_line_group.getInvoice_line_group_amount();
			invoice_amount = new BigDecimal(String.valueOf(invoice_amount)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			invoice_vat = invoice_vat + invoice_line_group.getInvoice_line_group_vat_amount();
			invoice_vat = new BigDecimal(String.valueOf(invoice_vat)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		
		double invoice_amount_including_vat = invoice_amount + invoice_vat;
		invoice_amount_including_vat = new BigDecimal(String.valueOf(invoice_amount_including_vat)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		invoice.setInvoice_amount(invoice_amount);
		invoice.setInvoice_vat(invoice_vat);
		invoice.setInvoice_amount_including_vat(invoice_amount_including_vat);
	}

	private ResultSet invoiceLineGroupRs() throws SQLException
	{
		Statement stmnt = con.createStatement();
		return stmnt.executeQuery("SELECT * FROM invoice_line_groups");
	}
	
	private void fillInvoiceLineGroupArray(ResultSet invoiceLineGroupRs) throws SQLException
	{
		while(invoiceLineGroupRs.next())
			invoiceLineGroups.add(new InvoiceLineGroup(invoiceLineGroupRs));
	}
}
