package sastra.panji.dhimas.firebase;

public class Users
{
    String Fname,Lname,FullName;

    public Users(String fname, String lname, String fullName) {
        Fname = fname;
        Lname = lname;
        FullName = fullName;
    }

    public String getFname() {
        return Fname;
    }

    public void setFname(String fname) {
        Fname = fname;
    }

    public String getLname() {
        return Lname;
    }

    public void setLname(String lname) {
        Lname = lname;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }
}
