package backend;

import java.io.Serializable;
import java.util.HashMap;

public class AttributesSet implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1715795001063729680L;
	private Integer hourOfDay;
	private String partOfDay;
	private String activity;
	private Integer callCount;
	private String locationType;
	
	public Integer getHourOfDay() {
		return hourOfDay;
	}



	public void setHourOfDay(Integer hourOfDay) {
		this.hourOfDay = hourOfDay;
	}



	public String getPartOfDay() {
		return partOfDay;
	}



	public void setPartOfDay(String partOfDay) {
		this.partOfDay = partOfDay;
	}



	public String getActivity() {
		return activity;
	}



	public void setActivity(String activity) {
		this.activity = activity;
	}



	public Integer getCallCount() {
		return callCount;
	}



	public void setCallCount(Integer callCount) {
		this.callCount = callCount;
	}



	public String getLocationType() {
		return locationType;
	}



	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}
	
	
	public HashMap<String, String> getDelta(AttributesSet other){
		HashMap<String, String> map= new HashMap<String,String>();
		
		if(hourOfDay != null && hourOfDay!=other.hourOfDay){
			map.put("hourOfDay", hourOfDay.toString());
		}
		
		if(partOfDay != null && partOfDay!=other.partOfDay){
			map.put("partOfDay", partOfDay.toString());
		}
		if(activity != null && activity!=other.activity){
			map.put("activity", activity.toString());
		}
		if(callCount != null && callCount!=other.callCount){
			map.put("callCount", callCount.toString());
		}
		if(locationType != null && locationType!=other.locationType){
			map.put("locationType", locationType.toString());
		}
		
		return map;
	}

}
