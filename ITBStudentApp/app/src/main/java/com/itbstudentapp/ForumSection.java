package com.itbstudentapp;

public class ForumSection
{
    private String sectionName;
    private String sectionDesc;

    public ForumSection(){}

    public ForumSection(String sectionName, String sectionDesc) {
        this.sectionName = sectionName;
        this.sectionDesc = sectionDesc;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getSectionDesc() {
        return sectionDesc;
    }

    public void setSectionDesc(String sectionDesc) {
        this.sectionDesc = sectionDesc;
    }
}