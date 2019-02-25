package com.zain.LocationFinder.elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.MetaData;
import org.jdiameter.api.Network;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.Session;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.Stack;
import org.jdiameter.api.StackType;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zain.LocationFinder.messages.ProvideLocationReq;
import com.zain.LocationFinder.utils.MyUtils;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class SlgClient {

	private static final Logger logger = LoggerFactory.getLogger(SlgClient.class);

	private String configFile;

	public SlgClient(String configFile) {
		this.configFile = configFile;
	}

	private static final int commandCode = 8388620;
	private static final long vendorID = 10415;
	private static final long applicationID = 16777255;
	private final ApplicationId authAppId = ApplicationId.createByAuthAppId(applicationID);
	private final AvpDictionary dictionary = AvpDictionary.INSTANCE;
	private Stack stack;
	private SessionFactory factory;
	private Session session; // session used as handle for communication
	private static final String dictionaryFile = "dictionary.xml";

	public void initStack() {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing Stack...");
		}
		InputStream is = null;
		try {
			// Parse dictionary, it is used for user friendly info.
			dictionary.parseDictionary(this.getClass().getClassLoader().getResourceAsStream(dictionaryFile));
			logger.info("AVP Dictionary successfully parsed.");

			this.stack = new StackImpl();
			// Parse stack configuration
			is = this.getClass().getClassLoader().getResourceAsStream(configFile);
			Configuration config = new XMLConfiguration(is);
			factory = stack.init(config);
			if (logger.isInfoEnabled()) {
				logger.info("Slg Daimeter Stack Configuration successfully loaded.");
			}
			// Print info about application
			Set<org.jdiameter.api.ApplicationId> appIds = stack.getMetaData().getLocalPeer().getCommonApplications();

			logger.info("Diameter Stack  :: Supporting " + appIds.size() + " applications.");
			for (org.jdiameter.api.ApplicationId x : appIds) {
				logger.info("Diameter Stack  :: Common :: " + x);
			}
			is.close();
			// Register network req listener, even though we wont receive requests
			// this has to be done to inform stack that we support application
			Network network = stack.unwrap(Network.class);
			network.addNetworkReqListener(new NetworkReqListener() {

				@Override
				public Answer processRequest(Request request) {
					// this wontbe called.

					return null;
				}
			}, this.authAppId); // passing our example app id.

		} catch (Exception e) {
			logger.info("SlgClient connection can't be established: " + e.toString());
			if (this.stack != null) {
				this.stack.destroy();
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.info("SlgClient connection can't be established: " + e1.toString());
				}
			}
			return;
		}

		MetaData metaData = stack.getMetaData();
		// ignore for now.
		if (metaData.getStackType() != StackType.TYPE_SERVER || metaData.getMinorVersion() <= 0) {
			stack.destroy();
			logger.error("Incorrect driver");
			return;
		}

		try {
			if (logger.isInfoEnabled()) {
				logger.info("Starting stack");
			}
			stack.start();
			if (logger.isInfoEnabled()) {
				logger.info("Stack is running.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			stack.destroy();
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Stack initialization successfully completed.");
		}
	}

	@SuppressWarnings("static-access")
	public void start() throws Exception {
		try {
			logger.info("Wait for connection to peer");
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// do send
			this.session = this.factory.getNewSession("Zain.jordan;" + System.currentTimeMillis());
		} catch (InternalException e) {
		}
	}

	public Request sendNextRequest(String IMSI, String MSISDN, String realmName, String MME) throws InternalException,
			IllegalDiameterStateException, RouteException, OverloadException, IOException, AvpDataException {
		Request r = this.session.createRequest(commandCode, this.authAppId, realmName, MME);
		r.setProxiable(true);

		// here we have all except our custom avps
		ProvideLocationReq clientSLgPLR = new ProvideLocationReq();

		AvpSet reqSet = r.getAvps();
		// code , value , vendor, mandatory,protected,isUnsigned32

		if (reqSet.getAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID) == null) {
			AvpSet vendorSpecificApplicationId = reqSet.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, true,
					false);
			// 1* [ Vendor-Id ]
			vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, vendorID, true);
			// 0*1{ Auth-Application-Id }
			vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, applicationID, true);
		}

		// { Auth-Session-State }
		if (reqSet.getAvp(Avp.AUTH_SESSION_STATE) == null) {
			reqSet.addAvp(Avp.AUTH_SESSION_STATE, 1, true, false);
		}

		// { Origin-Host }
		// { Origin-Realm }
		// { Destination-Host }
		// reqSet.addAvp(Avp.DESTINATION_HOST, serverHostname, true);
		// { Destination-Realm }
		// reqSet.addAvp(Avp.DESTINATION_REALM, realmName, true, false, false);
		// { SLg-Location-Type }
		int slgLocationType = 1;
		reqSet.addAvp(Avp.SLG_LOCATION_TYPE, slgLocationType, 10415, true, false);

		// [ User-Name ] IE: IMSI
		String userName = IMSI;
		reqSet.addAvp(Avp.USER_NAME, userName, true, false, false); // removed vendor specific

		// [ MSISDN ]
		byte[] msisdn = MyUtils.toHex(MSISDN);
		reqSet.addAvp(Avp.MSISDN, msisdn, 10415, true, false);

		// [ IMEI ]
		// String imei = clientSLgPLR.getIMEI();
		// if (imei != null) {
		// reqSet.addAvp(Avp.TGPP_IMEI, imei, 10415, true, false, false);
		// }
		// { LCS-EPS-Client-Name }
		AvpSet lcsEPSClientName = reqSet.addGroupedAvp(Avp.LCS_EPS_CLIENT_NAME, 10415, true, false);
		String lcsNameString = clientSLgPLR.getLCSNameString();
		int lcsFormatIndicator = clientSLgPLR.getLCSFormatIndicator(); // changed from 2 to zero

		if (lcsNameString != null) {
			lcsEPSClientName.addAvp(Avp.LCS_NAME_STRING, lcsNameString, 10415, true, false, true);
		}
		if (lcsFormatIndicator != -1) {
			lcsEPSClientName.addAvp(Avp.LCS_FORMAT_INDICATOR, lcsFormatIndicator, 10415, true, false);
		}

		// { LCS-Client-Type }
		int lcsClientType = clientSLgPLR.getLCSClientType();
		if (lcsClientType != -1) {
			reqSet.addAvp(Avp.LCS_CLIENT_TYPE, lcsClientType, 10415, true, false); // changed to true
		}

		// [ LCS-Requestor-Name ]
		// AvpSet lcsRequestorName = reqSet.addGroupedAvp(Avp.LCS_REQUESTOR_NAME, 10415,
		// false, false);
		// String lcsRequestorIdString = clientSLgPLR.getLCSRequestorIdString();
		// int reqLCSFormatIndicator = clientSLgPLR.getReqLCSFormatIndicator();
		// if (lcsRequestorIdString != null) {
		// lcsRequestorName.addAvp(Avp.LCS_REQUESTOR_ID_STRING, lcsRequestorIdString,
		// 10415, false, false, false);
		// }
		// if (reqLCSFormatIndicator != -1) {
		// lcsRequestorName.addAvp(Avp.LCS_FORMAT_INDICATOR, reqLCSFormatIndicator,
		// 10415, false, false);
		// }
		// [ LCS-Priority ]
		long lcsPriority = clientSLgPLR.getLCSPriority();
		if (lcsPriority != -1) {
			reqSet.addAvp(Avp.LCS_PRIORITY, lcsPriority, 10415, true, false, true);// changed
		}

		// [ LCS-QoS ]
		AvpSet lcsQoS = reqSet.addGroupedAvp(Avp.LCS_QOS, 10415, true, false);// changed
		int lcsQoSClass = clientSLgPLR.getLCSQoSClass();
		long horizontalAccuracy = clientSLgPLR.getHorizontalAccuracy();
