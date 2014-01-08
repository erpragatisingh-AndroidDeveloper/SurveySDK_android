package com.survey.android.model;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.survey.android.webclient.RestClient;

public class UserModel {

	private static UserModel instance;

	public static UserModel getInstance() {
		if (instance == null) {
			instance = new UserModel();
		}
		return instance;
	}

	String authenticationToken = "";
	String birthDate = "";
	String education = "";
	String email = "";
	String ethnicity = "";
	boolean gender = false;
	String householdSize = "";
	String income = "";
	String industry = "";
	String country = "";
	String state = "";
	String city = "";
	String zipCode = "";
	String managementLevel = "";
	String martialStatus = "";
	String name = "";
	String lastName="";
	String occuptaion = "";
	String organizationId = "";
	String otherOccupation = "";
	String password = "";
	String phoneNumber = "";
	String userHash = null;
	String userId = "";
	String version = "";
	String language = "";
	String appsflyerid = "";
	String deviceId = "";
	String deviceType = "";
	String deviceOsVersion = "";		
	boolean success = false;

	public UserModel() {

	}

	public UserModel(String name,String lastName, String email, String password,
			String organizationId, String phoneNumber, String location,
			boolean gender, String birthDate, String education, String income,
			String martialStatus, String householdSize, String ethnicity,
			String industry, String occuptaion, String otherOccupation,
			String managementLevel, String userHash, String version, String deviceType) {
		super();
		this.name = name;
		this.lastName=lastName;
		this.email = email;
		this.password = password;
		this.organizationId = organizationId;
		this.phoneNumber = phoneNumber;
		this.country = location;
		this.gender = gender;
		this.birthDate = birthDate;
		this.education = education;
		this.income = income;
		this.martialStatus = martialStatus;
		this.householdSize = householdSize;
		this.ethnicity = ethnicity;
		this.industry = industry;
		this.occuptaion = occuptaion;
		this.otherOccupation = otherOccupation;
		this.managementLevel = managementLevel;
		this.userHash = userHash;
		this.version = version;
		this.deviceType = deviceType;		
	}

	public UserModel(String name,String lastName, String email, String password,
			String organizationId, String phoneNumber, String location,
			boolean gender, String birthDate, String education, String income,
			String martialStatus, String householdSize, String ethnicity,
			String industry, String occuptaion, String otherOccupation,
			String managementLevel, String userHash, String version, String appsflyerid, String deviceType) {
		super();
		this.name = name;
		this.lastName=lastName;
		this.email = email;
		this.password = password;
		this.organizationId = organizationId;
		this.phoneNumber = phoneNumber;
		this.country = location;
		this.gender = gender;
		this.birthDate = birthDate;
		this.education = education;
		this.income = income;
		this.martialStatus = martialStatus;
		this.householdSize = householdSize;
		this.ethnicity = ethnicity;
		this.industry = industry;
		this.occuptaion = occuptaion;
		this.otherOccupation = otherOccupation;
		this.managementLevel = managementLevel;
		this.userHash = userHash;
		this.version = version;
		this.appsflyerid = appsflyerid;
		this.deviceType = deviceType;		
	}
	
