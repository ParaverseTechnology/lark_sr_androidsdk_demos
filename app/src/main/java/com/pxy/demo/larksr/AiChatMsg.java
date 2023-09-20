package com.pxy.demo.larksr;

public class AiChatMsg {
    public static final int TYPE_RECEIVED_AI = 0;
    public static final int TYPE_SENT_USER = 1;
    private String content;
    private int type;

    public AiChatMsg(String content,int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }
    public int getType() {
        return type;
    }
}
