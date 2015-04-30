package net.omplanet.starwheel.model.api;

import net.omplanet.starwheel.model.domain.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageData {
	private List<Message> messages = new ArrayList<Message>();
	
	public MessageData(List<Message> messages) {
		this.messages = messages;
	}

	public List<Message> getMessages() {
		return messages;
	}
}