//		long verticalAccuracy = clientSLgPLR.getVerticalAccuracy();
//		int verticalRequested = clientSLgPLR.getVerticalRequested();
		int responseTime = clientSLgPLR.getResponseTime();

		if (lcsQoSClass != -1) {
			lcsQoS.addAvp(Avp.LCS_QOS_CLASS, lcsQoSClass, 10415, true, false);
		}
		if (horizontalAccuracy != -1) {
			lcsQoS.addAvp(Avp.HORIZONTAL_ACCURACY, horizontalAccuracy, 10415, true, false, true);
		}
//		 if (verticalAccuracy != -1) {
//		 lcsQoS.addAvp(Avp.VERTICAL_ACCURACY, verticalAccuracy, 10415, false, false,
//		 true);
//		 }
//		 if (verticalRequested != -1) {
//		 lcsQoS.addAvp(Avp.VERTICAL_REQUESTED, verticalRequested, 10415, false,
//		 false);
//		 }
		if (responseTime != -1) {
			lcsQoS.addAvp(Avp.RESPONSE_TIME, responseTime, 10415, true, false);
		}

		// [ Velocity-Requested ]
//		int velocityRequested = clientSLgPLR.getVelocityRequested();
//		if (velocityRequested != -1) {
//			reqSet.addAvp(Avp.VELOCITY_REQUESTED, velocityRequested, 10415, true, false);
//		}

		// [ LCS-Supported-GAD-Shapes ]
//		long supportedGADShapes = clientSLgPLR.getLCSSupportedGADShapes();
//		if (supportedGADShapes != -1) {
//			reqSet.addAvp(Avp.LCS_SUPPORTED_GAD_SHAPES, supportedGADShapes, 10415, true, false, true);
//		}

		
		// [ GMLC-Address ]
		java.net.InetAddress gmlcAddress = clientSLgPLR.getGMLCAddress();
		if (gmlcAddress != null) {
			reqSet.addAvp(Avp.GMLC_ADDRESS, gmlcAddress, 10415, false, false);
		}

		return r;

	}

	public Session getSession() {
		return session;
	}

}
