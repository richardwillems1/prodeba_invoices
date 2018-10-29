package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nl.devoteam.prodeba.prodeba_invoices.model.HourRecord;
import nl.devoteam.prodeba.prodeba_invoices.model.InvoiceLine;

public class InvoiceLineGenerator 
{
	private List<HourRecord> hourRecords;
	private List<InvoiceLine> invoiceLines;
	private Connection con;
	private ResultSet hoursRs;
	
	public InvoiceLineGenerator(Connection con) throws SQLException
	{
		this.con = con;
		this.hoursRs = getHourRecords();
		this.hourRecords = new ArrayList<HourRecord>();
		this.invoiceLines = new ArrayList<InvoiceLine>();
	}
	
	public void generateInvoiceLines()
	{
		try
		{
			createInvoiceLines(hoursRs);
			writeInvoiceLines(con);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void writeInvoiceLines(Connection con) throws SQLException 
	{
		for(InvoiceLine invoiceLine : invoiceLines)
		{
			Statement stmnt = con.createStatement();
			stmnt.execute(
					"INSERT INTO invoice_lines (assessment_id, hour_id, invoice_line_date, invoice_line_amount, invoice_line_amount_calculation, invoice_line_volume, invoice_line_volume_calculation, invoice_line_vat_amount, invoice_line_vat_tariff, invoice_line_vat_amount_calculation)"
					+ "VALUES ("
					+ invoiceLine.getAssessment_id() + ","
					+ invoiceLine.getHour_id() + ","
					+ "'" + invoiceLine.getInvoice_line_date().toString() + "',"
					+ invoiceLine.getInvoice_line_amount() + ","
					+ "'" + invoiceLine.getInvoice_line_amount_calculation() + "',"
					+ invoiceLine.getInvoice_line_volume() + ","
					+ "'" + invoiceLine.getInvoice_line_volume_calculation() + "',"
					+ invoiceLine.getInvoice_line_vat_amount() + ","
					+ "'" + invoiceLine.getInvoice_line_vat_tariff() + "',"
					+ "'" + invoiceLine.getInvoice_line_vat_calculation() + "')");
		}
	}
	
	private ResultSet getHourRecords() throws SQLException
	{

		Statement stmnt = con.createStatement();
		return stmnt.executeQuery("SELECT * FROM hours_with_assessment_data");
	}

	private void createInvoiceLines(ResultSet hoursRs) throws SQLException 
	{
		while(hoursRs.next())
		{
			hourRecords.add(new HourRecord(hoursRs));
		}
		
		for(HourRecord hourRecord : hourRecords)
		{
			try
			{
				double volume = calculateVolume(hourRecord.getHours_minutes(), hourRecord.getAssessment_unit(), hourRecord.getProduct_unit());
				double amount = calculateAmount(volume, hourRecord.getAssessment_unit(), hourRecord.getProduct_unit(), hourRecord.getProduct_price_per_unit());
				double vatAmount = calculateVATAmount(amount, hourRecord.getAssessment_finance_modality());
				String volumeCalculation = generateInvoiceLineVolumeCalculationString(hourRecord.getHours_minutes(), hourRecord.getAssessment_unit(), hourRecord.getProduct_unit());
				String amountCalculation = generateInvoiceLineAmountCalculationString(hourRecord.getHours_minutes(), volumeCalculation, hourRecord.getAssessment_unit(), hourRecord.getProduct_unit(), hourRecord.getProduct_price_per_unit());
				String vatTariff = determineVATTariff(vatAmount, hourRecord.getAssessment_finance_modality());
				String vatAmountCalculation = generateVATCalculationString(volumeCalculation, amountCalculation, hourRecord.getAssessment_finance_modality());
				
				invoiceLines.add(new InvoiceLine(
						hourRecord.getHours_date(),
						hourRecord.getClient_code(),
						hourRecord.getAssessment_id(),
						hourRecord.getHour_id(),
						hourRecord.getProduct_code(),
						amount,
						amountCalculation,
						volume,
						hourRecord.getAssessment_unit(),
						volumeCalculation,
						vatAmount,
						vatTariff,
						vatAmountCalculation						
						));
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private double calculateAmount(double volume, String assessment_unit, String product_unit, double product_price_per_unit) throws Exception 
	{
		if(assessment_unit.equals(product_unit))
			return volume * product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("uur"))
			return (volume / 60) * product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("dagdeel"))
			return (volume / 60 / 4) * product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("etmaal"))
			return (volume / 60 / 24) * product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("minuut"))
			return (volume * 60) * product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("dagdeel"))
			return (volume / 4) * product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("etmaal"))
			return (volume / 24) * product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("minuut"))
			return (volume * 4 * 60) * product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("uur"))
			return (volume * 4) * product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("etmaal"))
			return (volume / 6) * product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("minuut"))
			return (volume * 24 * 60) * product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("uur"))
			return (volume * 24) * product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("dagdeel"))
			return (volume * 6) * product_price_per_unit;
		else
			throw new Exception("No valid unit conversion for amount was possible.");
	}
	
	private String generateInvoiceLineAmountCalculationString(int hours_minutes, String volumeCalculationString, String assessment_unit, String product_unit, double product_price_per_unit) throws Exception
	{
		if(assessment_unit.equals(product_unit))
			return "(" + volumeCalculationString + ")*" + product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("uur"))
			return "((" + volumeCalculationString + ")/60)*" + product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("dagdeel"))
			return "((" + volumeCalculationString + ")/60/4)*" + product_price_per_unit;
		else if(assessment_unit.equals("minuut") && product_unit.equals("etmaal"))
			return "((" + volumeCalculationString + ")/60/24)*" + product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("minuut"))
			return "((" + volumeCalculationString + ")*60)*" + product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("dagdeel"))
			return "((" + volumeCalculationString + ")/4)*" + product_price_per_unit;
		else if(assessment_unit.equals("uur") && product_unit.equals("etmaal"))
			return "((" + volumeCalculationString + ")/24)*" + product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("minuut"))
			return "((" + volumeCalculationString + ")*4*60)*" + product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("uur"))
			return "((" + volumeCalculationString + ")*4)*" + product_price_per_unit;
		else if(assessment_unit.equals("dagdeel") && product_unit.equals("etmaal"))
			return "((" + volumeCalculationString + ")/6)*" + product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("minuut"))
			return "((" + volumeCalculationString + ")*24*60)*" + product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("uur"))
			return "((" + volumeCalculationString + ")*24)*" + product_price_per_unit;
		else if(assessment_unit.equals("etmaal") && product_unit.equals("dagdeel"))
			return "((" + volumeCalculationString + ")*6)*" + product_price_per_unit;
		else
			throw new Exception("No valid unit calculation string for amount calculation was possible.");
	}
	
	private double calculateVolume(int hours_minutes, String assessment_unit, String product_unit) throws Exception 
	{
		if(assessment_unit.equals("minuut"))
			return hours_minutes;
		else if(assessment_unit.equals("uur"))
			return (double)hours_minutes/60;
		else if(assessment_unit.equals("dagdeel"))
			return (double)hours_minutes/60/4;
		else if(assessment_unit.equals("etmaal"))
			return (double)hours_minutes/60/24;
		else
			throw new Exception("No valid unit conversion for volume was possible.");
	}
	
	private String generateInvoiceLineVolumeCalculationString(int hours_minutes, String assessment_unit, String product_unit) throws Exception 
	{
		if(assessment_unit.equals("minuut"))
			return Integer.toString(hours_minutes);
		else if(assessment_unit.equals("uur"))
			return Integer.toString(hours_minutes) + "/60";
		else if(assessment_unit.equals("dagdeel"))
			return Integer.toString(hours_minutes) + "/60/4";
		else if(assessment_unit.equals("etmaal"))
			return Integer.toString(hours_minutes) + "/60/24";
		else
			throw new Exception("No valid unit calculation string for volume calculation was possible.");
	}
	
	private double calculateVATAmount(double amount, String assessment_finance_modality) throws Exception
	{
		if(assessment_finance_modality.equals("ZIN"))
			return amount * 0.06;
		else if(assessment_finance_modality.equals("PGB"))
			return (double)0;
		else if(assessment_finance_modality.equals("Particulier"))
			return amount * 0.21;
		else if(assessment_finance_modality.equals("Passend onderwijs"))
			return (double)0;
		else
			throw new Exception("Could not determine VAT amount.");
	}
	
	private String determineVATTariff(double amount, String assessment_finance_modality) throws Exception
	{
		if(assessment_finance_modality.equals("ZIN"))
			return "6%";
		else if(assessment_finance_modality.equals("PGB"))
			return "0%";
		else if(assessment_finance_modality.equals("Particulier"))
			return "21%";
		else if(assessment_finance_modality.equals("Passend onderwijs"))
			return "Vrijgesteld";
		else
			throw new Exception("Could not determine VAT amount.");
	}
	
	private String generateVATCalculationString(String volumeCalculation, String amountCalculation, String assessment_finance_modality) throws Exception
	{
		if(assessment_finance_modality.equals("ZIN"))
			return "("+ amountCalculation + ")*0.06";
		else if(assessment_finance_modality.equals("PGB"))
			return "("+ amountCalculation + ")*0.00";
		else if(assessment_finance_modality.equals("Particulier"))
			return "("+ amountCalculation + ")*0.21";
		else if(assessment_finance_modality.equals("Passend onderwijs"))
			return "("+ amountCalculation + ")*0.00";
		else
			throw new Exception("Could not determine VAT amount calculation.");
	}
}
