package nl.devoteam.prodeba.prodeba_invoices.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HourRecord 
{
	private int hour_id;
	private int assessment_id;
	private String assessment_unit;
	private String assessment_finance_modality;
	private String product_code;
	private String product_unit;
	private double product_price_per_unit;
	private String client_code;
	private String client_name;
	private Date hours_date;
	private int hours_minutes;
	
	public HourRecord(ResultSet hoursRs)
	{
		try 
		{
			hour_id = hoursRs.getInt(1);
			assessment_id = hoursRs.getInt(2);
			assessment_unit = hoursRs.getString(3);
			assessment_finance_modality = hoursRs.getString(4);
			product_code = hoursRs.getString(5);
			product_unit = hoursRs.getString(6);
			product_price_per_unit = hoursRs.getDouble(7);
			client_code = hoursRs.getString(8);
			client_name = hoursRs.getString(9);
			hours_date = hoursRs.getDate(10);
			hours_minutes = hoursRs.getInt(11);
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public int getHour_id() {
		return hour_id;
	}

	public int getAssessment_id() {
		return assessment_id;
	}

	public String getAssessment_unit() {
		return assessment_unit;
	}
	
	public String getAssessment_finance_modality() {
		return assessment_finance_modality;
	}
	
	public String getProduct_code() {
		return product_code;
	}

	public String getProduct_unit() {
		return product_unit;
	}

	public double getProduct_price_per_unit() {
		return product_price_per_unit;
	}

	public String getClient_code() {
		return client_code;
	}

	public String getClient_name() {
		return client_name;
	}

	public Date getHours_date() {
		return hours_date;
	}

	public int getHours_minutes() {
		return hours_minutes;
	}
}
