package com.example.word_test_80;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Words {
	Date date;
	int status;
	String eng;
	String chi;
	String association;
	int line;
	int which_file;

	
	
	public Words(Date date, int status, String eng, String chi, String association, int line, int which_file) {
		super();
		this.date = date;
		this.status = status;
		this.eng = eng;
		this.chi = chi;
		this.association = association;
		this.line = line;
        this.which_file = which_file;
	}


	public String getDateTime(){
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		String strDate = sdFormat.format(date);
		//System.out.println(strDate);
		return strDate;
	}

}
