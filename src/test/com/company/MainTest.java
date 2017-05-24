package com.company;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by BUBBABAIRD on 5/12/17.
 */
public class MainTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        User user = Main.selectUser(conn, "Alice");
        conn.close();
        assertTrue(user != null);
    }

    @Test
    public void testContact() throws SQLException {
        Connection conn = startConnection();
        User user = Main.selectUser(conn, "Alice");

        // using user id 1 because we know in the previous test
        // we inserted 1 user and since this is an in-memory database
        // we know that the 1 user will have id of 1.
        Contact c = new Contact(user.getId(), "Ben", "Sterrett", "numbers", 30);

        // perform the action:
        Main.insertContact(conn, c);

        // confirm that the action took place
        // we need a method to get all the contacts for a user.
        List<Contact> contacts = Main.getContactsForUser(startConnection(), user);

        // after we insert one contact, and we retrieve
        // all of Alice's contacts, the size of that list should
        // be 1.
        assertEquals(1, contacts.size());
    }
}
