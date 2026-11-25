import java.io.File;
import java.util.List;

public interface Fichier {

    public List<File> list();
    public byte[] convertirFichierVersByte();
    public File convertirByteVersFichier();


}
