package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServidorSocket extends Thread {
    private static Map<String, PrintStream> MAP_CLIENTES;
    private Socket conexao;
    private String nomeCliente;
    private static List<String> LISTA_DE_NOMES = new ArrayList<String>();

    /* Controle sobre o Socket */
    public ServidorSocket(Socket socket) {
        this.conexao = socket;
    }

    /**
     * Armazena a lista de usuários
     * @param newName String com nome do usuário
     * @return boolean
     */
    public boolean adiciona(String newName) {
        for( String linha : LISTA_DE_NOMES){
            if(linha.equals(newName)){
                return true;
            }
        }

        LISTA_DE_NOMES.add(newName);
        return false;
    }

    public void remove(String oldName) {
        for(String linha : LISTA_DE_NOMES){
            if(linha.equals(oldName)){
                LISTA_DE_NOMES.remove(oldName);
            }
        }
    }

    /* Método Principal */
    public static void main(String args[]) {
        MAP_CLIENTES = new HashMap<String, PrintStream>();
        try {
            ServerSocket servidor = new ServerSocket(5555);
            System.out.println("Servidor rodando na porta 5555");
            while (true) {
                Socket conexao = servidor.accept();
                Thread t = new ServidorSocket(conexao);
                t.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void run() {
        try {
            BufferedReader msgRecebida = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            PrintStream msgEnviada = new PrintStream(this.conexao.getOutputStream());

            this.nomeCliente = msgRecebida.readLine();

            if (adiciona(this.nomeCliente)) {
                msgEnviada.println("Já existe um usuário conectado com esse nome. Informe outro, e conecte novamente.");
                this.conexao.close();
                return;
            } else {
                //mostra o nome do cliente conectado ao servidor
                System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
                //Quando o cliente se conectar recebe todos que estão conectados
                msgEnviada.println("Conectados: " + LISTA_DE_NOMES.toString());
            }
            if (this.nomeCliente == null) {
                return;
            }
            //adiciona os dados de saida do cliente no objeto MAP_CLIENTES
            //A chave será o nome e valor o printstream
            MAP_CLIENTES.put(this.nomeCliente, msgEnviada);
            String[] msg = msgRecebida.readLine().split(":");
            while (msg != null && !(msg[0].trim().equals(""))) {
                send(msgEnviada, " escreveu: ", msg);
                msg = msgRecebida.readLine().split(":");
            }
            System.out.println(this.nomeCliente + " saiu do bate-papo!");
            String[] out = {" do bate-papo!"};
            send(msgEnviada, " saiu", out);
            remove(this.nomeCliente);
            MAP_CLIENTES.remove(this.nomeCliente);
            this.conexao.close();
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        }
    }

    /**
     * Se o array da msg tiver tamanho igual a 1, então envia para todos
     * Se o tamanho for 2, envia apenas para o cliente escolhido
     */
    public void send(PrintStream saida, String acao, String[] msg) {
        out:
        for (Map.Entry<String, PrintStream> cliente : MAP_CLIENTES.entrySet()) {
            PrintStream chat = cliente.getValue();
            if (chat != saida) {
                if (msg.length == 1) {
                    chat.println(this.nomeCliente + acao + msg[0]);
                } else {
                    if (msg[1].equalsIgnoreCase(cliente.getKey())) {
                        chat.println(this.nomeCliente + acao + msg[0]);
                        break out;
                    }
                }
            }
        }
    }
}
