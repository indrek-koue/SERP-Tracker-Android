package com.inc.im.serptracker.data;

public class Keyword {

	public int id;
	public int rank;
	public int lastRank;
	public String value;
	public String htmlSourceCode;

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
