package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;

public class ProdebaInvoices 
{
	private static String jdbcUrl = "jdbc:mysql://192.168.100.145:3306/prodeba";
	Connection con;
	
	public ProdebaInvoices()
	{
		try 
		{
			con = DriverManager.getConnection(jdbcUrl, "richard", "L3ias*l*22");
			System.out.println("Connection to database established.");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void generateConceptInvoiceLines()
	{
		try 
		{
			ResultSet hoursRs = getHourRecords(con);
			
			InvoiceLineGenerator ilg = new InvoiceLineGenerator(con, hoursRs);
			ilg.generateInvoiceLines();
			System.out.println("All operations finished. Program closing.");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void generateInvoiceLineGroups(PeriodType periodType, String invoice_invoice_range)
	{
		try
		{
			InvoiceLineGroupGenerator ilgg = new InvoiceLineGroupGenerator(con);
			ilgg.generateInvoiceLineGroups(periodType, invoice_invoice_range);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void generateInvoices(PeriodType periodType, String invoice_invoice_range)
	{
		try
		{
			InvoiceGenerator ig = new InvoiceGenerator(con);
			ig.generateInvoices(periodType, invoice_invoice_range);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void closeConnection()
	{
		try
		{
			con.close();
			System.out.println("Connection to database closed.");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private ResultSet getHourRecords(Connection con) throws SQLException
	{
		Statement stmnt = con.createStatement();
		return stmnt.executeQuery("SELECT * FROM hours_with_assessment_data");
	}

	

	public static void main(String[] args) 
	{
		ProdebaInvoices pi = new ProdebaInvoices();
		pi.generateConceptInvoiceLines();
		pi.generateInvoiceLineGroups(PeriodType.MONTH, "Augustus");
		pi.generateInvoiceLineGroups(PeriodType.MONTH, "September");
		pi.generateInvoiceLineGroups(PeriodType.PERIOD, "Periode 8");
		pi.generateInvoiceLineGroups(PeriodType.PERIOD, "Periode 9");
		pi.generateInvoiceLineGroups(PeriodType.PERIOD, "Periode 10");
		
		pi.generateInvoices(PeriodType.MONTH, "Augustus");
		pi.generateInvoices(PeriodType.MONTH, "September");
		pi.generateInvoices(PeriodType.PERIOD, "Periode 8");
		pi.generateInvoices(PeriodType.PERIOD, "Periode 9");
		pi.generateInvoices(PeriodType.PERIOD, "Periode 10");
		pi.closeConnection();
	}

}
