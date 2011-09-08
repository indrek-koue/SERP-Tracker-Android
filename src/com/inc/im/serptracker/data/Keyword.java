package com.inc.im.serptracker.data;

import java.util.ArrayList;
import java.util.List;

public class Keyword {

	public int id;
	public int rank;
	public String value;
	public List<String> searchEngineResults = new ArrayList<String>();

	public Keyword(String value) {
		this.value = value;
	}

	public Keyword(String value, int rank) {
		this.value = value;
		this.rank = rank;
	}

	public Keyword(int id, String value, int rank) {
		this.id = id;
		this.value = value;
		this.rank = rank;
	}

}
