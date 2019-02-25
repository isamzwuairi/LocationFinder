package com.zain.LocationFinder.model;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class Sites {

	private String id;

	private String name;
	private float lng;
	private float lat;
	private String governorate;
	private String district;
	private String subdistrict;

	public Sites() {
		super();
	}

	public Sites(String id, String name, float lng, float lat, String governorate, String district,
			String subdistrict) {
		super();
		this.id = id;
		this.name = name;
		this.lng = lng;
		this.lat = lat;
		this.governorate = governorate;
		this.district = district;
		this.subdistrict = subdistrict;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public String getGovernorate() {
		return governorate;
	}

	public void setGovernorate(String governorate) {
		this.governorate = governorate;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getSubdistrict() {
		return subdistrict;
	}

	public void setSubdistrict(String subdistrict) {
		this.subdistrict = subdistrict;
	}

}
