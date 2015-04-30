package net.omplanet.starwheel.model.domain;

import com.google.gson.annotations.SerializedName;

import net.omplanet.starwheel.model.api.APIConstants;

import java.util.Date;

public class Message extends Data {
	@SerializedName("from_user")
	private final String userName;

	@SerializedName("text")
	private final String message;

	@SerializedName("created_at")
	private String createdAt;

	private Date createdDate;

	public Message(String username, String message, String image){
		this.userName = username;
		this.message = message;
		super.image = image;
	}

	public String getUserName() {
		return userName;
	}

	public String getMessage() {
		return message;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public Date getCreatedDate(){
		if(createdDate == null && createdAt != null){
			try {
				createdDate = APIConstants.messageSdf.parse(createdAt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return createdDate;
	}
}
