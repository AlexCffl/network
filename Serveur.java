
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;

public class Serveur {

    // Fichiers contenant les clés RSA du serveur
    private static final Path FICHIER_PRIVE =
            Paths.get("serveur_privee.key");

    private static final Path FICHIER_PUBLIC =
            Paths.get("serveur_publique.key");


    // Génération ou récupération de la paire de clés RSA
    public static KeyPair obtenirClesRSA() throws Exception {

        KeyFactory factory = KeyFactory.getInstance("RSA");

        // Si les clés existent déjà, on les récupère
        if (Files.exists(FICHIER_PRIVE)
                && Files.exists(FICHIER_PUBLIC)) {

            byte[] privee = Base64.getDecoder().decode(
                    new String(Files.readAllBytes(FICHIER_PRIVE),
                            StandardCharsets.UTF_8)
            );

            byte[] publique = Base64.getDecoder().decode(
                    new String(Files.readAllBytes(FICHIER_PUBLIC),
                            StandardCharsets.UTF_8)
            );

            PrivateKey clePrivee = factory.generatePrivate(
                    new PKCS8EncodedKeySpec(privee)
            );

            PublicKey clePublique = factory.generatePublic(
                    new X509EncodedKeySpec(publique)
            );

            return new KeyPair(clePublique, clePrivee);
        }

        // Sinon, génération d'une nouvelle paire RSA
        KeyPairGenerator generateur =
                KeyPairGenerator.getInstance("RSA");

        generateur.initialize(2048);

        KeyPair paire = generateur.generateKeyPair();

        // Sauvegarde des clés
        String privee = Base64.getEncoder()
                .encodeToString(paire.getPrivate().getEncoded());

        String publique = Base64.getEncoder()
                .encodeToString(paire.getPublic().getEncoded());

        Files.write(FICHIER_PRIVE,
                privee.getBytes(StandardCharsets.UTF_8));

        Files.write(FICHIER_PUBLIC,
                publique.getBytes(StandardCharsets.UTF_8));

        System.out.println("Paire de clés RSA générée.");

        return paire;
    }


    public static void main(String[] args) {

        try {

            // 1. Ouverture du serveur
            ServerSocket serveur = new ServerSocket(4444);

            System.out.println("Le serveur est lancé.");

            // 2. Génération ou récupération des clés RSA
            KeyPair paire = obtenirClesRSA();

            PrivateKey clePrivee = paire.getPrivate();
            PublicKey clePublique = paire.getPublic();

            // 3. Attente du client
            try (Socket clientSocket = serveur.accept();

                 PrintWriter out = new PrintWriter(
                         clientSocket.getOutputStream(),
                         true,
                         StandardCharsets.UTF_8
                 );

                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(
                                 clientSocket.getInputStream(),
                                 StandardCharsets.UTF_8
                         )
                 )) {

                System.out.println("Client connecté.");

                // 4. Envoi de la clé publique RSA
                String clePubliqueBase64 = Base64.getEncoder()
                        .encodeToString(clePublique.getEncoded());

                out.println(clePubliqueBase64);


                // 5. Réception du défi envoyé par le client
                String defiRecu = in.readLine();

                byte[] defi = Base64.getDecoder()
                        .decode(defiRecu);


                // 6. Signature du défi avec la clé privée RSA
                Signature signature = Signature.getInstance(
                        "SHA256withRSA"
                );

                signature.initSign(clePrivee);

                signature.update(defi);

                byte[] signatureNumerique = signature.sign();


                // 7. Envoi de la signature au client
                String signatureBase64 = Base64.getEncoder()
                        .encodeToString(signatureNumerique);

                out.println(signatureBase64);


                // 8. Réception de la clé DES
                String reception = in.readLine();

                byte[] decodedKey = Base64.getDecoder()
                        .decode(reception);

                SecretKey key = new SecretKeySpec(
                        decodedKey,
                        "DES"
                );

                System.out.println("Clé DES reçue.");

                out.println("Clé reçue correctement.");


                // 9. Préparation du chiffrement DES
                Cipher cipher = Cipher.getInstance("DES");

                String message;


                // 10. Réception et déchiffrement des messages
                while ((message = in.readLine()) != null) {

                    // Décodage Base64
                    byte[] donneesChiffrees = Base64.getDecoder()
                            .decode(message);

                    // Déchiffrement
                    cipher.init(Cipher.DECRYPT_MODE, key);

                    byte[] donneesClaires = cipher.doFinal(
                            donneesChiffrees
                    );

                    String original = new String(
                            donneesClaires,
                            StandardCharsets.UTF_8
                    );

                    System.out.println(
                            "Message original reçu : " + original
                    );


                    // Réponse au client
                    out.println(
                            "Bonjour client, j'ai reçu : " + original
                    );

                    // Arrêt si le client envoie exit
                    if (original.equals("exit")) {
                        break;
                    }
                }

                System.out.println("Fin de la communication.");

            }

            serveur.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
