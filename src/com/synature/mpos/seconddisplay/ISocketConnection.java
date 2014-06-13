package com.synature.mpos.seconddisplay;

public interface ISocketConnection {
    public void send (String msg);
    public String receive();
}
