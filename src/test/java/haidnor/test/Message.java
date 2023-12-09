package haidnor.test;

public class Message {

    private String msgId;

    private String content;

    public Message(String msgId, String content) {
        this.msgId = msgId;
        this.content = content;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
