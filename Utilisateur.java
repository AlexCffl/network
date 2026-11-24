import javax.security.auth.Subject;
import java.security.Principal;

public class Utilisateur implements Principal {
    private String name;
    public Utilisateur(String name){
        this.name = name;
    }

   public void setName(String name) {
        this.name = name;
   }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
