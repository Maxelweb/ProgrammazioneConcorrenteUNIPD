package pcd2018.exe2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Classe da completare per l'esercizio 2.
 */
public class DiffieHellman {

  /**
   * Limite massimo dei valori segreti da cercare
   */
  private static final int LIMIT = 65536;

  private final long p;
  private final long g;

  public DiffieHellman(long p, long g) {
    this.p = p;
    this.g = g;
  }

  /**
   * Metodo da completare
   * 
   * @param publicA valore di A
   * @param publicB valore di B
   * @return tutte le coppie di possibili segreti a,b
   */
  public List<Integer> crack(long publicA, long publicB)
  {

    System.out.println("Avvio..");
    long timerStart = System.currentTimeMillis();

    // =================================
    // Configurazione degli array per i calcoli e i risultati
    // =================================

    ArrayList<Long> arrayNotThreadSafeA = new ArrayList<>();
    ArrayList<Long> arrayNotThreadSafeB = new ArrayList<>();
    List<Long> aCandidates = Collections.synchronizedList(arrayNotThreadSafeA);
    List<Long> bCandidates = Collections.synchronizedList(arrayNotThreadSafeB);

    // =================================
    // Configurazione della scalabilità e del lavoro che devo fare
    // =================================

    int NumberOfCores = Runtime.getRuntime().availableProcessors();
    int NumberOfThreads = NumberOfCores * 2;
    long ThreadBound = (long) Math.ceil(LIMIT/NumberOfThreads);

    ExecutorService PoolThreads = Executors.newFixedThreadPool(NumberOfThreads);

    // =================================
    // Avvio dei lavori per ciascun thread all'interno di un pool
    // =================================

    for(int i = 0; i < NumberOfThreads; ++i)
    {
      PoolThreads.execute(new Calcolatore(ThreadBound * i, ThreadBound * (i+1), p, g, publicA, publicB, aCandidates, bCandidates));
      System.out.println("(/) Esecuzione Thread per range: [" + ThreadBound * i + ", "+ ThreadBound * (i+1) + "]");
    }

    // Chiudo il pool di thread
    PoolThreads.shutdown();


    // Attesa del Pool dei Thread che tutti abbiano concluso, altrimenti sollevo eccezione.
    try
    {
      PoolThreads.awaitTermination(45, TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      System.out.println("Errore: un thread ci ha messo troppo tempo per terminare i calcoli.");
      System.out.println("Descrizione: "+ e.getMessage());
    }

    // =================================
    // Verifico e reperisco i risultati dei calcoli precedenti
    // =================================

    List<Integer> chiaviTrovate = computeResults(publicA, publicB, aCandidates, bCandidates);

    long timerEnd = System.currentTimeMillis();

    System.out.println("-> Tempo di esecuzione: " + ((timerEnd - timerStart)/1000) + "s");

    // =================================
    // Ritorno i risultati
    // =================================

    return chiaviTrovate;
  }

  // =================================
  // Classe ausiliaria per la computazione delle chiavi risultanti
  // =================================

  public List<Integer> computeResults(long publicA, long publicB, List<Long> a_candidates, List<Long> b_candidates)
  {
    List<Integer> res = new ArrayList<>();

    for(long a : a_candidates)
    {
      for(long b : b_candidates)
      {
        if (DiffieHellmanUtils.modPow(publicB, a, p) == DiffieHellmanUtils.modPow(publicA, b, p))
        {
          res.add((int) a);
          res.add((int) b);
          System.out.println("chiave a = " + a);
          System.out.println("chiave b = " + b);
        }
      }
    }

    return res;
  }


  // =================================
  // Classe ausiliaria che compie i calcoli delle potenze di 'a' e 'b',
  // funzionante tramite Thread
  // =================================

  class Calcolatore extends Thread
  {

    private long fromNumber, toNumber, keyP, keyG, resA, resB;
    private List<Long> aResults;
    private List<Long> bResults;

    public Calcolatore(long from, long to, long p, long g, long A, long B, List<Long> a, List<Long> b)
    {
      this.fromNumber = from;
      this.toNumber = to;
      this.keyP = p;
      this.keyG = g;
      this.resA = A;
      this.resB = B;
      this.aResults = a;
      this.bResults = b;
    }

    @Override
    public void run()
    {
      for(long x = fromNumber; x < toNumber; ++x)
      {
          if (DiffieHellmanUtils.modPow(keyG, x, keyP) == resA)
            aResults.add(x);

          if (DiffieHellmanUtils.modPow(keyG, x, keyP) == resB)
            bResults.add(x);
      }
    }
  }

}
