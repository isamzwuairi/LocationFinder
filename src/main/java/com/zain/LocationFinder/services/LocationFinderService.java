package com.zain.LocationFinder.services;

import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.jdiameter.api.Message;
import org.jdiameter.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.zain.LocationFinder.controller.WebServiceController;
import com.zain.LocationFinder.elements.SlgClient;
import com.zain.LocationFinder.elements.SlhClient;
import com.zain.LocationFinder.model.RoutingInfoResult;

import dk.i1.diameter.node.SimpleSyncClient;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
@Service
public class LocationFinderService {

	private static final Logger logger = LoggerFactory.getLogger(WebServiceController.class);

	@Autowired
	private Environment env;

	private SlhClient slhClient;
	private SimpleSyncClient hqSCC;
	private SlgClient hqSlgClient;
	private SlgClient aaSlgClient;
	private SlgClient hqVepcSlgClient;
	private SlgClient aaVepcSlgClient;

	@PostConstruct
	public void initializer() throws Exception {
		slhClient = new SlhClient(env.getProperty("HSS.host.id"), env.getProperty("HSS.src.realm"),
				env.getProperty("HSS.dst.realm"), env.getProperty("HSS.destination.host"),
				Integer.parseInt(env.getProperty("HSS.destination.port")));
		hqSCC = slhClient.startStack();
		hqSlgClient = new SlgClient(env.getProperty("HQ.MME.configFile"));
		hqSlgClient.initStack();
		hqSlgClient.start();
		aaSlgClient = new SlgClient(env.getProperty("AA.MME.configFile"));
		aaSlgClient.initStack();
		aaSlgClient.start();
		hqVepcSlgClient = new SlgClient(env.getProperty("HQ.VEPC.MME.configFile"));
		hqVepcSlgClient.initStack();
		hqVepcSlgClient.start();
		aaVepcSlgClient = new SlgClient(env.getProperty("AA.VEPC.MME.configFile"));
		aaVepcSlgClient.initStack();
		aaVepcSlgClient.start();
	}

	public RoutingInfoResult sendSlhRequest(String msisdn, String gmlc) throws Exception {
		return slhClient.sendDiameterSlh(hqSCC, msisdn, gmlc);
	}

	@Async
	public Future<Message> sendSlgRequests(RoutingInfoResult routingInfoResult, String msisdn) throws Exception {
		logger.info("MME: " + routingInfoResult.getMme());
		Future<Message> future = null;
		if (routingInfoResult.getMme().equals(env.getProperty("HQ.MME.HostName"))) {
			Request provideLocationAnswer = hqSlgClient.sendNextRequest(routingInfoResult.getImsi(), msisdn,
					env.getProperty("MME.realm.name"), routingInfoResult.getMme());
			future = hqSlgClient.getSession().send(provideLocationAnswer);
		} else if (routingInfoResult.getMme().equals(env.getProperty("AA.MME.HostName"))) {
			Request provideLocationAnswer = aaSlgClient.sendNextRequest(routingInfoResult.getImsi(), msisdn,
					env.getProperty("MME.realm.name"), routingInfoResult.getMme());
			future = aaSlgClient.getSession().send(provideLocationAnswer);
		} else if (routingInfoResult.getMme().equals(env.getProperty("HQ.VEPC.MME.HostName"))) {
			Request provideLocationAnswer = hqVepcSlgClient.sendNextRequest(routingInfoResult.getImsi(), msisdn,
					env.getProperty("MME.realm.name"), routingInfoResult.getMme());
			future = hqVepcSlgClient.getSession().send(provideLocationAnswer);
		} else if (routingInfoResult.getMme().equals(env.getProperty("AA.VEPC.MME.HostName"))) {
			Request provideLocationAnswer = aaVepcSlgClient.sendNextRequest(routingInfoResult.getImsi(), msisdn,
					env.getProperty("MME.realm.name"), routingInfoResult.getMme());
			future = aaVepcSlgClient.getSession().send(provideLocationAnswer);
		}
		return future;
	}

}
