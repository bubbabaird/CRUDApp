package com.company;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS contacts (id IDENTITY, userId INT, firstName VARCHAR, lastName VARCHAR, cell VARCHAR, age INT)");
    }

    public static void insertUser(Connection conn, String username, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, username, password);
        }
        return null;
    }

    public static void insertContact(Connection conn, Contact contact) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO contacts VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setInt(1, contact.userId);
        stmt.setString(2, contact.firstName);
        stmt.setString(3, contact.lastName);
        stmt.setString(4, contact.cell);
        stmt.setInt(5, contact.age);
        stmt.execute();
    }

    public static Contact selectContact(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM contacts INNER JOIN users ON contacts.user_id = users.id WHERE contacts.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int userId = results.getInt("contacts.user_id");
            String firstName = results.getString("contacts.firstName");
            String lastName = results.getString("contacts.lastName");
            String cell = results.getString("contacts.cell");
            int age = results.getInt("contacts.age");
            return new Contact(userId, firstName, lastName, cell, age);
        }
        return null;
    }

    public static List<Contact> getContactsForUser(Connection connection, User user) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("select * from contacts where userId = ?");

        // replace the first question mark in the query
        // with the id of the user
        stmt.setInt(1, user.getId());

        // run the prepared statement and
        // get a series of results back.
        ResultSet results = stmt.executeQuery();

        List<Contact> contacts = new ArrayList<>();
        // while the results has a next row to process:
        while (results.next()) {
            // results.getString or results.getInt will attempt
            // to retrieve the values of the columns in this row.
            Contact c = new Contact(
                    user.getId(),
                    results.getString("firstName"),
                    results.getString("lastName"),
                    results.getString("cell"),
                    results.getInt("age"));

            contacts.add(c);
        }

        return contacts;
    }

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    // notice how we reference the session here...
                    Session session = request.session();

                    // and try to find the currently-logged-in user from session immediately after.
                    // this will be YOUR username after you login, for you and you only.
                    String username = session.attribute("username");
                    // we can't use the hashmap anymore...
                    //User user = users.get(username);

                    // ... we have to use SQL.
                    User user = selectUser(conn, username);

                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    }
                    else {
                        m.put("id", user.id);
                        m.put("name", user.username);
                        m.put("contacts", getContactsForUser(conn, user));
                        return new ModelAndView(m, "index.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String name = request.queryParams("username");
//                    User user = users.get(name);
//                    if (user == null) {
//                        user = new User(name);
//                        users.put(name, user);
//                    }


                    if (selectUser(conn, name) == null) {
                        insertUser(conn, name, request.queryParams("password"));
                    }

                    Session session = request.session();
                    session.attribute("username", name);

                    response.redirect("/");
                    return "";
                })
            );
            Spark.post(
                    "/create-contact",
                    ((request, response) -> {
                        // retrieve a user from the database
                        // the username we're interested in has the user name
                        // "request.session().attribute("username")"
                        User user = selectUser(conn, request.session().attribute("username"));

                        if (user == null) {
                            throw new Exception("User is not logged in");
                        }
    //                    int id;
    //                    int userId;
    //                    String firstName;
    //                    String lastName;
    //                    String cell;
    //                    int age;
                        int userId = Integer.valueOf(request.queryParams("userId"));
                        String firstName = request.queryParams("firstName");
                        String lastName = request.queryParams("lastName");
                        String cell = request.queryParams("cell");
                        int age = Integer.valueOf(request.queryParams("age"));
                        Contact contact = new Contact(userId, firstName, lastName, cell, age);

                        insertContact(conn, contact);

                        response.redirect("/");
                        return "";
                    })
            );
            Spark.post(
                    "/logout",
                    ((request, response) -> {
                        Session session = request.session();
                        session.invalidate();
                        response.redirect("/");
                        return "";
                    })
        );
    }
}
