package com.inc.im.serptracker.data;

/**
 * Keyword holder object
 */

public class Keyword {

	public int id;
	public String keyword;

	public int oldRank;
	public int newRank;

	public String anchorText;
	public String url;

	public Keyword(String value) {
		this.keyword = value;
	}

	public Keyword(String value, int rank) {
		this.keyword = value;
		this.oldRank = rank;
	}

	public Keyword(int id, String value, int rank) {
		this.id = id;
		this.keyword = value;
		this.oldRank = rank;
	}

	public Keyword(int id, String value, int rank, String anchor, String url) {
		this.id = id;
		this.keyword = value;
		this.oldRank = rank;
		this.anchorText = anchor;
		this.url = url;
	}

	@Override
	public String toString() {
		return id + ":" + keyword + ":old=" + oldRank + ":new=" + newRank
				+ ":anchorText=" + anchorText + ":url" + url;
	}

}
