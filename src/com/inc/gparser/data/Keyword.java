package com.inc.gparser.data;

public class Keyword {

	public int id;
	public int rank;
	public String value;

	public Keyword(String value) {
		this.value = value;
	}

	public Keyword(String value, int rank) {
		this.value = value;
		this.rank = rank;
	}

	public Keyword(int id, String value, int rank) {
		this.id = id;
		this.rank = rank;
		this.value = value;
	}

}
