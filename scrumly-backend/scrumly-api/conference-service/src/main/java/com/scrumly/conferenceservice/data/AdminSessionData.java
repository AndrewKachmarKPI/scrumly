package com.scrumly.conferenceservice.data;

public class AdminSessionData {
	
	long expires;
	
	public AdminSessionData(long expires) {
		this.expires = expires;
	} 

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}



	
}
