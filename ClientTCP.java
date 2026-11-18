
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import java.security.spec.X509EncodedKeySpec;

import java.util.Base64;

public class ClientTCP {

    public static void main(String[] args) {

        try {

            // 1. Connexion au serveur
            Socket echo = new Socket("localhost", 4444);

            PrintWriter out = new PrintWriter(
                    echo.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
            );

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            echo.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );


            // 2. Récupération de la clé publique envoyée par le serveur
            String clePubliqueBase64 = in.readLine();

            byte[] clePubliqueDecodee = Base64.getDecoder()
                    .decode(clePubliqueBase64);


            // 3. Lecture de la clé publique de confiance
            // Elle doit avoir été copiée depuis le serveur
            String clePubliqueConnue = new String(
                    Files.readAllBytes(
                            Paths.get("serveur_publique.key")
                    ),
                    StandardCharsets.UTF_8
            );

            byte[] clePubliqueConnueDecodee = Base64.getDecoder()
                    .decode(clePubliqueConnue);


            // 4. Comparaison des deux clés publiques
            if (!java.util.Arrays.equals(
                    clePubliqueDecodee,
                    clePubliqueConnueDecodee)) {

                System.out.println(
                        "Attention : la clé publique du serveur est inconnue !"
                );

                echo.close();
                return;
            }


            // Reconstruction de la clé publique
            KeyFactory factory = KeyFactory.getInstance("RSA");

            PublicKey clePublique = factory.generatePublic(
                    new X509EncodedKeySpec(clePubliqueDecodee)
            );


            // 5. Génération d'un défi aléatoire
            byte[] defi = new byte[32];

            SecureRandom random = new SecureRandom();
            random.nextBytes(defi);

            String defiBase64 = Base64.getEncoder()
                    .encodeToString(defi);

            // Envoi du défi au serveur
            out.println(defiBase64);


            // 6. Réception de la signature
            String signatureRecue = in.readLine();

            byte[] signatureDecodee = Base64.getDecoder()
                    .decode(signatureRecue);


            // 7. Vérification de la signature
            Signature verifier = Signature.getInstance(
                    "SHA256withRSA"
            );

            verifier.initVerify(clePublique);

            verifier.update(defi);

            boolean authentifie = verifier.verify(
                    signatureDecodee
            );


            if (authentifie) {

                System.out.println("Serveur authentifié !");

            } else {

                System.out.println("Authentification échouée !");

                echo.close();
                return;
            }

            // 8. Génération de la clé DES
            KeyGenerator kg = KeyGenerator.getInstance("DES");

            SecretKey key = kg.generateKey();

            byte[] encodedKey = key.getEncoded();

            String encoded = Base64.getEncoder()
                    .encodeToString(encodedKey);


            // 9. Envoi de la clé DES au serveur
            out.println(encoded);

            System.out.println("Serveur : " + in.readLine());


            // 10. Préparation du chiffrement
            Cipher cipher = Cipher.getInstance("DES");

            BufferedReader stdin = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            String userInput;


            // 11. Échange des messages
            while ((userInput = stdin.readLine()) != null) {

                // Conversion du message en octets
                byte[] data = userInput.getBytes(
                        StandardCharsets.UTF_8
                );

                // Chiffrement
                cipher.init(Cipher.ENCRYPT_MODE, key);

                byte[] result = cipher.doFinal(data);

                // Encodage Base64
                String messageChiffre = Base64.getEncoder()
                        .encodeToString(result);

                // Envoi au serveur
                out.println(messageChiffre);


                // Réception de la réponse
                String reponse = in.readLine();

                System.out.println("Serveur : " + reponse);


                if (userInput.equals("exit")) {
                    break;
                }
            }


            // 12. Fermeture des connexions
            stdin.close();
            in.close();
            out.close();
            echo.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
