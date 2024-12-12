package com.example.accessibilitymonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "auth.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_TOKEN = "token";
    private static final String COLUMN_TOKEN_CREATED_AT = "token_created_at";

    private static final long TOKEN_EXPIRATION_PERIOD = 3600000;

    public AuthDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_TOKEN + " TEXT,"
                + COLUMN_TOKEN_CREATED_AT + " INTEGER"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Hash the password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT).trim();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Add a new user (Registration)
    public void addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password)); // Save hashed password
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    // Authenticate user by username or email and generate a token
    public String authenticateAndGenerateToken(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String hashedPassword = hashPassword(password);

        // Query for user with matching username or email and password
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL},
                "(" + COLUMN_EMAIL + "=? OR " + COLUMN_USERNAME + "=?) AND " + COLUMN_PASSWORD + "=?",
                new String[]{usernameOrEmail, usernameOrEmail, hashedPassword},
                null, null, null);

        if (cursor.moveToFirst()) {
            // Get the user ID to update the correct record
            int userIdIndex = cursor.getColumnIndex(COLUMN_ID);
            if (userIdIndex == -1) {
                cursor.close();
                db.close();
                return null; // Column not found, exit early
            }

            // Generate token and store it
            String token = generateToken();
            long currentTime = System.currentTimeMillis();

            // Update the token and its creation time
            ContentValues values = new ContentValues();
            values.put(COLUMN_TOKEN, token);
            values.put(COLUMN_TOKEN_CREATED_AT, currentTime);

            // Update the user with the new token
            db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(cursor.getInt(userIdIndex))});
            cursor.close();
            db.close();
            return token;
        } else {
            cursor.close();
            db.close();
            return null; // No matching user found
        }
    }


    // Generate a simple random token
    private String generateToken() {
        return Base64.encodeToString(String.valueOf(System.currentTimeMillis()).getBytes(), Base64.DEFAULT).trim();
    }

    // Check if a token has expired
    public boolean isTokenExpired(String token) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_TOKEN_CREATED_AT},
                COLUMN_TOKEN + "=?",
                new String[]{token},
                null, null, null);

        if (cursor.moveToFirst()) {
            // Ensure the column exists before accessing it
            int tokenCreatedAtIndex = cursor.getColumnIndex(COLUMN_TOKEN_CREATED_AT);
            if (tokenCreatedAtIndex == -1) {
                cursor.close();
                db.close();
                return true; // Treat as expired if column is not found
            }

            long tokenCreatedAt = cursor.getLong(tokenCreatedAtIndex);
            long currentTime = System.currentTimeMillis();
            cursor.close();
            db.close();
            return (currentTime - tokenCreatedAt) > TOKEN_EXPIRATION_PERIOD; // Check expiration
        }

        cursor.close();
        db.close();
        return true; // Treat as expired if token not found
    }


    // Logout (Clear token)
    public void logout(String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(COLUMN_TOKEN); // Remove token
        values.putNull(COLUMN_TOKEN_CREATED_AT); // Clear creation time
        db.update(TABLE_USERS, values, COLUMN_TOKEN + "=?", new String[]{token});
        db.close();
    }
}
