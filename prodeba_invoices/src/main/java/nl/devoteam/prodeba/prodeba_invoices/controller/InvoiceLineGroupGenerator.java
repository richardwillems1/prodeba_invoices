package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.udojava.evalex.Expression;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;
import nl.devoteam.prodeba.prodeba_invoices.model.InvoiceLine;
import nl.devoteam.prodeba.prodeba_invoices.model.InvoiceLineGroup;

public class InvoiceLineGroupGenerator 
{	
	private List<InvoiceLine> invoiceLines;
	private List<InvoiceLineGroup> invoiceLineGroups;
	
	private Connection con;
	
	public InvoiceLineGroupGenerator(Connection con)
	{
		this.con = con;
		invoiceLines = new ArrayList<InvoiceLine>();
		invoiceLineGroups = new ArrayList<InvoiceLineGroup>();
		
		try
		{
			fillInvoiceArray(invoiceRs(con));
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private ResultSet invoiceRs(Connection con) throws SQLException
	{
		Statement stmnt = con.createStatement();
		return stmnt.executeQuery("SELECT "
				+ "invoice_line_id,"
				+ "assessment_id,"
				+ "hour_id,"
				+ "client_code,"
				+ "company_code,"
				+ "assessment_finance_modality,"
				+ "assessment_unit,"
				+ "product_code,"
				+ "product_law,"
				+ "product_price_per_unit,"
				+ "invoice_line_date,"
				+ "invoice_line_volume_calculation,"
				+ "invoice_line_vat_tariff"
				+ " FROM invoice_lines_with_data");
	}
	
	private void fillInvoiceArray(ResultSet invoiceRs) throws SQLException
	{
		while(invoiceRs.next())
			invoiceLines.add(new InvoiceLine(invoiceRs));
	}
	
	public void generateInvoice(PeriodType periodType, String invoiceRange, Connection con) throws Exception
	{
		switch(periodType)
		{
			case MONTH:
				generateMonthlyInvoiceLineGroups(invoiceRange);
				break;
			case PERIOD:
				generatePeriodInvoiceLineGroups(invoiceRange);
				break;
		}
		
		writeInvoiceLines(con);
	}
	
	private void writeInvoiceLines(Connection con) throws Exception 
	{
		System.out.println("Writing records into database.");
		for(InvoiceLineGroup invoiceLineGroup : invoiceLineGroups)
		{
			calculateValues(invoiceLineGroup);
			Statement stmnt = con.createStatement();
			stmnt.execute(
					"INSERT INTO invoice_line_groups (invoice_line_group_volume, invoice_line_group_amount, invoice_line_group_vat)"
					+ "VALUES ("
					+ invoiceLineGroup.getInvoice_line_group_volume() + ","
					+ invoiceLineGroup.getInvoice_line_group_amount() + ","
					+ invoiceLineGroup.getInvoice_line_group_vat_amount() + ")");
			
			stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			int invoice_line_group_id = rs.getInt(1);
			
			invoiceLineGroup.setInvoice_line_group_id(invoice_line_group_id);
			
			for(InvoiceLine invoiceLine : invoiceLineGroup.getInvoiceLines())
			{
				stmnt = con.createStatement();
				stmnt.execute("INSERT INTO il_to_ilg (il_to_ilg_il, il_to_ilg_ilg)"
					+ "VALUES ("
					+ invoiceLine.getInvoice_line_id() + ","
					+ invoiceLineGroup.getInvoice_line_group_id() +")");
			}
		}
		System.out.println("All records written into database.");
	}
	
	private void calculateValues(InvoiceLineGroup invoiceLineGroup) throws Exception
	{
		String volumeCalculationTotal = "";
		
		for(InvoiceLine invoiceLine : invoiceLineGroup.getInvoiceLines())
			volumeCalculationTotal = volumeCalculationTotal.concat(invoiceLine.getInvoice_line_volume_calculation() + "+");
		
		volumeCalculationTotal = volumeCalculationTotal.substring(0, volumeCalculationTotal.length() -1);
		
		Expression expression = new Expression(volumeCalculationTotal);
		
		double invoice_line_group_volume = expression.eval().doubleValue();
		invoice_line_group_volume = new BigDecimal(String.valueOf(invoice_line_group_volume)).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		double invoice_line_group_amount = invoice_line_group_amount(invoiceLineGroup, invoice_line_group_volume);
		
		String invoice_line_group_vat_tariff = invoiceLineGroup.getInvoice_line_group_vat_tariff();
		double invoice_line_group_vat_amount = 0;
		
		if(invoice_line_group_vat_tariff.equals("0%") || invoice_line_group_vat_tariff.equals("Vrijgesteld"))
			invoice_line_group_vat_amount = 0;
		else if(invoice_line_group_vat_tariff.equals("6%"))
			invoice_line_group_vat_amount = invoice_line_group_amount * 0.06;
		else if(invoice_line_group_vat_tariff.equals("21%"))
			invoice_line_group_vat_amount = invoice_line_group_amount * 0.21;
		else
			throw new Exception("Invalid VAT tariff.");	
		
		invoiceLineGroup.setCalculatedValues(invoice_line_group_amount, invoice_line_group_volume, invoice_line_group_vat_amount);
	}
	
	private double invoice_line_group_amount(InvoiceLineGroup invoiceLineGroup, double invoice_line_group_volume) throws Exception 
	{
		String assessment_unit = invoiceLineGroup.getAssessment_unit();
		double product_price_per_unit = invoiceLineGroup.getProduct_price_per_unit();
		
		Statement stmnt = con.createStatement();
		ResultSet rs = stmnt.executeQuery("SELECT product_unit FROM external_products WHERE product_code = '" + invoiceLineGroup.getProduct_code() + "'");
		rs.next();
		String product_unit = rs.getString(1);

		
		if(assessment_unit.equals(product_unit))
			return invoice_line_group_volume * product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("uur"))
			return (invoice_line_group_volume / 60) * product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("dagdeel"))
			return (invoice_line_group_volume / 60 / 4) * product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("etmaal"))
			return (invoice_line_group_volume / 60 / 24) * product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("minuut"))
			return (invoice_line_group_volume * 60) * product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("dagdeel"))
			return (invoice_line_group_volume / 4) * product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("etmaal"))
			return (invoice_line_group_volume / 24) * product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("minuut"))
			return (invoice_line_group_volume * 4 * 60) * product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("uur"))
			return (invoice_line_group_volume * 4) * product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("etmaal"))
			return (invoice_line_group_volume / 6) * product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("minuut"))
			return (invoice_line_group_volume * 24 * 60) * product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("uur"))
			return (invoice_line_group_volume * 24) * product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("dagdeel"))
			return (invoice_line_group_volume * 6) * product_price_per_unit;
		else
			throw new Exception("No valid unit conversion for amount was possible.");
	}

	private void generateMonthlyInvoiceLineGroups(String invoiceRange) throws Exception 
	{
		LocalDate minDate;
		LocalDate maxDate;
		
		if(invoiceRange.equals("Augustus"))
		{
			minDate = LocalDate.parse("2018-08-01");
			maxDate = LocalDate.parse("2018-08-31");
		}
		else if(invoiceRange.equals("September"))
		{
			minDate = LocalDate.parse("2018-09-01");
			maxDate = LocalDate.parse("2018-09-30");
		}
		else
			throw new Exception("Invalid month provided.");
		
		for(InvoiceLine invoiceLine : invoiceLines)
		{
			if(invoiceLine.getInvoice_line_date().toLocalDate().isAfter(minDate.minusDays(1)) &&
					invoiceLine.getInvoice_line_date().toLocalDate().isBefore(maxDate.plusDays(1)))
				if(!addToInvoiceLineGroup(invoiceLine))
					invoiceLineGroups.add(new InvoiceLineGroup(invoiceLine.getCompany_code(), invoiceLine.getClient_code(), invoiceLine.getProduct_law(), invoiceLine.getProduct_code(), invoiceLine.getAssessment_unit(), invoiceLine, PeriodType.MONTH, invoiceRange, invoiceLine.getProduct_price_per_unit(), invoiceLine.getInvoice_line_vat_tariff()));									
		}
	}

	private void generatePeriodInvoiceLineGroups(String invoiceRange) throws Exception 
	{
		LocalDate minDate;
		LocalDate maxDate;
		
		if(invoiceRange.equals("Periode 8"))
		{
			minDate = LocalDate.parse("2018-08-01");
			maxDate = LocalDate.parse("2018-08-29");
		}
		else if(invoiceRange.equals("Periode 9"))
		{
			minDate = LocalDate.parse("2018-08-30");
			maxDate = LocalDate.parse("2018-09-27");
		}
		else if(invoiceRange.equals("Periode 10"))
		{
			minDate = LocalDate.parse("2018-09-28");
			maxDate = LocalDate.parse("2018-10-26");
		}
		else
			throw new Exception("Invalid period provided.");
		
		for(InvoiceLine invoiceLine : invoiceLines)
		{
			if(invoiceLine.getInvoice_line_date().toLocalDate().isAfter(minDate.minusDays(1)) &&
					invoiceLine.getInvoice_line_date().toLocalDate().isBefore(maxDate.plusDays(1)))
				if(!addToInvoiceLineGroup(invoiceLine))
					invoiceLineGroups.add(new InvoiceLineGroup(invoiceLine.getCompany_code(), invoiceLine.getClient_code(), invoiceLine.getProduct_law(), invoiceLine.getProduct_code(), invoiceLine.getAssessment_unit(), invoiceLine, PeriodType.PERIOD, invoiceRange, invoiceLine.getProduct_price_per_unit(), invoiceLine.getInvoice_line_vat_tariff()));									
		}
		
	}

	private boolean addToInvoiceLineGroup(InvoiceLine invoiceLine) 
	{
		for(InvoiceLineGroup invoiceLineGroup : invoiceLineGroups)
		{
			if(
					invoiceLineGroup.getClient_code().equals(invoiceLine.getClient_code()) &&
					invoiceLineGroup.getAssessment_unit().equals(invoiceLine.getAssessment_unit()) &&
					invoiceLineGroup.getProduct_code().equals(invoiceLine.getProduct_code()) &&
					invoiceLineGroup.getCompany_code().equals(invoiceLine.getCompany_code()))
			{
				invoiceLineGroup.addInvoiceLine(invoiceLine);
				return true;
			}
		}
		return false;
	}
}
