package com.mobidroid.englishlessons.item;

import java.io.Serializable;

public class addVideo implements Serializable{

    private String id_course;
    private String id_video;
    private String title_video;
    private String title_course;
    private String first_question;
    private String second_question;
    private String third_question;

    private String first_answer;
    private String second_answer;
    private String third_answer;
    private String download_url;

    public addVideo() { }

    public String getId_course() {
        return id_course;
    }

    public void setId_course(String id_course) {
        this.id_course = id_course;
    }

    public String getId_video() {
        return id_video;
    }

    public void setId_video(String id_video) {
        this.id_video = id_video;
    }

    public String getFirst_question() {
        return first_question;
    }

    public void setFirst_question(String first_question) {
        this.first_question = first_question;
    }

    public String getSecond_question() {
        return second_question;
    }

    public void setSecond_question(String second_question) {
        this.second_question = second_question;
    }

    public String getThird_question() {
        return third_question;
    }

    public void setThird_question(String third_question) {
        this.third_question = third_question;
    }

    public String getFirst_answer() {
        return first_answer;
    }

    public void setFirst_answer(String first_answer) {
        this.first_answer = first_answer;
    }

    public String getSecond_answer() {
        return second_answer;
    }

    public void setSecond_answer(String second_answer) {
        this.second_answer = second_answer;
    }

    public String getThird_answer() {
        return third_answer;
    }

    public void setThird_answer(String third_answer) {
        this.third_answer = third_answer;
    }

    public String getTitle_video() {
        return title_video;
    }

    public void setTitle_video(String title_video) {
        this.title_video = title_video;
    }

    public String getTitle_course() {
        return title_course;
    }

    public void setTitle_course(String title_course) {
        this.title_course = title_course;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }
}
