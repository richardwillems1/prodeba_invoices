package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;

public class ProdebaInvoices 
{
	private static String jdbcUrl = "jdbc:mysql://192.168.0.20:3306/prodeba";
	Connection con;
	
	public ProdebaInvoices()
	{
		try 
		{
			con = DriverManager.getConnection(jdbcUrl, "richard", "L3ias*l*22");
			System.out.println("Connection established to database.");
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
	
	public void generateInvoiceLineGroups()
	{
		try
		{
			InvoiceLineGroupGenerator ilgg = new InvoiceLineGroupGenerator(con);
			ilgg.generateInvoice(PeriodType.MONTH, "Augustus", con);
		}
		catch(Exception e)
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
		pi.generateInvoiceLineGroups();
	}

}
