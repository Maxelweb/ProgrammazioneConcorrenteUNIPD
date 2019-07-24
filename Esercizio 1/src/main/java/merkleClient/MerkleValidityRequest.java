package merkleClient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static merkleClient.HashUtil.md5Java;

public class MerkleValidityRequest {

	/**
	 * IP address of the authority
	 * */
	private final String authIPAddr;
	/**
	 * Port number of the authority
	 * */
	private final int  authPort;
	/**
	 * Hash value of the merkle tree root. 
	 * Known before-hand.
	 * */
	private final String mRoot;
	/**
	 * List of transactions this client wants to verify 
	 * the existence of.
	 * */
	private List<String> mRequests;

	/**
	 * Sole constructor of this class - marked private.
	 * */
	private MerkleValidityRequest(Builder b){
		this.authIPAddr = b.authIPAddr;
		this.authPort = b.authPort;
		this.mRoot = b.mRoot;
		this.mRequests = b.mRequest;
	}

	/**
	 * <p>Method implementing the communication protocol between the client and the authority.</p>
	 * <p>The steps involved are as follows:</p>
	 * 		<p>0. Opens a connection with the authority</p>
	 * 	<p>For each transaction the client does the following:</p>
	 * 		<p>1.: asks for a validityProof for the current transaction</p>
	 * 		<p>2.: listens for a list of hashes which constitute the merkle nodes contents</p>
	 * 	<p>Uses the utility method {@link #isTransactionValid(String, String, List<String>) isTransactionValid} </p>
	 * 	<p>method to check whether the current transaction is valid or not.</p>
	 * */

	@SuppressWarnings("unchecked")
	public Map<Boolean, List<String>> checkWhichTransactionValid() throws IOException {

		// ===================================
		// Inizializzazione delle strutture dati
		// ===================================

		Map<Boolean, List<String>> risultati = new HashMap<>();
		List<String> HashFromServer = null;

		risultati.put(true, new ArrayList<>());
		risultati.put(false,  new ArrayList<>());

		// ===================================
		// Inizializzazione della connessione per ogni richiesta
		// ===================================

		debug("Connessione al server [" + authIPAddr + ":" + authPort + "]...");


		// Consumo ciascuna singola richiesta
		for(String currentRequest : mRequests)
		{

			// Passo 0.
			// Inizio della connessione col server

			try(Socket sock = new Socket(authIPAddr, authPort))
			{
				PrintWriter mexSending = new PrintWriter(sock.getOutputStream(), true);
				ObjectInputStream mexReceived = new ObjectInputStream(sock.getInputStream());


					// Invio al server le mie richieste
					mexSending.println(currentRequest);
					debug(" -> Invio TransID: " + currentRequest);

				// Ricezione degli hash da confrontare
				try
				{
					HashFromServer = (ArrayList<String>) mexReceived.readObject();
					debug("<- Ricezione Hash: " + HashFromServer);
				}
				catch(ClassNotFoundException e)
				{
					debug("Errore di comunicazione: ");
					System.out.print(e.getMessage());
				}

				// Controllo la validità della transazione
				if(HashFromServer != null && isTransactionValid(currentRequest, HashFromServer))
				{
					risultati.get(true).add(currentRequest);
					debug("==> Transazione valida!");
				}
				else
				{
					risultati.get(false).add(currentRequest);
					debug("==> Transazione non valida!");
				}


				// Chiudo gli I/O stream
				// Nota: Socket è autoclosable

				mexSending.close();
				mexReceived.close();

				// Il server rimarrà in attesa di altri contatti

			}
			catch (IOException exc)
			{
				debug("Connessione fallita!");
				throw exc;
			}
		}

		return risultati;

	}

	/**
	 * 	Checks whether a transaction 'merkleTx' is part of the merkle tree.
	 *
	 *  @param merkleTx String: the transaction we want to validate
	 *  @param merkleNodes String: the hash codes of the merkle nodes required to compute
	 *  the merkle root
	 *
	 *  @return: boolean value indicating whether this transaction was validated or not.
	 * */
	private boolean isTransactionValid(String merkleTx, List<String> merkleNodes) {
		String HashedString = merkleTx;
		debug("Calcolo il risultato...");
		for(String singleNode : merkleNodes)
		{
			HashedString += singleNode;
			HashedString = md5Java(HashedString);
		}
		return HashedString.equals(mRoot);
	}


	/**
	 * Classe ausiliaria per debugging del client
	 */

	public static void debug(String s)
	{
		System.out.println("[Client] "+ s);
	}


	/**
	 * Builder for the MerkleValidityRequest class. 
	 * */
	public static class Builder {
		private String authIPAddr;
		private int authPort;
		private String mRoot;
		private List<String> mRequest;

		public Builder(String authorityIPAddr, int authorityPort, String merkleRoot) {
			this.authIPAddr = authorityIPAddr;
			this.authPort = authorityPort;
			this.mRoot = merkleRoot;
			mRequest = new ArrayList<>();
		}

		public Builder addMerkleValidityCheck(String merkleHash) {
			mRequest.add(merkleHash);
			return this;
		}

		public MerkleValidityRequest build() {
			return new MerkleValidityRequest(this);
		}
	}
}