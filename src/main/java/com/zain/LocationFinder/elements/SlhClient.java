package com.zain.LocationFinder.elements;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zain.LocationFinder.model.RoutingInfoResult;
import com.zain.LocationFinder.utils.MyUtils;

import dk.i1.diameter.AVP;
import dk.i1.diameter.AVP_Grouped;
import dk.i1.diameter.AVP_OctetString;
import dk.i1.diameter.AVP_UTF8String;
import dk.i1.diameter.AVP_Unsigned32;
import dk.i1.diameter.InvalidAVPLengthException;
import dk.i1.diameter.Message;
import dk.i1.diameter.ProtocolConstants;
import dk.i1.diameter.Utils;
import dk.i1.diameter.node.Capability;
import dk.i1.diameter.node.EmptyHostNameException;
import dk.i1.diameter.node.InvalidSettingException;
import dk.i1.diameter.node.NodeSettings;
import dk.i1.diameter.node.Peer;
import dk.i1.diameter.node.SimpleSyncClient;
import dk.i1.diameter.node.UnsupportedTransportProtocolException;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
public class SlhClient {

	private static final Logger logger = LoggerFactory.getLogger(SlhClient.class);

	private String hostId;
	private String srcRealm;
	private String dstRealm;
	private String destHost;
	private int destPort;

	public SlhClient(String hostId, String srcRealm, String dstRealm, String destHost, int destPort) {
		super();
		this.hostId = hostId;
		this.srcRealm = srcRealm;
		this.dstRealm = dstRealm;
		this.destHost = destHost;
		this.destPort = destPort;
	}

	public SimpleSyncClient startStack()
			throws EmptyHostNameException, IOException, InterruptedException, UnsupportedTransportProtocolException {

		Capability capability = new Capability();
		capability.addVendorAuthApp(10415, 16777291);

		NodeSettings node_settings = null;
		try {
			node_settings = new NodeSettings(hostId, srcRealm, 10415, // vendor-id
					capability, 3868, "ZainJoLocationFinder", 16777216);
		} catch (InvalidSettingException e) {
			logger.info("SlhClient connection can't be established: " + e.toString());
		}

		Peer peers[] = new Peer[] { new Peer(destHost, destPort) };

		SimpleSyncClient ssc = new SimpleSyncClient(node_settings, peers);
		ssc.start();
		logger.info("Waiting for the SlhClient connection to be established!");
		ssc.waitForConnection(); // allow connection to be established.
		logger.info("SlhClient connection established!");

		return ssc;
	}

	public RoutingInfoResult sendDiameterSlh(SimpleSyncClient simpleSyncClient, String msisdn, String gmlcAddress)
			throws Exception {

		Message ccr = new Message();
		ccr.hdr.command_code = 8388622; // LRR
		ccr.hdr.application_id = 16777291; // Slh
		ccr.hdr.setRequest(true);
		ccr.hdr.setProxiable(true);

		RoutingInfoResult slhResult;

		// < Session-Id >
		ccr.add(new AVP_UTF8String(ProtocolConstants.DI_SESSION_ID, simpleSyncClient.node().makeNewSessionId()));

		// { Auth-Session-State }
		ccr.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_SESSION_STATE, 1)); // a lie but a minor one

		// { Origin-Host, Origin-Realm }
		simpleSyncClient.node().addOurHostAndRealm(ccr);

		// { Destination-Host }
		ccr.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_HOST, destHost));

		// { Destination-Realm }
		ccr.add(new AVP_UTF8String(ProtocolConstants.DI_DESTINATION_REALM, dstRealm));

		// MSISDN
		ccr.add(new AVP_OctetString(701, 10415, MyUtils.toHex(msisdn)).setM());

		///////////////////
		// { Auth-Application-Id }
		ccr.add(new AVP_Unsigned32(ProtocolConstants.DI_AUTH_APPLICATION_ID, 16777291)); // a lie but a minor one

		// GMLC
		ccr.add(new AVP_OctetString(1474, 10415, MyUtils.toHex(gmlcAddress)).setM());

		Utils.setMandatory_RFC3588(ccr);
		Utils.setMandatory_RFC4006(ccr);

		// Send it
		Message cca = simpleSyncClient.sendRequest(ccr);
		logger.info("Diameter request sent for: " + msisdn);

		// Now look at the result
		if (cca == null) {
			logger.error("No response");
			slhResult = new RoutingInfoResult(100, "NA", "NA");
			return slhResult;
		}
		AVP resultCodeAVP = cca.find(ProtocolConstants.DI_RESULT_CODE);
		AVP imsiAVP = cca.find(ProtocolConstants.DI_USER_NAME);
		AVP servingNodeAVP = cca.find(2401, 10415);

		if (resultCodeAVP == null) {
			logger.error("No result code");
			slhResult = new RoutingInfoResult(100, "NA", "NA");
			return slhResult;
		}
		try {
			AVP_Unsigned32 result_code_u32 = new AVP_Unsigned32(resultCodeAVP);
			AVP_UTF8String imsi_UTF8String = new AVP_UTF8String(imsiAVP);
			int rc = result_code_u32.queryValue();
			String imsi = imsi_UTF8String.queryValue();

			AVP_Grouped MMEName = new AVP_Grouped(servingNodeAVP);
			AVP[] mmeGrouped = MMEName.queryAVPs();
			String mme = new AVP_UTF8String(mmeGrouped[0]).queryValue();

			if (rc >= 2000 && rc < 2999) {
				slhResult = new RoutingInfoResult(rc, imsi, mme);
				logger.info("Success: " + slhResult.getResultCode());
				logger.info("IMSI: " + slhResult.getImsi());
				logger.info("MME: " + slhResult.getMme());

			} else {
				slhResult = new RoutingInfoResult(100, "NA", "NA");
			}

		} catch (InvalidAVPLengthException ex) {
			logger.error("result-code was malformed");
			slhResult = new RoutingInfoResult(100, "NA", "NA");
			return slhResult;
		}
		return slhResult;
	}
}
