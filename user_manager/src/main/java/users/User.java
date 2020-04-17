package users;

import java.util.HashMap;
import java.util.Map;

public class User {
    public Map<Integer, Integer> shares;
    public String name;
    public int balance;

    public User(String name, int balance) {
        shares = new HashMap<>();
        this.name = name;
        this.balance = balance;
    }
}
