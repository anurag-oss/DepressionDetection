package service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import backend.Aggregator;
import backend.ProcessJson;
import colibri.app.Analyzer;

@RestController
public class Controller {
	ProcessJson pj = new ProcessJson();
	Aggregator agg = new Aggregator();
	Analyzer ana = new Analyzer();
	JSONParser parser = new JSONParser();

	@RequestMapping(path = "/process", method=RequestMethod.POST)
	public JSONObject process(@RequestBody String dailyDataString) {
		
		System.out.println("Got a new request string. Processing starts......");
		//System.out.println(dailyDataString);
		JSONObject responseObj = null;
		try {
			// Process the string received from the front end
			//we will parse the json and extract the relevant events
			pj.cleanUp();
			pj.processDailyDataString(dailyDataString);
			System.out.println("The JSON has been processed and we got the below life events:");
			pj.printDailySet();
			
			// send the daily set to the aggregate fn
			agg.aggregate(pj.dailySet);
			System.out.println("Agrregating the above life events on a per hour basis:");
			agg.printAggregatedSet();
			
			// send to the analyzer
			ana.toBeExecuted(agg.aggregatedAttributes);
			
			responseObj = (JSONObject) parser.parse("{\"result\": \"done\"}");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseObj = (JSONObject) parser.parse("{\"result\": \"failed\"}");
			} catch (ParseException e1) {
			}
		}


		return responseObj;
	}
}
