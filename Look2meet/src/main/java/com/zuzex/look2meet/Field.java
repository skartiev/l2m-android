package com.zuzex.look2meet;

/**
 * Created by sanchirkartiev on 07.07.14.
 */
public class Field {
    public String firstField;
    public String secondField;
    public int isCheckbox;
    public String dataType;

    public Field(String firstField, String secondField, int isCheckbox, String dataType)
    {
        this.isCheckbox = isCheckbox;
        this.firstField = firstField;
        this.secondField = secondField;
        this.dataType = dataType;
    }
}
