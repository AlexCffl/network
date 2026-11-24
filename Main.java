import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

void main() throws Exception {
    // 1. Préparation de l'environnement (fichier propre + inscription d'Alice)
    Conn auth = new Conn();
    auth.inscription("Alice", "MonSuperMdp1!");
    auth.inscription("Natalia", "MonSuperMdp1!");
    auth.inscription("Alice", "MonSuperMdp1!");


    // Configuration JAAS intégrée directement en mémoire (pas besoin de fichier jaas.config)
    Configuration config = new Configuration() {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            return new AppConfigurationEntry[]{
                    new AppConfigurationEntry(
                            LoginModule.class.getName(),
                            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                            Map.of()
                    )
            };
        }
    };

    IO.println("=== 1. TESTS DE CONNEXION JAAS (login & commit) ===");

    // Test 1 : Connexion réussie
    LoginContext lcAlice = new LoginContext("TestApp", new Subject(), creerHandler("Alice", "MonSuperMdp1!"), config);
    boolean connexionReussie = false;
    try {
        lcAlice.login();
        connexionReussie = true;
    } catch (LoginException e) {
        connexionReussie = false;
    }
    verifier("Connexion valide avec Alice", connexionReussie == true);

    // Test 2 : Vérification du Subject et du Principal (Utilisateur)
    Subject subject = lcAlice.getSubject();
    boolean principalAjoute = subject.getPrincipals().stream()
            .anyMatch(p -> p.getName().equals("Alice"));
    verifier("Le Principal 'Alice' est bien ajouté au Subject", principalAjoute == true);

    // Test 3 : Déconnexion (logout)
    lcAlice.logout();
    verifier("Déconnexion vide bien les Principals du Subject", subject.getPrincipals().isEmpty() == true);


    IO.println("\n=== 2. TESTS D'ÉCHEC D'AUTHENTIFICATION ===");

    // Test 4 : Mauvais mot de passe
    verifier("Mauvais mot de passe refusé", testerEchec("Alice", "MauvaisMdp1!", config));

    // Test 5 : Utilisateur inconnu
    verifier("Utilisateur inconnu refusé", testerEchec("Inconnu", "MonSuperMdp1!", config));

    // Test 6 : Identifiants null (saisie vide)
    verifier("Identifiants null refusés", testerEchec(null, null, config));
}

// --- MÉTHODES UTILITAIRES ---

CallbackHandler creerHandler(String pseudo, String mdp) {
    return callbacks -> {
        for (Callback cb : callbacks) {
            if (cb instanceof NameCallback nc) {
                nc.setName(pseudo);
            } else if (cb instanceof PasswordCallback pc) {
                pc.setPassword(mdp != null ? mdp.toCharArray() : null);
            }
        }
    };
}

boolean testerEchec(String pseudo, String mdp, Configuration config) {
    try {
        LoginContext lc = new LoginContext("TestApp", new Subject(), creerHandler(pseudo, mdp), config);
        lc.login();
        return false; // Si on arrive ici, la connexion est passée alors qu'elle n'aurait pas dû
    } catch (LoginException e) {
        return true; // Comportement attendu : JAAS a bien rejeté la connexion
    }
}

void verifier(String nomDuTest, boolean condition) {
    if (condition) {
        IO.println("[OK]     " + nomDuTest);
    } else {
        IO.println("[ERREUR] " + nomDuTest);
    }
}
