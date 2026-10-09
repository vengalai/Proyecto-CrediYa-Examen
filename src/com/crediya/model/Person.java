package com.crediya.model;

/** Abstract parent of Employee and Client (inheritance + encapsulation). */
public abstract class Person {
    private int id;
    private final String name;
    private final String document;
    private final String email;

    protected Person(int id, String name, String document, String email) {
        this.id = id;
        this.name = name;
        this.document = document;
        this.email = email;
    }

    public int getId()            { return id; }
    public void setId(int id)     { this.id = id; }
    public String getName()       { return name; }
    public String getDocument()   { return document; }
    public String getEmail()      { return email; }

    /** Each subclass says what kind of person it is (polymorphism). */
    public abstract String getType();

    /** One-line text used in console listings (polymorphism). */
    public abstract String describe();

    /** One-line record written to the text files (polymorphism). */
    public abstract String toFileLine();

    @Override
    public String toString() { return describe(); }
}