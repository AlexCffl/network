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

public final class ClientTCP {

	private final KeyGenerator kg;
	private final SecretKey key;
	private final KeyFactory factory;
	
	public ClientTCP() {
		kg = KeyGenerator.getInstance("AES");
		key = kg.generateKey();
		factory = KeyFactory.getInstance("RSA");
	}
	
	public void put(String filepath) {
		try {
			//Recherche du serveur
			server = new Socket("localhost",4444);
			out = new PrintWriter(server.getOutputStream(), true, StandardCharsets.UTF_8);
			in = new BufferedReader(new InputStreamReader(server.getInputStream(), StandardCharsets.UTF_8));
			
			//Récupération de la clé publique du serveur
			var clePubliqueBase64 = in.readLine();
			var clePubliqueDecodee = Base64.getDecoder().decode(clePubliqueBase64);
			
			//Récupération de la clé publique enregistrée
			var clePubliqueConnue = new String(Files.readAllBytes(Paths.get("serveur_publique.key")),StandardCharsets.UTF_8);
			var clePubliqueConnueDecodee = Base64.getDecoder().decode(clePubliqueConnue);
			
			//Comparaison
            if (!java.util.Arrays.equals(clePubliqueDecodee, clePubliqueConnueDecodee)) {
            	System.out.println("Attention : la clé publique du serveur est inconnue !");
                echo.close();
                return;
            }

            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey clePublique = factory.generatePublic(new X509EncodedKeySpec(clePubliqueDecodee));

            //Envoi d'un défi au serveur
            byte[] defi = new byte[32];
            var random = new SecureRandom();
            random.nextBytes(defi);
            var defiBase64 = Base64.getEncoder().encodeToString(defi);
            out.println(defiBase64);

            //Réception de la signature
            var signatureRecue = in.readLine();
            byte[] signatureDecodee = Base64.getDecoder().decode(signatureRecue);

            //Vérification de la signature
            var verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(clePublique);
            verifier.update(defi);
            boolean authentifie = verifier.verify(signatureDecodee);
            if (authentifie) {
            	System.out.println("Serveur authentifié !");
            } else {
            	System.out.println("Authentification échouée !");
                echo.close();
                return;
            }
            
            //Préparation du chiffrement
            Cipher cipher = Cipher.getInstance("AES");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            //Échange des messages
            while ((userInput = stdin.readLine()) != null) {

                // Conversion du message en octets
                var data = userInput.getBytes(StandardCharsets.UTF_8);

                // Chiffrement
                cipher.init(Cipher.ENCRYPT_MODE, key);
                var result = cipher.doFinal(data);

                // Encodage Base64
                var messageChiffre = Base64.getEncoder().encodeToString(result);

                // Envoi au serveur
                out.println(messageChiffre);

                // Réception de la réponse
                String reponse = in.readLine();
                System.out.println("Serveur : " + reponse);

                if (userInput.equals("exit")) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	stdin.close();
            in.close();
            out.close();
            echo.close();
        }
	}
}
