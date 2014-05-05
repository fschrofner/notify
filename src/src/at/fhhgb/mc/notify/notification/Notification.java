package at.fhhgb.mc.notify.notification;

import java.util.ArrayList;

/**
 * Container class that is used to transfer notifications between methods.
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class Notification {
	private String title;
	private int startYear;
	private int startMonth;
	private int startDay;
	private int endYear;
	private int endMonth;
	private int endDay;
	private int startHours;
	private int startMinutes;
	private int endHours;
	private int endMinutes;
	private String message;
	private ArrayList<String> files;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getStartYear() {
		return startYear;
	}
	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}
	public int getStartMonth() {
		return startMonth;
	}
	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}
	public int getStartDay() {
		return startDay;
	}
	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}
	public int getEndYear() {
		return endYear;
	}
	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}
	public int getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}
	public int getEndDay() {
		return endDay;
	}
	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}
	public int getStartHours() {
		return startHours;
	}
	public void setStartHours(int startHours) {
		this.startHours = startHours;
	}
	public int getStartMinutes() {
		return startMinutes;
	}
	public void setStartMinutes(int startMinutes) {
		this.startMinutes = startMinutes;
	}
	public int getEndHours() {
		return endHours;
	}
	public void setEndHours(int endHours) {
		this.endHours = endHours;
	}
	public int getEndMinutes() {
		return endMinutes;
	}
	public void setEndMinutes(int endMinutes) {
		this.endMinutes = endMinutes;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ArrayList<String> getFiles() {
		return files;
	}
	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}
}
