import java.nio.file.Files;
import java.nio.file.Path;

void main() throws Exception {
    // Nettoyage avant de commencer pour partir d'un fichier vide
    Files.deleteIfExists(Path.of("utilisateurs.txt"));

    Conn auth = new Conn();

    IO.println("=== 1. TESTS DE ROBUSTESSE (estFort) ===");
    verifier("Mot de passe valide", auth.estFort("MonSuperMdp1!") == true);
    verifier("Trop court (< 12)", auth.estFort("Abc1!def") == false);
    verifier("Sans majuscule", auth.estFort("monsupermdp1!") == false);
    verifier("Sans minuscule", auth.estFort("MONSUPERMDP1!") == false);
    verifier("Sans chiffre", auth.estFort("MonSuperMdp!!") == false);
    verifier("Sans caractère spécial", auth.estFort("MonSuperMdp12") == false);

    IO.println("\n=== 2. TESTS D'INSCRIPTION (inscription & estPresent) ===");
    auth.inscription("Alice", "MonSuperMdp1!");
    verifier("Alice est bien inscrite", auth.estPresent("Alice") == true);

    auth.inscription("Bob", "faible");
    verifier("Bob (mdp faible) est refusé", auth.estPresent("Bob") == false);

    auth.inscription("Alice", "AutreMdpFort2@");
    verifier("Doublon Alice ignoré", auth.connexion("Alice", "MonSuperMdp1!") == true);

    IO.println("\n=== 3. TESTS DE CONNEXION (connexion) ===");
    verifier("Connexion Alice réussie", auth.connexion("Alice", "MonSuperMdp1!") == true);
    verifier("Mauvais mot de passe refusé", auth.connexion("Alice", "MauvaisMdp1!") == false);
    verifier("Utilisateur inconnu refusé", auth.connexion("Inconnu", "MonSuperMdp1!") == false);
}

// Petite méthode utilitaire pour afficher clairement le résultat
void verifier(String nomDuTest, boolean condition) {
    if (condition) {
        IO.println("[OK]     " + nomDuTest);
    } else {
        IO.println("[ERREUR] " + nomDuTest);
    }
}
