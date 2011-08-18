package com.inc.im.serptracker.util;

import java.util.ArrayList;

//TODO: remove dublicate

public class GoogleParser {

	final String specialCase1 = "webcache.googleusercontent.com";
	final String specialCase2 = "<a href=\"/search";
	final String specialCase3 = "translate.google.";
	final String specialCase4 = "<a href=\"/url?q=";
	final String specialCase5 = "<a href=\"http://docs.google.com/viewer?";
	final String specialCase6 = "<a href=\"/aclk?sa=";

	final String startTag = "<a href=";
	final String endTag = "</a>";

	public ArrayList<String> parse(String sourceText) {

		ArrayList<String> allLinks = new ArrayList<String>();

		int i = sourceText.indexOf("<h3");
		// int j = sourceText.indexOf("</head>");
		// int k = sourceText.indexOf("</head>");

		// remove beginning and end
		String sourceText2 = sourceText.substring(i,
				sourceText.lastIndexOf("</h3>"));

		String previousLink = null;
		Boolean isDublicate = false;

		while (sourceText2.contains(startTag)) {

			int startIndex = sourceText2.indexOf(startTag);
			int endIndex = sourceText2.indexOf(endTag) + 4;

			if (startIndex < endIndex) {
				String link = sourceText2.substring(startIndex, endIndex);

				final boolean b = link.contains(specialCase1);
				final boolean c = link.contains(specialCase2);
				final boolean d = link.contains(specialCase3);
				final boolean e = link.contains(specialCase4);
				final boolean f = link.contains(specialCase5);
				final boolean g = link.contains(specialCase6);

				if (!b && !c && !d && !e && !f && !g) {

					// compare if is dublicate
					if (previousLink != null) {
						String one = previousLink.replace("http://", "");

						String four = one.substring(0, one.indexOf("/"));

						String two = link.replace("http://", "");

						String three = two.substring(0, two.indexOf("/"));

						if (four.equals(three)) {
							// Achtung achtung dublicate
							isDublicate = true;
						}

					}

					if (!isDublicate)
						allLinks.add(link);

					previousLink = link;

				}

				isDublicate = false;

			}

			sourceText2 = sourceText2.substring(endIndex);
		}

		return allLinks;
	}

}
