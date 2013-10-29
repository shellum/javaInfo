package com.finalhack.javainfo;

import java.io.Serializable;

public class Question implements Serializable {
	
	public String subject;
	public String question;
	public String answer;
	public String index;
	
	public Question(String question, String answer, String subject, String index)
	{
		this.question = question;
		this.answer = answer;
		this.subject = subject;
		this.index = index;
	}

	
}
