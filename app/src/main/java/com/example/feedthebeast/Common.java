package com.example.feedthebeast;

import android.content.Context;
import android.widget.Toast;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
    // ngrok http 80 <-- Send PHP requests to port 80
    public static final String BASE_URL = "http://eed0-69-80-148-57.ngrok.io/FTBServer/login_signup/";
    public static final String SIGNUP_URL = BASE_URL + "SignUp.php";
    public static final String LOGIN_URL = BASE_URL + "LogIn.php";
    public static final String FEEDERS_URL = BASE_URL + "FeederList.php";
    public static final String ADD_FEEDER_URL = BASE_URL + "AddFeeder.php";
    public static final String EDIT_NAME_URL = BASE_URL + "EditName.php";
    public static final String REMOVE_FEEDER_URL = BASE_URL + "RemoveFeeder.php";
    public static final String GET_FEEDING_TIMES_URL = BASE_URL + "GetFeedingTimes.php";
    public static final String SET_FEEDING_TIMES_URL = BASE_URL + "SetFeedingTimes.php";
    public static final String EMAIL_EMPTY = "The email must not be empty.";
    public static final String EMAIL_INVALID = "The email provided is invalid.";
    public static final String USERNAME_EMPTY = "The username must not be empty.";
    public static final String PASSWORD_EMPTY = "The password must not be empty.";

    public static final UUID SERVICE_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

    public static String username = "";

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+" +
                            "(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|" +
                            "\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|" +
                            "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                            "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+" +
                            "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|" +
                            "\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}" +
                            "(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]" +
                            ":(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|" +
                            "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
                    Pattern.CASE_INSENSITIVE);

    public static void showMessage(Context context, String message, int length) {
        if (length != 0) {
            length = 1;
        }

        Toast.makeText(context, message, length).show();
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }

    public static boolean checkEmail(Context context, String email) {
        if (email.isEmpty()) {
            Common.showMessage(context, Common.EMAIL_EMPTY, Toast.LENGTH_SHORT);
            return false;
        }

        if (!Common.isValidEmail(email)) {
            Common.showMessage(context, Common.EMAIL_INVALID, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    public static boolean checkUsername(Context context, String username) {
        if (username.isEmpty()) {
            Common.showMessage(context, Common.USERNAME_EMPTY, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    public static boolean checkPassword(Context context, String password) {
        if (password.isEmpty()) {
            Common.showMessage(context, Common.PASSWORD_EMPTY, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }
}
