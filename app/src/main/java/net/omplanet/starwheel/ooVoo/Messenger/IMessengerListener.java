package net.omplanet.starwheel.ooVoo.Messenger;

public interface IMessengerListener {
	
	public void onTextReceived(byte[] buffer, String participantName);

}
