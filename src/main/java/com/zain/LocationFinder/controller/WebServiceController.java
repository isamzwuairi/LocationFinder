package com.zain.LocationFinder.controller;

import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.jdiameter.api.Avp;
import org.jdiameter.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.zain.LocationFinder.model.Ati;
import com.zain.LocationFinder.model.Ecgi;
import com.zain.LocationFinder.model.Result;
import com.zain.LocationFinder.model.RoutingInfoResult;
import com.zain.LocationFinder.model.Sites;
import com.zain.LocationFinder.services.LocationFinderService;
import com.zain.LocationFinder.utils.MyUtils;

/**
 * @author Issam Zuwairi <Issam.AlZawairi@jo.zain.com>
 * @date: 24/07/2018
 * @version: 1.0
 *
 **/
@RestController
public class WebServiceController {

	private static final Logger logger = LoggerFactory.getLogger(WebServiceController.class);
	private static final Logger cdrWriter = LoggerFactory.getLogger("CdrGenerator");

	@Autowired
	private Environment env;

	private final LocationFinderService locationFinderService;

	public WebServiceController(LocationFinderService locationFinderService) {
		this.locationFinderService = locationFinderService;

	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Autowired
	RestTemplate restTemplate;

	@RequestMapping("/getLocation")
	public Result getLocation(@RequestParam(value = "msisdn") String msisdn, HttpServletRequest request)
			throws Exception {
		String ecgi = "";
		String resultCode = "";
		logger.info("Request received for: " + msisdn + ". Source IP: " + request.getRemoteAddr());
		msisdn = MyUtils.numberNormalization(msisdn);
		logger.info("Number after normalization: " + msisdn);
		Result returnValue = new Result(new Ecgi("NA", "NA", "NA"), "NA", "NA", "NA", "NA", "NA", 0, 0, "NA");
		if (msisdn == null || msisdn.isEmpty()) {
			logger.warn(returnValue.toString());
			return returnValue;
		}
		// Try GSM First
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(env.getProperty("ATI.URL") + msisdn, String.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				String atiBody = response.getBody();
				Ati myAti = MyUtils.getAti(atiBody);
				if (!myAti.getCellid().equals("-1")) {
					Sites myCell = getCellInfo(myAti.getCellid());
					if (myCell != null) {
						returnValue =  new Result(new Ecgi(myAti.getMcc(), myAti.getMnc(), myAti.getCellid()), myAti.getVlrNumber(),
								myCell.getName(), myCell.getGovernorate(), myCell.getDistrict(), myCell.getSubdistrict(),
								myCell.getLng(), myCell.getLat(), "GSM");
					} else {
						returnValue=  new Result(new Ecgi(myAti.getMcc(), myAti.getMnc(), myAti.getCellid()), myAti.getVlrNumber(), "Cell Not Found", "NA", "NA", "NA", 0, 0,
								"GSM");
					}
					
					logger.info(returnValue.toString());
					cdrWriter.info(msisdn + "|" + returnValue.writeCDR() + "|" + request.getRemoteAddr()); // Write a CDR
					return returnValue;
				}
			}
		} catch (HttpStatusCodeException exception) {
		    int statusCode = exception.getStatusCode().value();
		    logger.warn("MSISDN: " + msisdn + ". Unsuccessful call for ATI API. Http status code: " + statusCode);
		}
		// Try LTE
		RoutingInfoResult routingInfoResult = locationFinderService.sendSlhRequest(msisdn,
				env.getProperty("HSS.GMLC.Number"));
		if (routingInfoResult.getResultCode() == 2001) {
			logger.info("Slh details --> ResultCode: " + routingInfoResult.getResultCode() + " MME: "
					+ routingInfoResult.getMme() + " IMSI: " + routingInfoResult.getImsi());

			Future<Message> future = locationFinderService.sendSlgRequests(routingInfoResult, msisdn);

			if (future.get() != null) {
				for (Avp avp : future.get().getAvps()) {
					if (avp.getCode() == 2517) {
						ecgi = javax.xml.bind.DatatypeConverter.printHexBinary(avp.getOctetString());
					}
					if (avp.getCode() == 268) {
						resultCode = String.valueOf(avp.getUnsigned32());
					}
				}
			} else { // get MCC-MNC from Serving MME'S URI in case of no HPLMN MME (Roaming)
				ecgi = MyUtils.buildEcgiString(routingInfoResult.getMme());
				returnValue = new Result(MyUtils.getEcgi(ecgi), routingInfoResult.getMme(), "NA", "NA", "NA", "NA", 0,
						0, "LTE");
			}
		} else {
			logger.warn("MSISDN: " + msisdn + ". Unsuccessful slh request. Diameter result code: " + routingInfoResult.getResultCode());
		}

		if (resultCode.equals("2001")) {
			Ecgi myEcgi = MyUtils.getEcgi(ecgi);
			Sites myCell = getCellInfo(myEcgi.getCellID());
			if (myCell != null) {
				returnValue = new Result(myEcgi, routingInfoResult.getMme(), myCell.getName(), myCell.getGovernorate(),
						myCell.getDistrict(), myCell.getSubdistrict(), myCell.getLng(), myCell.getLat(), "LTE");
			} else {
				returnValue = new Result(myEcgi, routingInfoResult.getMme(), "Cell Not Found", "NA", "NA", "NA", 0, 0,
						"LTE");
			}
		} else {
			logger.warn("MSISDN: " + msisdn + ". Unsuccessful slg request. Diameter result code: " + resultCode);
		}
		logger.info(returnValue.toString());
		cdrWriter.info(msisdn + "|" + returnValue.writeCDR() + "|" + request.getRemoteAddr()); // Write a CDR
		return returnValue;
	}

	private Sites getCellInfo(String cellId) {
		return restTemplate.getForObject(env.getProperty("CellFinder.URL") + cellId, Sites.class);
	}
}
