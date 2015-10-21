package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Recebedor implements Runnable {

    private ClienteSocket clienteSocket;
    private InputStream servidor;

    public Recebedor(ClienteSocket clienteSocket, InputStream servidor) {
        this.clienteSocket = clienteSocket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            //recebe mensagens de outro cliente através do servidor
            clienteSocket.entrada = new BufferedReader(new InputStreamReader(clienteSocket.socket.getInputStream()));
            //cria variavel de mensagem
            String msg;
            while (true) {
                // pega o que o servidor enviou
                msg = clienteSocket.entrada.readLine();
                //se a mensagem contiver dados, passa pelo if, caso contrario cai no break e encerra a conexao
                if (msg == null) {
                    System.out.println("Conexão encerrada!");
                    System.exit(0);
                }
                clienteSocket.areaTexto.append(msg + "\n");
            }
        } catch (IOException e) {
            // caso ocorra alguma exceção de E/S, mostra qual foi.
            System.out.println("Ocorreu uma Falha... .. ." + " IOException: " + e);
        }
    }
}
