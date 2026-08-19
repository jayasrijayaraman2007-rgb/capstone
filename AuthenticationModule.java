 import java.util.HashMap;
import java.util.Scanner;

class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}

public class AuthenticationModule {

    static HashMap<String, User> users = new HashMap<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        // Default Admin Account
        users.put("admin", new User("admin", "admin123"));

        while (true) {
            System.out.println("\n===== Visitor Entry & Gate Pass Management =====");
            System.out.println("1. createUsername");
            System.out.println("2. Login");
            System.out.println("3. Change Password");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

    case 1:
        createUsername();
        break;

    case 2:
        login();
        break;

    case 3:
        changePassword();
        break;

    case 4:
        System.exit(0);
        break;

    default:
        System.out.println("Invalid Choice!");
}
        }
    }

    public static void login() {

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        if (users.containsKey(username) &&
                users.get(username).login(username, password)) {

            System.out.println("Login Successful!");
        } else {
            System.out.println("Invalid Username or Password!");
        }
    }
    public static void createUsername() {
    System.out.print("Enter new username: ");
    String username = sc.nextLine();

    if (users.containsKey(username)) {
        System.out.println("Username already exists!");
        return;
    }

    System.out.print("Enter password: ");
    String password = sc.nextLine();

    users.put(username, new User(username, password));

    System.out.println("Account created successfully!");
}

    public static void changePassword() {

        System.out.print("Enter Username: ");
        String username = sc.nextLine();

        System.out.print("Enter Old Password: ");
        String oldPassword = sc.nextLine();

        if (users.containsKey(username) &&
                users.get(username).login(username, oldPassword)) {

            System.out.print("Enter New Password: ");
            String newPassword = sc.nextLine();

            users.get(username).changePassword(newPassword);

            System.out.println("Password Changed Successfully!");
        } else {
            System.out.println("Incorrect Username or Password!");
        }
    }
}
