package com.company;

/**
 * Created by BUBBABAIRD on 5/9/17.
 */
public class Contact {
    int id;
    int userId;
    String firstName;
    String lastName;
    String cell;
    int age;
    boolean gender;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public Contact(int userId, String firstName, String lastName, String cell, int age) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cell = cell;
        this.age = age;
    }
}
