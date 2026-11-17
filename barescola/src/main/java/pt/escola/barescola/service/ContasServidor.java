package pt.escola.barescola.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Contas de login no domínio de segurança do WildFly (ApplicationRealm).
 *
 * <p>Escreve nos mesmos ficheiros que a ferramenta {@code add-user} do WildFly
 * ({@code application-users.properties} e {@code application-roles.properties}), no mesmo formato:
 * a palavra-passe nunca é guardada, apenas {@code HEX(MD5(utilizador:ApplicationRealm:palavra-passe))}.
 * O WildFly volta a ler os ficheiros quando mudam, por isso a conta fica ativa de imediato.</p>
 *
 * <p>Em produção recomenda-se um {@code jdbc-realm} do Elytron com bcrypt (ver docs/checklist-seguranca.md).</p>
 */
@ApplicationScoped
public class ContasServidor {

    private static final Logger LOG = Logger.getLogger(ContasServidor.class.getName());
    private static final String REALM = "ApplicationRealm";
    private static final Object TRINCO = new Object();

    private Path pasta() {
        String dir = System.getProperty("jboss.server.config.dir");
        if (dir == null) {
            throw new IllegalStateException("jboss.server.config.dir não definido (a aplicação não está no WildFly?)");
        }
        return Path.of(dir);
    }

    public boolean existe(String utilizador) {
        synchronized (TRINCO) {
            try {
                return Files.readAllLines(pasta().resolve("application-users.properties"), StandardCharsets.UTF_8)
                        .stream()
                        .map(String::trim)
                        .anyMatch(l -> !l.startsWith("#") && l.startsWith(utilizador + "="));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public void criar(String utilizador, String palavraPasse, String papel) {
        synchronized (TRINCO) {
            acrescentar("application-users.properties", utilizador + "=" + hash(utilizador, palavraPasse));
            acrescentar("application-roles.properties", utilizador + "=" + papel);
            LOG.info(() -> "Conta criada no " + REALM + ": " + utilizador + " (" + papel + ")");
        }
    }

    private void acrescentar(String ficheiro, String linha) {
        Path p = pasta().resolve(ficheiro);
        try {
            String atual = Files.readString(p, StandardCharsets.UTF_8);
            String prefixo = atual.isEmpty() || atual.endsWith("\n") ? "" : "\n";
            Files.writeString(p, prefixo + linha + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar " + ficheiro, e);
        }
    }

    static String hash(String utilizador, String palavraPasse) {
        try {
            byte[] d = MessageDigest.getInstance("MD5")
                    .digest((utilizador + ":" + REALM + ":" + palavraPasse).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
