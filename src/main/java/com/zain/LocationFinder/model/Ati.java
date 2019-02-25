package com.zain.LocationFinder.model;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class Ati {
	private String mcc;
	private String mnc;
	private String lac;
	private String cellid;
	private String aol;
	private String vlrNumber;

	public Ati(String mcc, String mnc, String lac, String cellid, String aol, String vlrNumber) {
		super();
		this.mcc = mcc;
		this.mnc = mnc;
		this.lac = lac;
		this.cellid = cellid;
		this.aol = aol;
		this.vlrNumber = vlrNumber;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getMnc() {
		return mnc;
	}

	public void setMnc(String mnc) {
		this.mnc = mnc;
	}

	public String getLac() {
		return lac;
	}

	public void setLac(String lac) {
		this.lac = lac;
	}

	public String getCellid() {
		return cellid;
	}

	public void setCellid(String cellid) {
		this.cellid = cellid;
	}

	public String getAol() {
		return aol;
	}

	public void setAol(String aol) {
		this.aol = aol;
	}

	public String getVlrNumber() {
		return vlrNumber;
	}

	public void setVlrNumber(String vlrNumber) {
		this.vlrNumber = vlrNumber;
	}

	@Override
	public String toString() {
		return "Ati [mcc=" + mcc + ", mnc=" + mnc + ", lac=" + lac + ", cellid=" + cellid + ", aol=" + aol
				+ ", vlrNumber=" + vlrNumber + "]";
	}

}
