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
    // =================================
    // Configurazione degli array per i calcoli e i risultati
    // =================================

    List<Integer> res = new ArrayList<>();

    HashMap<Character, List<Long>> calcoli = new HashMap<>();

    ArrayList<Long> arrayNotThreadSafeA = new ArrayList<>();
    ArrayList<Long> arrayNotThreadSafeB = new ArrayList<>();

    List<Long> ThreadSafeA = Collections.synchronizedList(arrayNotThreadSafeA);
    List<Long> ThreadSafeB = Collections.synchronizedList(arrayNotThreadSafeB);

    calcoli.put('a', ThreadSafeA);
    calcoli.put('b', ThreadSafeB);

    // =================================
    // Configurazione della scalabilità e del lavoro da fare
    // =================================

    int NumberOfCores = Runtime.getRuntime().availableProcessors();
    int NumberOfThreads = NumberOfCores * 2;
    long ThreadBound = (long) Math.ceil(LIMIT/NumberOfThreads);

    ExecutorService EsecutoreDeiThreads = Executors.newFixedThreadPool(NumberOfThreads);

    // =================================
    // Avvio dei lavori per ciascun thread all'interno di un pool
    // =================================

    for(int i = 0; i < NumberOfThreads; ++i)
    {
      EsecutoreDeiThreads.execute(new Calcolatore(ThreadBound * i, ThreadBound * (i+1), p, g, publicA, publicB, calcoli));
      System.out.println("Esecuzione Thread per [" + ThreadBound * i + ", "+ ThreadBound * (i+1) + "]");
    }

    // Chiudo il pool di thread
    EsecutoreDeiThreads.shutdown();

    try
    {
      EsecutoreDeiThreads.awaitTermination(45, TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      System.out.println("Un thread ci ha messo troppo tempo per terminare i calcoli.");
    }

    // =================================
    // Verifico e reperisco i risultati
    // =================================

    for(long a : calcoli.get('a'))
    {
      for(long b : calcoli.get('b')) {
        if (DiffieHellmanUtils.modPow(publicB, a, p) == DiffieHellmanUtils.modPow(publicA, b, p))
        {
          res.add((int) a);
          res.add((int) b);
          System.out.println("a = " + a);
          System.out.println("b = " + b);
        }
      }
    }

    // =================================
    // Ritorno i risultati
    // =================================

    return res;
  }
}


// =================================
// Classe ausiliaria che compie i calcoli delle potenze di 'a' e 'b',
// funzionante tramite Thread
// =================================

class Calcolatore extends Thread {

  private long fromNumber, toNumber, keyP, keyG, resA, resB;
  private HashMap<Character, List<Long>> calcoli;

  public Calcolatore(long from, long to, long p, long g, long A, long B, HashMap<Character, List<Long>> calc)
  {
    this.fromNumber = from;
    this.toNumber = to;
    this.keyP = p;
    this.keyG = g;
    this.resA = A;
    this.resB = B;
    this.calcoli = calc;
  }

  @Override
  public void run()
  {
    for(long x = fromNumber; x < toNumber; ++x)
    {
      if(DiffieHellmanUtils.modPow(keyG, x, keyP) == resA)
        calcoli.get('a').add(x);

      if(DiffieHellmanUtils.modPow(keyG, x, keyP) == resB)
        calcoli.get('b').add(x);
    }
  }
}
