
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Servidor {

    private static ServerSocket server;
    private int porta = 1025;
    private final File baseFortunes = new File("fortune-br.txt");
    private final Random rng = new Random();

    public void start() {
        System.out.println("Servidor iniciado na porta: " + porta);
        try {
            server = new ServerSocket(porta);
            while (true) {
                try (Socket socket = server.accept();
                     DataInputStream entrada = new DataInputStream(socket.getInputStream());
                     DataOutputStream saida = new DataOutputStream(socket.getOutputStream())) {

                    String req = entrada.readUTF();
                    String resposta = parse(req);
                    saida.writeUTF(resposta);
                } catch (Exception cEx) {
                    cEx.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // PParse para o JSON
    private String parse(String json) {
        try {
            if (json == null) return "{\"result\":\"false\"}";
            String j = json.trim();
            String compact = j.replaceAll("\\s+", "");

            if (compact.contains("\"method\":\"read\"")) {
                String fortuna = read();
                fortuna = fortuna.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
                return "{\"result\":\"" + fortuna + "\"}";
            }

            if (compact.contains("\"method\":\"write\"")) {
                String arg = extrairPrimeiroArg(j);
                if (arg == null) return "{\"result\":\"false\"}";
                boolean ok = write(arg);
                if (!ok) return "{\"result\":\"false\"}";
                String eco = arg.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
                return "{\"result\":\"" + eco + "\"}";
            }

            return "{\"result\":\"false\"}";
        } catch (Exception e) {
            return "{\"result\":\"false\"}";
        }
    }

    // Extrai o primeiro elemento do array args
    private String extrairPrimeiroArg(String json) {
        try {
            int idxArgs = json.indexOf("\"args\"");
            if (idxArgs < 0) return null;
            int abreCol = json.indexOf('[', idxArgs);
            int fechaCol = json.indexOf(']', abreCol + 1);
            if (abreCol < 0 || fechaCol < 0) return null;
            String dentro = json.substring(abreCol + 1, fechaCol).trim();
            if (dentro.length() == 0) return "";
            if (dentro.charAt(0) == '\"') {
                StringBuilder sb = new StringBuilder();
                boolean escape = false;
                for (int i = 1; i < dentro.length(); i++) {
                    char c = dentro.charAt(i);
                    if (escape) {
                        if (c == 'n') sb.append('\n');
                        else if (c == 't') sb.append('\t');
                        else sb.append(c);
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '\"') {
                        break;
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String read() throws IOException {
        List<String> fortunes = carregarFortunes();
        if (fortunes.isEmpty()) return "Base de fortunas vazia.";
        return fortunes.get(rng.nextInt(fortunes.size()));
    }

    private boolean write(String frase) {
        if (frase == null) return false;
        frase = frase.replace("\r\n", "\n").trim();
        if (frase.isEmpty()) return false;
        // Tentamos usar o UTF-8, mas ficou imprimindo caracteres desconhecidos, então encontramos ISO_8859_1 que funciona aparentemente correto
        try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(baseFortunes, true), StandardCharsets.ISO_8859_1)) {
            fw.write("%\n");
            fw.write(frase);
            fw.write("\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> carregarFortunes() throws IOException {
        List<String> list = new ArrayList<>();
        if (!baseFortunes.exists()) return list;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(baseFortunes), StandardCharsets.ISO_8859_1))) {
            StringBuilder atual = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("%")) {
                    if (atual.length() > 0) {
                        list.add(atual.toString().trim());
                        atual.setLength(0);
                    }
                } else {
                    if (atual.length() > 0) atual.append("\n");
                    atual.append(line);
                }
            }
            if (atual.length() > 0) list.add(atual.toString().trim());
        }
        return list;
    }

    public static void main(String[] args) {
        new Servidor().start();
    }
}
