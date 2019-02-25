package com.zain.LocationFinder.model;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class RoutingInfoResult {

	private int resultCode;
	private String imsi;
	private String mme;

	public RoutingInfoResult(int resultCode, String imsi, String mme) {
		super();
		this.resultCode = resultCode;
		this.imsi = imsi;
		this.mme = mme;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getMme() {
		return mme;
	}

	public void setMme(String mme) {
		this.mme = mme;
	}

	@Override
	public String toString() {
		return "RoutingInfoResult [resultCode=" + resultCode + ", imsi=" + imsi + ", mme=" + mme + "]";
	}

}
