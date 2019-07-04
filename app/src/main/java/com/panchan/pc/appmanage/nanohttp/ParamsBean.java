package com.panchan.pc.appmanage.nanohttp;

import java.io.Serializable;

public class ParamsBean implements Serializable {   //参过来的参数Bean

    private String msgType;



    public ParamsBean() {
    }


    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }


    @Override
    public String toString() {
        return "ParamsBean{" +
                "msgType='" + msgType + '\'' +
                '}';
    }
}
