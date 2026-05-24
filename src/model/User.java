package model;

public abstract class User {

    protected final long id;
    protected String username;
    protected String firstname;
    protected String lastname;
    protected String password;

    public User(long id, String username, String firstname, String lastname, String password) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }

    public long getId()            { return id; }
    public String getUsername()    { return username; }
    public String getFirstname()   { return firstname; }
    public String getLastname()    { return lastname; }
    public String getPassword()    { return password; }

    public void setUsername(String username)   { this.username = username; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public void setLastname(String lastname)   { this.lastname = lastname; }
    public void setPassword(String password)   { this.password = password; }
}