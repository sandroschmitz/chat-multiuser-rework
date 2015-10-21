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

            this.nomeCliente = msgRecebida.readLine().trim();

            /* Sem nome de cliente, não aceita conexão */
            if (this.nomeCliente.length() == 0) {
                msgEnviada.println("Necessário nome de usuário. Informe um, e conecte novamente.");
                this.conexao.close();
                return;
            }

            if (adiciona(this.nomeCliente)) {
                msgEnviada.println("Já existe um usuário conectado com esse nome. Informe outro, e conecte novamente.");
                this.conexao.close();
                return;
            } else {
                System.out.println(this.nomeCliente + " : Conectado ao Servidor!");
                msgEnviada.println("Conectados: " + LISTA_DE_NOMES.toString());
            }

            /* Adiciona o nome do Usuario à string que será enviada */
            MAP_CLIENTES.put(this.nomeCliente, msgEnviada);

            String[] msg = msgRecebida.readLine().split(":");

            while (msg != null && !(msg[0].trim().equals(""))) {
                enviar(msgEnviada, " escreveu: ", msg);
                msg = msgRecebida.readLine().split(":");
            }

            System.out.println(this.nomeCliente + " saiu do bate-papo!");

            String[] out = {" do bate-papo!"};
            enviar(msgEnviada, " saiu", out);

            remove(this.nomeCliente);

            MAP_CLIENTES.remove(this.nomeCliente);
            this.conexao.close();

        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        }
    }

    /**
     * Envia mensagem recebida.
     * @param saida
     * @param acao
     * @param msg
     */
    public void enviar(PrintStream saida, String acao, String[] msg) {
        out:
        for (Map.Entry<String, PrintStream> cliente : MAP_CLIENTES.entrySet()) {
            PrintStream chat = cliente.getValue();

            if (chat != saida) {
                if (msg.length == 1) {
                    chat.println(this.nomeCliente + acao + msg[0]);
                } else {
                    if (msg[0].equalsIgnoreCase(cliente.getKey())) {
                        chat.println(this.nomeCliente + acao + msg[1]);
                        break out;
                    }
                }
            }
        }
    }
}
