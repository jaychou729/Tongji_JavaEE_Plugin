package com.example.test.trackcode.datastruct;

public class CodeVersion {
    private String date;
    private String time;
    private String code;

    public CodeVersion(String date, String time, String code) {
        this.date = date;
        this.time = time;
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
