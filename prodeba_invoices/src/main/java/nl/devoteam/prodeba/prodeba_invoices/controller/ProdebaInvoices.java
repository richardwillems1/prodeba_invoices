package nl.devoteam.prodeba.prodeba_invoices.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import nl.devoteam.prodeba.prodeba_invoices.enumeration.PeriodType;

public class ProdebaInvoices 
{
	private static String jdbcUrl = "jdbc:mysql://192.168.56.101:3306/prodeba";
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
	
	public void resetDatabase(int numberOfHourRegistrations)
	{
		try
		{
			System.out.println("Now starting database reset and entering " + numberOfHourRegistrations + " random hour records.");
			Statement stmnt = con.createStatement();
			
			stmnt.addBatch("SET FOREIGN_KEY_CHECKS = 0;");
			stmnt.addBatch("TRUNCATE table hours;");
			stmnt.addBatch("TRUNCATE table il_to_ilg;");
			stmnt.addBatch("TRUNCATE table ilg_to_i;");
			stmnt.addBatch("TRUNCATE table invoice_line_groups;");
			stmnt.addBatch("TRUNCATE table invoice_lines;");
			stmnt.addBatch("TRUNCATE table invoices;");
			stmnt.addBatch("CALL create_random_hour_registrations(" + numberOfHourRegistrations + ")");
			
			stmnt.executeBatch();
			
			System.out.println("Database reset and random hour registratration generation finished.");
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
			System.out.println("Now calculating invoice lines based on hour registrations.");
			
			InvoiceLineGenerator ilg = new InvoiceLineGenerator(con);
			ilg.generateInvoiceLines();
			System.out.println("All invoice lines calculated and written into database.");
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
			System.out.println("Now calculating invoice line groups based on invoice lines stored in database for " + invoice_invoice_range + ".");
			InvoiceLineGroupGenerator ilgg = new InvoiceLineGroupGenerator(con);
			ilgg.generateInvoiceLineGroups(periodType, invoice_invoice_range);
			System.out.println("All invoice line groups calculated and written into database.");
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
			System.out.println("Now calculating invoices based on invoice line groups stored in database for " + invoice_invoice_range + ".");
			InvoiceGenerator ig = new InvoiceGenerator(con);
			ig.generateInvoices(periodType, invoice_invoice_range);
			System.out.println("All invoices calculated and written into database.");
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
	
	public static void main(String[] args) 
	{
		ProdebaInvoices pi = new ProdebaInvoices();
		
		pi.resetDatabase(300);
		
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