	public String getLanguage() {
		return language;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getEducation() {
		return education;
	}

	public String getEmail() {
		return email;
	}

	public String getEthnicity() {
		return ethnicity;
	}

	public boolean getGender() {
		return gender;
	}

	public String getHouseholdSize() {
		return householdSize;
	}

	public String getIncome() {
		return income;
	}

	public String getIndustry() {
		return industry;
	}

	public String getManagementLevel() {
		return managementLevel;
	}

	public String getMartialStatus() {
		return martialStatus;
	}

	public String getName() {
		return name;
	}
	
	public String getLastName(){
		return this.lastName;
	}

	public String getOccuptaion() {
		return occuptaion;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public String getOtherOccupation() {
		return otherOccupation;
	}

	public String getPassword() {
		return password;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getState() {
		return state;
	}

	public String getUserHash() {
		return userHash;
	}

	public String getUserId() {
		return userId;
	}

	public String getVersion() {
		return version;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getAppsFlyerId() {
		return appsflyerid;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public String getDeviceType() {
		return deviceType;
	}

	public String getDeviceOSVersion() {
		return deviceOsVersion;
	}	
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}

	public void setHouseholdSize(String householdSize) {
		this.householdSize = householdSize;
	}

	public void setIncome(String income) {
		this.income = income;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public void setManagementLevel(String managementLevel) {
		this.managementLevel = managementLevel;
	}

	public void setMartialStatus(String martialStatus) {
		this.martialStatus = martialStatus;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setLastName(String lastName){
		this.lastName=lastName;
	}

	public void setOccuptaion(String occuptaion) {
		this.occuptaion = occuptaion;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public void setOtherOccupation(String otherOccupation) {
		this.otherOccupation = otherOccupation;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setUserHash(String userHash) {
		this.userHash = userHash;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public void setAppsFlyerId(String appsflyerid) {
		this.appsflyerid = appsflyerid;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	public void setDeviceOSVersion(String deviceOsVersion) {
		this.deviceOsVersion = deviceOsVersion;
	}
	
	public void signIn() throws IOException, Exception {
		UserModel u = this;
		JSONObject object = new JSONObject();
		object.put("email", u.getEmail());
		object.put("password", u.getPassword());
		object.put("organization_id", u.getOrganizationId());		
		object.put("version", u.getVersion());
		JSONObject resultObject = RestClient.signIn(object);
		u.authenticationToken = resultObject.getString("authentication_token");
		u.success = resultObject.getBoolean("is_success");
		u.userId = resultObject.getString("user_id");
		// u.version = resultObject.getString("version");
	}
	
	public void signInOrRegister() throws IOException, Exception{
		UserModel u = this;
		JSONObject object = new JSONObject();
		object.put("email", u.getEmail());
		object.put("password", u.getPassword());
		object.put("organization_id", u.getOrganizationId());
		object.put("device_id", u.getDeviceId());
		object.put("device_type", u.getDeviceType());
		object.put("device_os_version", u.getDeviceOSVersion());
		object.put("first_name", u.getName());
		object.put("last_name", u.getLastName());	
		object.put("gender",u.getGender());
		object.put("birthdate",u.getBirthDate());
		
		JSONObject resultObject = RestClient.signInOrRegister(object);
		u.authenticationToken = resultObject.getString("token");
		u.userId = resultObject.getString("_id");
		if(u.getAuthenticationToken()!= null && u.getUserId() != null){
			u.success =true;	
		}
		
	}

	public void signUp() throws JSONException, IOException, Exception {
		UserModel userModel = this;
		JSONObject object = new JSONObject();
		JSONObject user = new JSONObject();
		user.put("first_name", userModel.getName());
		user.put("last_name", userModel.getLastName());		
		user.put("email", userModel.getEmail());
		user.put("password", userModel.getPassword());
		user.put("locale", userModel.getLanguage());
		user.put("device_type", userModel.getDeviceType());
		user.put("device_os_version", userModel.getDeviceOSVersion());				
		user.put("organization_id", userModel.getOrganizationId());
		user.put("app_flyer_id", userModel.getAppsFlyerId());
		
		JSONObject contact = new JSONObject();
		contact.put("phone_number", userModel.getPhoneNumber());
		contact.put("country", userModel.getCountry());
		contact.put("state", userModel.getState());
		contact.put("city", userModel.getCity());
		contact.put("zipcode", userModel.getZipCode());
		user.put("contact_info_attributes", contact);
		
		JSONObject demographic = new JSONObject();
		demographic.put("gender", userModel.getGender());
		demographic.put("birthdate", userModel.getBirthDate());
		demographic.put("education", userModel.getEducation());
		demographic.put("income", userModel.getIncome());
		demographic.put("marital_status", userModel.getMartialStatus());
		demographic.put("household_size", userModel.getHouseholdSize());
		demographic.put("ethnicity", userModel.getEthnicity());
		user.put("demographic_info_attributes", demographic);
		
		JSONObject career = new JSONObject();
		career.put("industry", userModel.getIndustry());
		career.put("occupation", userModel.getOccuptaion());
		career.put("other_occupation", userModel.getOtherOccupation());
		user.put("career_info_attributes", career);

		if (userHash != null) {
			JSONObject facebook = new JSONObject();
			JSONObject userHash = new JSONObject(userModel.getUserHash());
			facebook.put("user_hash", userHash);
			user.put("facebook_info", facebook);
		}

		object.put("user", user);
		object.put("version", userModel.getVersion());
		JSONObject resultObject = RestClient.signUp(object);
		
		if (resultObject.has("error_messages")) {
			String error = "";
			JSONObject errorMessages = resultObject
					.getJSONObject("error_messages");

			JSONArray errors = errorMessages.getJSONArray("user");
			for (int i = 0; i < errors.length(); i++) {
				error += errors.getString(i);
				if (i < errors.length() - 1)
					error += "\n";
			}
			
//			errors = errorMessages.getJSONArray("contact_info");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i) + "\n";
//			}
//			errors = errorMessages.getJSONArray("demographic_info");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i) + "\n";
//			}
//			errors = errorMessages.getJSONArray("career_info");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i) + "\n";
//			}

			throw new Exception(error);
		}
		userModel.authenticationToken = resultObject
				.getString("authentication_token");
		userModel.userId = resultObject.getString("user_id");
		// userModel.version = resultObject.getString("version");
	}
	
	public void editProfile(String token, String id) throws JSONException, IOException, Exception {
		UserModel userModel = this;
		JSONObject object = new JSONObject();
		JSONObject user = new JSONObject();
		user.put("first_name", userModel.getName());
		user.put("last_name", userModel.getLastName());		
		user.put("email", userModel.getEmail());
//		user.put("password", userModel.getPassword());
		user.put("locale", userModel.getLanguage());
		user.put("app_flyer_id", userModel.getAppsFlyerId());
		user.put("device_type", userModel.getDeviceType());
		user.put("device_os_version", userModel.getDeviceOSVersion());				
		
		JSONObject contact = new JSONObject();
		contact.put("phone_number", userModel.getPhoneNumber());
		contact.put("country", userModel.getCountry());
		contact.put("state", userModel.getState());
		contact.put("city", userModel.getCity());
		contact.put("zipcode", userModel.getZipCode());
		user.put("contact_info_attributes", contact);
		
		JSONObject demographic = new JSONObject();
		demographic.put("gender", userModel.getGender());
		demographic.put("birthdate", userModel.getBirthDate());
		demographic.put("education", userModel.getEducation());
		demographic.put("income", userModel.getIncome());
		demographic.put("marital_status", userModel.getMartialStatus());
		demographic.put("household_size", userModel.getHouseholdSize());
		demographic.put("ethnicity", userModel.getEthnicity());
		user.put("demographic_info_attributes", demographic);
		
		JSONObject career = new JSONObject();
		career.put("industry", userModel.getIndustry());
		career.put("occupation", userModel.getOccuptaion());
		career.put("other_occupation", userModel.getOtherOccupation());
		user.put("career_info_attributes", career);

		if (userHash != null) {
			JSONObject facebook = new JSONObject();
			JSONObject userHash = new JSONObject(userModel.getUserHash());
			facebook.put("user_hash", userHash);
			user.put("facebook_info", facebook);
		}

		object.put("user", user);
		object.put("version", userModel.getVersion());
		@SuppressWarnings("unused")
		JSONObject resultObject = RestClient.editProfile(object, token, id);
//		if (resultObject.has("error_messages")) {
//			String error = "";
//			JSONObject errorMessages = resultObject
//					.getJSONObject("error_messages");
//			JSONArray errors = errorMessages.getJSONArray("contact_info");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i) + "\n";
//			}
//			errors = errorMessages.getJSONArray("demographic_info");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i) + "\n";
//			}
//			errors = errorMessages.getJSONArray("career_info");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i) + "\n";
//			}
//			errors = errorMessages.getJSONArray("user");
//			for (int i = 0; i < errors.length(); i++) {
//				error += errors.getString(i);
//				if (i < errors.length() - 1)
//					error += "\n";
//			}
//			throw new Exception(error);
//		}
//		userModel.authenticationToken = resultObject.getString("authentication_token");
//		userModel.userId = resultObject.getString("user_id");
//		userModel.version = resultObject.getString("version");
	}
}
