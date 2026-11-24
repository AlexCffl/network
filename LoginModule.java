import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;

public class LoginModule implements javax.security.auth.spi.LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private String pseudo;
    private boolean succeeded = false;
    private Conn connexion;
    private char[] motDePasse;

    private boolean authentifie = false;
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if(callbackHandler == null){
            throw new LoginException("callbackhandler manquant");
        }
        NameCallback pseudo = new NameCallback("pseudo");
        PasswordCallback mdp = new PasswordCallback("mdp: ",false);

        try {
            callbackHandler.handle(new Callback[]{pseudo,mdp});
        } catch (IOException | UnsupportedCallbackException e) {
            throw new RuntimeException(e);
        }

        this.pseudo = pseudo.getName();
        this.motDePasse = mdp.getPassword();
        mdp.clearPassword();

        if(pseudo == null || motDePasse == null) {
            throw new LoginException("identifaints non saisie");
        }

        boolean ok = connexion.connexion(this.pseudo,new String(this.motDePasse));

        if(ok){
            succeeded = true;
            return true;
        }
        succeeded = false;
        return false;
    }

    @Override
    public boolean commit() throws LoginException {
        if(succeeded){
            return subject.getPrincipals().add(new Utilisateur(this.pseudo));
        }
        return false;
    }

    public boolean abort() {
        succeeded = false;
        pseudo = null;
        return true;
    }

    @Override
    public boolean logout() {
        subject.getPrincipals().clear();
        succeeded = false;
        pseudo = null;
        return true;
    }
}
