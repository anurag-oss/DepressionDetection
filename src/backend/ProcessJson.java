package backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ProcessJson {
	Hashtable<Integer, String> locationTypes;
	JSONParser parser = new JSONParser();
	
	public ArrayList<AttributesSet> dailySet = new ArrayList<AttributesSet>();

	// Constructor that loads the location types
	public ProcessJson(){
		try {
			locationTypes = loadLocationTypes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void cleanUp(){
		dailySet = new ArrayList<AttributesSet>();
	}

	/**
	 * Public facing function that processes the daily json string
	 * @param json - in string format
	 */
	public void processDailyDataString(String json){
		JSONObject jObj = null;
		try {
			jObj = (JSONObject) parser.parse(json);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		processDailyData(jObj);
	}

	/**
	 * Extracts and processes the activity log
	 * This function will iterate over the current day's logs 
	 * and sends 5 min chunks to the processIntervalData function  
	 * @param jObj
	 */
	private void processDailyData(JSONObject jObj){
		JSONArray jArray = (JSONArray) jObj.get("activity_log");
		Iterator<?> dayDataIterator = jArray.iterator();


		while(dayDataIterator.hasNext()){
			JSONObject dayData = (JSONObject) dayDataIterator.next();
			Iterator<?> keys = dayData.keySet().iterator();

			while(keys.hasNext()){
				String key = (String) keys.next();
				JSONArray fiveMinData = (JSONArray) dayData.get(key);
				AttributesSet attrs = processIntervalData(fiveMinData);
				if(attrs.getHourOfDay() != null){
					dailySet.add(attrs);
				}
			}
		}

	}

	/**
	 * Given a json array representing the 5 min log 
	 * this fn will extract the relevant information
	 * @param jArray
	 * @return AttributeSet object containing attributes of that interval
	 */
	public AttributesSet processIntervalData(JSONArray jArray){

		// JSONParser parser = new JSONParser();

		Iterator<?> jsonIterator = jArray.iterator();
		AttributesSet attrs = new AttributesSet();

		while(jsonIterator.hasNext()){
			JSONObject object = (JSONObject) jsonIterator.next();
			String context = (String) object.get("context");


			try{
				if (context != null){
					if (context.equals("time")){
						ArrayList<String> timeDetails = processTime(object);
						Integer hourOfDay = Integer.parseInt(timeDetails.get(0));
						String partOfDay = timeDetails.get(1);

						attrs.setHourOfDay(hourOfDay);
						attrs.setPartOfDay(partOfDay);

//						System.out.println(hourOfDay);
//						System.out.println(partOfDay);

					}else if(context.equals("activity")){
						String activityType = processActivity(object);
//						System.out.println(activityType);
						attrs.setActivity(activityType);

					}else if(context.equals("location")){
						String type = processLocation(object);
//						System.out.println(type);
						attrs.setLocationType(type);
					}else if(context.equals("media")){
						processMedia();
					}else if(context.equals("application")){
						processApplication();
					}else if(context.equals("call")){
						int callCount = processCalls(object);
//						System.out.println(callCount);
						attrs.setCallCount(callCount);
					}
				}
			}catch(Exception e){
				System.out.println("Could not process json");
				break;
			}
		}
		return attrs;


	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private ArrayList<String> processTime(JSONObject object){

		ArrayList<String> timeDetails = new ArrayList<String>();

		long longTime = Long.parseLong(object.get("longTime").toString()) * 1000;

		Date d = new Date (longTime);
		String partOfDay = null;
		Integer hour = d.getHours();
		//		String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(d);

		if (hour < 6){
			partOfDay = "Night";
		}else if (hour < 12){
			partOfDay = "Morning";
		}else if (hour < 18){
			partOfDay = "Afternoon";
		}else{
			partOfDay = "Evening";
		}

		timeDetails.add(hour.toString());
		timeDetails.add(partOfDay);
		return timeDetails;
	}

	private String processLocation(JSONObject object){
		String type = "";
		//		System.out.println(object.get("venueType").toString().replaceAll("\\D+", " "));
		String venueName = object.get("venueName").toString();
		if (venueName.contains("Park West")){
			type = "Home";
		}else{
			String[] types = object.get("venueType").toString().replaceAll("\\D+", " ").split(" ");
			//		System.out.println(types[1]);
			int firstLocationId = Integer.parseInt(types[1]);
			type = locationTypes.get(firstLocationId);
		}
		return type;
	}

	private String processActivity(JSONObject object){
		String[] activity = object.get("activityType").toString().split("\\W");
		String activityType = activity[1];
		if (activityType.equals("standing")) activityType = "still"; 
		return activityType;
	}
	private void processMedia(){

	}
	private void processApplication(){

	}
	private int processCalls(JSONObject object){
		int callCount = Integer.parseInt(object.get("callCount").toString());
		return callCount;
	}


	public String readFile( String file ) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (file));
		String         line = null;
		StringBuilder  stringBuilder = new StringBuilder();
		String         ls = System.getProperty("line.separator");

		try {
			while( ( line = reader.readLine() ) != null ) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}

			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}

	private Hashtable<Integer, String> loadLocationTypes() throws IOException{
		BufferedReader reader = new BufferedReader( new FileReader ("data/placeType.txt"));
		String line = null;
		String category = null;
		Hashtable<Integer, String> locTypes = new Hashtable<Integer, String>();

		try {
			while( ( line = reader.readLine() ) != null) {
				if(!line.startsWith("------")){
					String[] contents = line.split(" = ");
					if(category.equals("Uncategorized")){
						locTypes.put(Integer.parseInt(contents[1]), contents[0]);
					}else{
						locTypes.put(Integer.parseInt(contents[1]), category);
					}
				}else{
					category = line.replace("------", "");
				}
			}
			return locTypes;
		} finally {
			reader.close();
		}
	}
	
	public void printDailySet(){
		Iterator<?> it = dailySet.iterator();
		
		while(it.hasNext()){
			AttributesSet attrs = (AttributesSet)it.next();
			System.out.println(attrs.getHourOfDay() + ": " + attrs.getActivity() + "," + attrs.getLocationType());
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ProcessJson pj = new ProcessJson();

		String s = pj.readFile("data/large.json");
		pj.processDailyDataString(s);
		pj.printDailySet();

	}

}
