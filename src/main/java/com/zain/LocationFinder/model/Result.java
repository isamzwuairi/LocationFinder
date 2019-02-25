package com.zain.LocationFinder.model;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class Result {

	private final Ecgi ECGI;
	private final String servingNode;
	private final String CellName;
	private final String Governorate;
	private final String District;
	private final String Subdistrict;
	private final float Long;
	private final float lat;
	private final String rat;

	public Result(Ecgi eCGI, String servingnode, String cellName, String governorate, String district,
			String subdistrict, float l, float lat, String rat) {
		super();
		ECGI = eCGI;
		servingNode = servingnode;
		CellName = cellName;
		Governorate = governorate;
		District = district;
		Subdistrict = subdistrict;
		Long = l;
		this.lat = lat;
		this.rat = rat;
	}

	public Ecgi getECGI() {
		return ECGI;
	}

	public String getServingNode() {
		return servingNode;
	}

	public String getCellName() {
		return CellName;
	}

	public String getGovernorate() {
		return Governorate;
	}

	public String getDistrict() {
		return District;
	}

	public String getSubdistrict() {
		return Subdistrict;
	}

	public float getLong() {
		return Long;
	}

	public float getLat() {
		return lat;
	}

	public String getRat() {
		return rat;
	}

	@Override
	public String toString() {
		return "Result [ECGI=" + ECGI + ", ServingNode=" + servingNode + ", CellName=" + CellName + ", Governorate="
				+ Governorate + ", District=" + District + ", Subdistrict=" + Subdistrict + ", Long=" + Long
				+ ", lat=" + lat + ", rat=" + rat + "]";
	}
	
	public String writeCDR() {
		return ECGI.getCellID() + "|" + ECGI.getMCC() + "|" + ECGI.getMNC() + "|" + servingNode + "|" + CellName + "|"
				+ Governorate + "|" + District + "|" + Subdistrict + "|" + Long
				+ "|" + lat + "|" + rat;
	}

}
