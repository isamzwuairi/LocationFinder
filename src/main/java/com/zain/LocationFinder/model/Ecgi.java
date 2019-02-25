package com.zain.LocationFinder.model;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class Ecgi {

	private final String MCC;
	private final String MNC;
	private final String CellID;

	public Ecgi(String mCC, String mNC, String cellID) {
		super();
		MCC = mCC;
		MNC = mNC;
		CellID = cellID;
	}

	public String getMCC() {
		return MCC;
	}

	public String getMNC() {
		return MNC;
	}

	public String getCellID() {
		return CellID;
	}

	@Override
	public String toString() {
		return "Ecgi [MCC=" + MCC + ", MNC=" + MNC + ", CellID=" + CellID + "]";
	}

}
