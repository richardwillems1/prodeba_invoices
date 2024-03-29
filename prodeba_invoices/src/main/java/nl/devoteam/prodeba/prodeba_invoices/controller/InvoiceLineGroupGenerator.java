package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
			fillInvoiceLineArray(invoiceLineRs());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private ResultSet invoiceLineRs() throws SQLException
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
	
	private void fillInvoiceLineArray(ResultSet invoiceRs) throws SQLException
	{
		while(invoiceRs.next())
			invoiceLines.add(new InvoiceLine(invoiceRs));
	}
	
	public void generateInvoiceLineGroups(PeriodType periodType, String invoiceRange) throws Exception
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
		
		writeInvoiceLineGroups();
	}
	
	private void writeInvoiceLineGroups() throws Exception 
	{
		for(InvoiceLineGroup invoiceLineGroup : invoiceLineGroups)
		{
			calculateValues(invoiceLineGroup);
			Statement stmnt = con.createStatement();
			stmnt.execute(
					"INSERT INTO invoice_line_groups ("
					+ "invoice_line_group_volume,"
					+ "invoice_line_group_amount,"
					+ "invoice_line_group_vat,"
					+ "invoice_line_group_period_type,"
					+ "invoice_line_group_invoice_range,"
					+ "invoice_line_group_unit,"
					+ "invoice_line_group_finance_modality,"
					+ "invoice_line_group_law,"
					+ "invoice_line_group_price_per_unit,"
					+ "invoice_line_group_price_per_unit_calculation,"
					+ "invoice_line_group_volume_calculation,"
					+ "invoice_line_group_amount_calculation,"
					+ "company_company_code,"
					+ "product_product_code,"
					+ "client_client_code)"
					+ "VALUES ("
					+ invoiceLineGroup.getInvoice_line_group_volume() + ","
					+ invoiceLineGroup.getInvoice_line_group_amount() + ","
					+ invoiceLineGroup.getInvoice_line_group_vat_amount() + ","
					+ "'" + invoiceLineGroup.getInvoice_line_group_period_type().toString().toLowerCase() + "',"
					+ "'" + invoiceLineGroup.getInvoice_line_group_range().toLowerCase() + "',"
					+ "'" + invoiceLineGroup.getInvoice_line_group_unit() + "',"
					+ "'" + invoiceLineGroup.getInvoice_line_group_finance_modality() + "',"
					+ "'" + invoiceLineGroup.getInvoice_line_group_law() + "',"
					+ invoiceLineGroup.getInvoice_line_group_price_per_unit() + ","
					+ "'" + invoiceLineGroup.getInvoice_line_group_price_per_unit_calculation() + "',"
					+ "'" + invoiceLineGroup.getInvoice_line_group_volume_calculation() + "',"
					+ "'" + invoiceLineGroup.getInvoice_line_group_amount_calculation() + "',"
					+ "'" + invoiceLineGroup.getCompany_code() + "',"
					+ "'" + invoiceLineGroup.getProduct_code() + "',"
					+ "'" + invoiceLineGroup.getClient_code() + "')");
			
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
	}
	
	private void calculateValues(InvoiceLineGroup invoiceLineGroup) throws Exception
	{
		int invoice_information_volume_decimals = invoice_information_volume_decimals(invoiceLineGroup);
		String invoice_information_rounding_rule = invoice_information_rounding_rule(invoiceLineGroup); 
		
		String invoice_line_group_volume_calculation = "(";
		String invoice_line_group_amount_calculation = "";
		
		for(InvoiceLine invoiceLine : invoiceLineGroup.getInvoiceLines())
			invoice_line_group_volume_calculation = invoice_line_group_volume_calculation.concat(invoiceLine.getInvoice_line_volume_calculation() + ")+(");
		
		invoice_line_group_volume_calculation = invoice_line_group_volume_calculation.substring(0, invoice_line_group_volume_calculation.length() -2);
		
		Expression expression = new Expression(invoice_line_group_volume_calculation);
		
		BigDecimal invoice_line_group_volume = expression.eval();
		
		invoice_line_group_volume = invoice_line_group_volume.setScale(5, RoundingMode.HALF_UP);
		invoice_line_group_volume = invoice_line_group_volume.setScale(invoice_information_volume_decimals, roundingMethod(invoice_information_rounding_rule));
		
		double invoice_line_group_price_per_unit = invoice_line_group_price_per_unit(invoiceLineGroup);
		
		double invoice_line_group_amount = invoice_line_group_price_per_unit * invoice_line_group_volume.doubleValue();
		invoice_line_group_amount = new BigDecimal(String.valueOf(invoice_line_group_amount)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		invoice_line_group_amount_calculation = invoice_line_group_volume + "*" + invoice_line_group_price_per_unit;
		
		String invoice_line_group_vat_tariff = invoiceLineGroup.getInvoice_line_group_vat_tariff();
		double invoice_line_group_vat_amount = 0;
		
		String invoice_line_group_price_per_unit_calculation = invoice_line_group_price_per_unit_calculation(invoiceLineGroup);
		
		if(invoice_line_group_vat_tariff.equals("0%") || invoice_line_group_vat_tariff.equals("Vrijgesteld"))
			invoice_line_group_vat_amount = 0;
		else if(invoice_line_group_vat_tariff.equals("6%"))
			invoice_line_group_vat_amount = invoice_line_group_amount * 0.06;
		else if(invoice_line_group_vat_tariff.equals("21%"))
			invoice_line_group_vat_amount = invoice_line_group_amount * 0.21;
		else
			throw new Exception("Invalid VAT tariff.");	
		
		invoice_line_group_vat_amount = new BigDecimal(String.valueOf(invoice_line_group_vat_amount)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		invoiceLineGroup.setCalculatedValues(invoice_line_group_amount, 
				invoice_line_group_volume.doubleValue(), 
				invoice_line_group_vat_amount, 
				invoice_line_group_price_per_unit, 
				invoice_line_group_price_per_unit_calculation,
				invoice_line_group_volume_calculation,
				invoice_line_group_amount_calculation);
	}
	
	private int roundingMethod(String invoice_information_rounding_method) throws Exception
	{
		switch(invoice_information_rounding_method)
		{
		case "Rekenkundig":
			return BigDecimal.ROUND_HALF_UP;
		case "Boven":
			return BigDecimal.ROUND_UP;
		case "Beneden":
			return BigDecimal.ROUND_DOWN;
		default:
			throw new Exception("Cannot determine rounding method.");
		}
	}
	
	private String invoice_information_rounding_rule(InvoiceLineGroup invoiceLineGroup) throws SQLException
	{
		Statement stmnt = con.createStatement();
		ResultSet rs = stmnt.executeQuery(""
				+ "SELECT invoice_information_rounding_rule " 
				+ "FROM invoice_information "
				+ "WHERE company_code = '" + invoiceLineGroup.getCompany_code() + "' AND "
				+ "invoice_information_law = '" + invoiceLineGroup.getInvoice_line_group_law() + "'");
		rs.next();
		return rs.getString(1);
	}
	
	private int invoice_information_volume_decimals(InvoiceLineGroup invoiceLineGroup) throws SQLException
	{
		Statement stmnt = con.createStatement();
		ResultSet rs = stmnt.executeQuery(""
				+ "SELECT company_type "
				+ "FROM commissioning_companies "
				+ "WHERE company_code = '" + invoiceLineGroup.getCompany_code() + "'");
		rs.next();		
		
		String company_type = rs.getString(1);
		String finance_modality = invoiceLineGroup.getInvoice_line_group_finance_modality();
		
		if(company_type.equals("Gemeente") && finance_modality.equals("ZIN"))
			return 0;
		else
			return 2;
	}
		
	private String invoice_line_group_price_per_unit_calculation(InvoiceLineGroup invoiceLineGroup) throws Exception
	{
		String assessment_unit = invoiceLineGroup.getInvoice_line_group_unit();
		String product_unit = product_unit(invoiceLineGroup.getProduct_code());
		double product_price_per_unit = invoiceLineGroup.getProduct_price_per_unit();
		
		if(assessment_unit.equals(product_unit))
			return Double.toString(product_price_per_unit);
		else if(assessment_unit.equals("minuut") && product_unit.equals("uur"))
			return product_price_per_unit + "/60";
		else if(assessment_unit.equals("minuut") && product_unit.equals("dagdeel"))
			return product_price_per_unit + "/60/4";
		else if(assessment_unit.equals("minuut") && product_unit.equals("etmaal"))
			return product_price_per_unit + "/60/24";
		else if(assessment_unit.equals("uur") && product_unit.equals("minuut"))
			return product_price_per_unit + "*60";
		else if(assessment_unit.equals("uur") && product_unit.equals("dagdeel"))
			return product_price_per_unit + "/4";
		else if(assessment_unit.equals("uur") && product_unit.equals("etmaal"))
			return product_price_per_unit + "/24";
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("minuut"))
			return product_price_per_unit + "*4*60";
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("uur"))
			return product_price_per_unit + "*4";
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("etmaal"))
			return product_price_per_unit + "/6";
		else if(assessment_unit.equals("etmaal") && product_unit.equals("minuut"))
			return product_price_per_unit + "*24*60";
		else if(assessment_unit.equals("etmaal") && product_unit.equals("uur"))
			return product_price_per_unit + "*24";
		else if(assessment_unit.equals("etmaal") && product_unit.equals("dagdeel"))
			return product_price_per_unit + "*6";
		else
			throw new Exception("No valid calculation for the invoice line group price per unit could be determined.");
	}
	
	private double invoice_line_group_price_per_unit(InvoiceLineGroup invoiceLineGroup) throws SQLException, Exception
	{
		String assessment_unit = invoiceLineGroup.getInvoice_line_group_unit();
		String product_unit = product_unit(invoiceLineGroup.getProduct_code());
		double product_price_per_unit = invoiceLineGroup.getProduct_price_per_unit();
		
		double invoice_line_price_per_unit;
		
		if(assessment_unit.equals(product_unit))
			return product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("uur"))
			invoice_line_price_per_unit = product_price_per_unit / 60;
		else if(assessment_unit.equals("minuut") && product_unit.equals("dagdeel"))
			invoice_line_price_per_unit = product_price_per_unit / 60 / 4;
		else if(assessment_unit.equals("minuut") && product_unit.equals("etmaal"))
			invoice_line_price_per_unit = product_price_per_unit / 60 / 24;
		else if(assessment_unit.equals("uur") && product_unit.equals("minuut"))
			invoice_line_price_per_unit = product_price_per_unit * 60;
		else if(assessment_unit.equals("uur") && product_unit.equals("dagdeel"))
			invoice_line_price_per_unit = product_price_per_unit / 4;
		else if(assessment_unit.equals("uur") && product_unit.equals("etmaal"))
			invoice_line_price_per_unit = product_price_per_unit / 24;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("minuut"))
			invoice_line_price_per_unit = product_price_per_unit * 4 * 60;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("uur"))
			invoice_line_price_per_unit = product_price_per_unit * 4;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("etmaal"))
			invoice_line_price_per_unit = product_price_per_unit / 6;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("minuut"))
			invoice_line_price_per_unit = product_price_per_unit * 24 * 60;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("uur"))
			invoice_line_price_per_unit = product_price_per_unit * 24;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("dagdeel"))
			invoice_line_price_per_unit = product_price_per_unit * 6;
		else
			throw new Exception("No valid unit conversion for price per unit was possible.");
		
		return new BigDecimal(String.valueOf(invoice_line_price_per_unit)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	private String product_unit(String product_code) throws SQLException
	{
		Statement stmnt = con.createStatement();
		ResultSet rs = stmnt.executeQuery("SELECT product_unit FROM external_products WHERE product_code = '" + product_code + "'");
		rs.next();
		return rs.getString(1);
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
			if(invoice_information_period(invoiceLine).equals("Maand") &&
					invoiceLine.getInvoice_line_date().toLocalDate().isAfter(minDate.minusDays(1)) &&
					invoiceLine.getInvoice_line_date().toLocalDate().isBefore(maxDate.plusDays(1)))
				if(!addToInvoiceLineGroup(invoiceLine))
					invoiceLineGroups.add(new InvoiceLineGroup(invoiceLine.getCompany_code(), invoiceLine.getClient_code(), invoiceLine.getProduct_law(), invoiceLine.getProduct_code(), invoiceLine.getAssessment_unit(), invoiceLine, PeriodType.MONTH, invoiceRange, invoiceLine.getProduct_price_per_unit(), invoiceLine.getInvoice_line_vat_tariff(), assessment_assessment_finance_modality(invoiceLine)));									
		}
	}
	
	private String assessment_assessment_finance_modality(InvoiceLine invoice_Line) throws SQLException
	{
		Statement stmnt = con.createStatement();
		String queryString = "SELECT DISTINCT assessments.assessment_finance_modality " + 
				"FROM invoice_lines LEFT JOIN " +  
				"assessments ON assessments.assessment_id = invoice_lines.assessment_id " + 
				"WHERE invoice_lines.invoice_line_id = "+ invoice_Line.getInvoice_line_id();
		ResultSet financeModalityRs = stmnt.executeQuery(queryString);
		
		financeModalityRs.next();
		return financeModalityRs.getString(1);
	}
	
	private String invoice_information_period(InvoiceLine invoiceLine) throws SQLException
	{
		Statement stmnt = con.createStatement();
		String queryString = "SELECT invoice_information.invoice_information_period "
				+ "FROM invoice_information "
				+ "WHERE invoice_information.company_code = '" + invoiceLine.getCompany_code() + "' AND invoice_information.invoice_information_law = '" + invoiceLine.getProduct_law() + "'";
		ResultSet periodRs = stmnt.executeQuery(queryString);
		
		periodRs.next();
		return periodRs.getString(1);
	}

	private void generatePeriodInvoiceLineGroups(String invoiceRange) throws Exception 
	{
		LocalDate minDate;
		LocalDate maxDate;
		
		if(invoiceRange.equals("Periode 8"))
		{
			minDate = LocalDate.parse("2018-08-01");
			maxDate = LocalDate.parse("2018-08-28");
		}
		else if(invoiceRange.equals("Periode 9"))
		{
			minDate = LocalDate.parse("2018-08-29");
			maxDate = LocalDate.parse("2018-09-25");
		}
		else if(invoiceRange.equals("Periode 10"))
		{
			minDate = LocalDate.parse("2018-09-26");
			maxDate = LocalDate.parse("2018-10-23");
		}
		else
			throw new Exception("Invalid period provided.");
		
		for(InvoiceLine invoiceLine : invoiceLines)
		{
			if(invoice_information_period(invoiceLine).equals("Per 4 weken") &&
					invoiceLine.getInvoice_line_date().toLocalDate().isAfter(minDate.minusDays(1)) &&
					invoiceLine.getInvoice_line_date().toLocalDate().isBefore(maxDate.plusDays(1)))
				if(!addToInvoiceLineGroup(invoiceLine))
					invoiceLineGroups.add(new InvoiceLineGroup(invoiceLine.getCompany_code(), invoiceLine.getClient_code(), invoiceLine.getProduct_law(), invoiceLine.getProduct_code(), invoiceLine.getAssessment_unit(), invoiceLine, PeriodType.PERIOD, invoiceRange, invoiceLine.getProduct_price_per_unit(), invoiceLine.getInvoice_line_vat_tariff(), assessment_assessment_finance_modality(invoiceLine)));									
		}
		
	}

	private boolean addToInvoiceLineGroup(InvoiceLine invoiceLine) 
	{
		for(InvoiceLineGroup invoiceLineGroup : invoiceLineGroups)
		{
			if
			(
					invoiceLineGroup.getClient_code().equals(invoiceLine.getClient_code()) &&
					invoiceLineGroup.getInvoice_line_group_unit().equals(invoiceLine.getAssessment_unit()) &&
					invoiceLineGroup.getProduct_code().equals(invoiceLine.getProduct_code()) &&
					invoiceLineGroup.getCompany_code().equals(invoiceLine.getCompany_code()) &&
					invoiceLineGroup.getInvoice_line_group_finance_modality().equals(invoiceLine.getAssessment_finance_modality())
			)
			{
				invoiceLineGroup.addInvoiceLine(invoiceLine);
				return true;
			}
		}
		return false;
	}
}
