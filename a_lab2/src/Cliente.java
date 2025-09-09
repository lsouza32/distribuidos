
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Cliente {

    private static Socket socket;
    private static DataInputStream entrada;
    private static DataOutputStream saida;

    private int porta = 1025;

    private String enviar(String json) throws Exception {
        socket = new Socket("127.0.0.1", porta);
        entrada = new DataInputStream(socket.getInputStream());
        saida = new DataOutputStream(socket.getOutputStream());
        saida.writeUTF(json);
        String resposta = entrada.readUTF();
        socket.close();
        return resposta;
    }

    public void start() {
        System.out.println("Cliente iniciado. Conectando na porta: " + porta);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("\n=== MENU ===");
                System.out.println("1) read  - obter uma frase aleatória");
                System.out.println("2) write - adicionar uma nova frase");
                System.out.println("0) sair");
                System.out.print("> ");
                String opt = br.readLine();
                if (opt == null) break;
                opt = opt.trim();

                if ("0".equals(opt)) {
                    System.out.println("Saindo...");
                    break;
                } else if ("1".equals(opt) || opt.equalsIgnoreCase("read")) {
                    String req = "{\"method\":\"read\",\"args\":[\"\"]}";
                    String resp = enviar(req);
                    System.out.println("Servidor -> " + resp);
                } else if ("2".equals(opt) || opt.equalsIgnoreCase("write")) {
                    System.out.print("Digite a nova frase: ");
                    String frase = br.readLine();
                    if (frase == null) frase = "";
                    // Escapar aspas na frase para manter JSON simples
                    frase = frase.replace("\\", "\\\\").replace("\"", "\\\"");
                    String req = "{\"method\":\"write\",\"args\":[\"" + frase + "\"]}";
                    String resp = enviar(req);
                    System.out.println("Servidor -> " + resp);
                } else {
                    System.out.println("Opção inválida.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Cliente().start();
    }
}
