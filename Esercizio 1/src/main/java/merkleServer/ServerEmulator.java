package merkleServer;

import merkleClient.HashUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static merkleClient.HashUtil.md5Java;

public class ServerEmulator {

    final static int PORT = 2323;

    protected static Consumer<String> hashCode = x -> HashUtil.md5Java(x);

    private List<String> hashToCheck = new ArrayList<>();

    private void getNodes()
    {
        hashToCheck.add("prova1");
        hashToCheck.add("prova2");
        hashToCheck.add("prova3");
    }

    public static void main(String[] args) {

        debug("Avvio...");

        while(true)
        {

            try (   // Realizzo un server socket pronto all'ascolto
                    ServerSocket server = new ServerSocket(PORT);
                    Socket sock = server.accept()
            )
            {
                // Configuro lo stream per ricevere e inviare
                ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
                BufferedReader buff = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                try {
                    debug("Connessione ricevuta: " + sock.getRemoteSocketAddress());
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    debug(e.getMessage());
                }

                // Attendo richieste
                String incomingRequest = buff.readLine();

                if (incomingRequest == null)
                {
                    output.close();
                    buff.close();
                    continue;
                }

                debug("Richiesta ricevuta: " + incomingRequest);

                // Invio la lista degli hash richiesti verificando la connessione
                if (sock.isConnected()) {
                    output.writeObject(hashToCheck);
                    debug("Hash per la computazione inviati");
                    output.flush();
                } else
                    debug("Errore nell'invio degli hash");

                output.close();
                buff.close();


            } catch (IOException e) {
                debug("Errore: " + e.getMessage());
            }

        }
    }

    public static void debug(String s)
    {
        System.out.println("[Server] " + s);
    }
}
