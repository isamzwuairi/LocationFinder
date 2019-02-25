package com.zain.LocationFinder.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zain.LocationFinder.model.Ati;
import com.zain.LocationFinder.model.Ecgi;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class MyUtils {

	private static final Logger logger = LoggerFactory.getLogger(MyUtils.class);

	public static byte[] toHex(String msisdn) {

		int x1 = Integer.parseInt(new StringBuilder(msisdn.substring(0, 2)).reverse().toString(), 16);
		int x2 = Integer.parseInt(new StringBuilder(msisdn.substring(2, 4)).reverse().toString(), 16);
		int x3 = Integer.parseInt(new StringBuilder(msisdn.substring(4, 6)).reverse().toString(), 16);
		int x4 = Integer.parseInt(new StringBuilder(msisdn.substring(6, 8)).reverse().toString(), 16);
		int x5 = Integer.parseInt(new StringBuilder(msisdn.substring(8, 10)).reverse().toString(), 16);
		int x6 = Integer.parseInt(new StringBuilder(msisdn.substring(10, 12)).reverse().toString(), 16);

		byte[] result = new byte[] { (byte) x1, (byte) x2, (byte) x3, (byte) x4, (byte) x5, (byte) x6 };

		return result;
	}

	public static String numberNormalization(String msisdn) {
		if (msisdn.length() < 9 || msisdn.length() > 12 || msisdn.length() == 11) {
			return null;
		} else if (!(msisdn.startsWith("79") || msisdn.startsWith("079") || msisdn.startsWith("96279")
				|| msisdn.startsWith("70") || msisdn.startsWith("070") || msisdn.startsWith("96270"))) { // Zain & TDD (Mada & Zain)
			return null;
		} else if (msisdn.length() == 9) {
			msisdn = "962" + msisdn;
		} else if (msisdn.length() == 10) {
			msisdn = "962" + msisdn.substring(msisdn.length() - 9);
		}
		return msisdn;

	}

	public static Ecgi getEcgi(String ecgiString) {
		String MccMncRaw = new StringBuilder(ecgiString.substring(0, 2)).reverse().toString()
				+ new StringBuilder(ecgiString.substring(2, 4)).reverse().toString()
				+ new StringBuilder(ecgiString.substring(4, 6)).reverse().toString();
		String Eci = ecgiString.substring(ecgiString.length() - 7);
		String enodeB = Integer.toString(Integer.parseInt(Eci.substring(0, 5), 16));
		String cellId = Integer.toString(Integer.parseInt(Eci.substring(Eci.length() - 2), 16));

		return new Ecgi(MccMncRaw.substring(0, 3), MccMncRaw.substring(MccMncRaw.length() - 2), enodeB + cellId);
	}

	public static String buildEcgiString(String mmeAddress) {
		String mcc = "000";
		String mnc = "00";
		String[] stringArray = mmeAddress.split("\\.");
		for (int i = 0; i < stringArray.length; i++) {
			String stringField = stringArray[i];
			if (stringField.toLowerCase().contains("mcc")) {
				mcc = stringField.substring(stringField.length() - 3);
			} else if (stringField.toLowerCase().contains("mnc")) {
				mnc = stringField.substring(stringField.length() - 2);
			}
		}
		String MccMncRaw = mcc + "F" + mnc;
		String resultEcgiString = new StringBuilder(MccMncRaw.substring(0, 2)).reverse().toString()
				+ new StringBuilder(MccMncRaw.substring(2, 4)).reverse().toString()
				+ new StringBuilder(MccMncRaw.substring(4, 6)).reverse().toString() + "00000000";

		return resultEcgiString;
	}

	public static Ati getAti(String atiString) {
		String mcc = "-1";
		String mnc = "-1";
		String lac = "-1";
		String cellid = "-1";
		String aol = "-1";
		String vlrNumber = "-1";

		if (atiString.contains("vlrNumber")) { // Check if the ATI response is coming as expected
			String[] stringArray = atiString.split(",");
			for (int i = 0; i < stringArray.length; i++) {
				String stringField = stringArray[i];
				String key = stringField.substring(0, stringField.indexOf("=")).toLowerCase();
				String value = stringField.substring(stringField.indexOf("=") + 1);
				logger.debug("Splitting ATI response: " + atiString + ". into fields: key" + i + " = " + key + " value"
						+ i + " = " + value);
				switch (key) {
				case "mcc":
					mcc = value;
					break;
				case "mnc":
					if (Integer.parseInt(value) < 10) {
						value = "0" + value;
					}
					mnc = value;
					break;
				case "lac":
					lac = value;
					break;
				case "cellid":
					cellid = value;
					break;
				case "aol":
					aol = value;
					break;
				case "vlrnumber":
					vlrNumber = value;
				}
			}
		}
		return new Ati(mcc, mnc, lac, cellid, aol, vlrNumber);
	}
}
