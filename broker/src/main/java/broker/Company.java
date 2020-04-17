package broker;

public class Company {
    public String name;
    public Shares shares;

    public Company(String name, int price, int shares) {
        this.name = name;
        this.shares = new Shares(shares, price);
    }
}