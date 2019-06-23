package edu.buffalo.cse.cse486586.simpledht;



public class Message implements java.io.Serializable {

    private String msgValue;
    private String requestedPort;
    private  String currentPort;
    private  String finalPort;
    private  String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsgValue() {
        return msgValue;
    }

    public void setMsgValue(String msgValue) {
        this.msgValue = msgValue;
    }

    public String getRequestedPort() {
        return requestedPort;
    }

    public void setRequestedPort(String requestedPort) {
        this.requestedPort = requestedPort;
    }

    public String getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(String currentPort) {
        this.currentPort = currentPort;
    }

    public String getFinalPort() {
        return finalPort;
    }

    public void setFinalPort(String finalPort) {
        this.finalPort = finalPort;
    }
}
