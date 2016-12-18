package backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Aggregator {
	public Map<Integer, AttributesSet> aggregatedAttributes = new HashMap<Integer, AttributesSet>();

	HashMap<String, Integer> hourActivities;
	HashMap<String, Integer> hourLocations;



	public void aggregate(ArrayList<AttributesSet> attributes){
		Iterator<?> it = attributes.iterator();

		// these two values will not change over the hour
		Integer prevhour = null;
		String prevPartOfDay = null;

		//
		hourActivities = new HashMap<String, Integer>();
		hourLocations = new HashMap<String, Integer>();

		while(it.hasNext()){
			AttributesSet attr =  (AttributesSet) it.next();

			String activity = attr.getActivity();
			String location = attr.getLocationType();

			// if there is a change in the hour then aggregate attributes for the hour
			if (prevhour != null && prevhour != attr.getHourOfDay()){
				// store the most common attributes for the hour
				AttributesSet hourlyAttributes = new AttributesSet();

				hourlyAttributes.setHourOfDay(prevhour);
				hourlyAttributes.setPartOfDay(prevPartOfDay);
				hourlyAttributes.setActivity(getMaxActivity());
				hourlyAttributes.setLocationType(getMaxLocation());

				aggregatedAttributes.put(prevhour, hourlyAttributes);
				
				// re-initialize the arrays
				hourActivities = new HashMap<String, Integer>();
				hourLocations = new HashMap<String, Integer>();
			}

			addActivity(activity);
			addLocation(location);

			prevhour = attr.getHourOfDay();
			prevPartOfDay = attr.getPartOfDay();

		}
		
		// writing for the last set
		AttributesSet hourlyAttributes = new AttributesSet();

		hourlyAttributes.setHourOfDay(prevhour);
		hourlyAttributes.setPartOfDay(prevPartOfDay);
		hourlyAttributes.setActivity(getMaxActivity());
		hourlyAttributes.setLocationType(getMaxLocation());

		aggregatedAttributes.put(prevhour, hourlyAttributes);
		
		
	}

	private void addActivity(String activity){
		if(!hourActivities.containsKey(activity)) {
			hourActivities.put(activity,0);
		}
		else{
			hourActivities.put(activity, hourActivities.get(activity) + 1); 
		}
	}


	private void addLocation(String location){
		if(!hourLocations.containsKey(location)) {
			hourLocations.put(location,0);
		}
		else{
			hourLocations.put(location, hourLocations.get(location) + 1); 
		}
	}

	private String getMaxActivity(){
		Iterator<?> it = hourActivities.keySet().iterator();
		int max_count = 0;
		String max_activity = null;
		while(it.hasNext()){
			String activity = (String) it.next();
			int count = hourActivities.get(activity);

			// enter if either when it is the first case or the if the count > max_count
			if (max_activity == null || count > max_count){
				max_count = count;
				max_activity = activity;
			}
		}
		return max_activity;
	}

	private String getMaxLocation(){
		Iterator<?> it = hourLocations.keySet().iterator();
		int max_count = 0;
		String max_location = null;
		while(it.hasNext()){
			String activity = (String) it.next();
			int count = hourLocations.get(activity);

			// enter if either when it is the first case or the if the count > max_count
			if (max_location == null || count > max_count){
				max_count = count;
				max_location = activity;
			}
		}
		return max_location;
	}

	public void printAggregatedSet(){
		
		for(Integer key: aggregatedAttributes.keySet()){
			System.out.println(key + ": "+ aggregatedAttributes.get(key).getActivity() +", "+ aggregatedAttributes.get(key).getLocationType() + "," + aggregatedAttributes.get(key).getPartOfDay());
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ProcessJson pj = new ProcessJson();
		Aggregator agg = new Aggregator();

		String s = pj.readFile("data/large.json");
		pj.processDailyDataString(s);
		pj.printDailySet();
		agg.aggregate(pj.dailySet);
		agg.printAggregatedSet();
	}

}
