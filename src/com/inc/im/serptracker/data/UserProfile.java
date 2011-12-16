package com.inc.im.serptracker.data;

import java.util.ArrayList;


/**
 * Userprofile holder object
 */


public class UserProfile {

	public int id;
	public String url;
	public ArrayList<Keyword> keywords;

	public UserProfile(String url, ArrayList<Keyword> keywords) {
		this.url = url;
		this.keywords = keywords;
	}

	public UserProfile(int id, String url, ArrayList<Keyword> keywords) {
		this.id = id;
		this.url = url;
		this.keywords = keywords;
	}

}
