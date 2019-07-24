package multiset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A MultiSet models a data structure containing elements along with their frequency count i.e., </p>
 * <p>the number of times an element is present in the set.</p>
 * <p>HashMultiSet is a Map-based concrete implementation of the MultiSet concept.</p>
 * 
 * <p>MultiSet a = <{1:2}, {2:2}, {3:4}, {10:1}></p>
 * */
public final class HashMultiSet<T, V> {

	/**
	 * XXX: data structure backing this MultiSet implementation.
	 */

	// Nota: si presuppone che trattandosi di frequenze, il tipo 'V' deriverà da 'Numbers'

	private List<T> oggetti;
	private List<V> frequenze;
	
	/**
	 * Sole constructor of the class.
	 **/
	public HashMultiSet() {

		// Creo una due array come ausilio alla struttura dati
		this.oggetti = new ArrayList<>();
		this.frequenze = new ArrayList<>();
	}
	
	
	/**
	 * If not present, adds the element to the data structure, otherwise 
	 * simply increments its frequency.
	 * 
	 * @param t T: element to include in the multiset
	 * 
	 * @return V: frequency count of the element in the multiset
	 * */	
	public V addElement(T t) {

		Integer n = 1;
		Integer index = oggetti.indexOf(t);

		if(index != -1)
		{
			n = (Integer) frequenze.get(index) + 1;
			frequenze.set(index, (V) n);
		}
		else
		{
			oggetti.add(t);
			frequenze.add((V) n);
		}

		return (V) n;
	}

	/**
	 * Check whether the elements is present in the multiset.
	 * 
	 * @param t T: element
	 * 
	 * @return V: true if the element is present, false otherwise.
	 * */	
	public boolean isPresent(T t)
	{
		return oggetti.indexOf(t) > -1;
	}
	
	/**
	 * @param t T: element
	 * @return V: frequency count of parameter t ('0' if not present)
	 * */

	@SuppressWarnings("unchecked")
	public V getElementFrequency(T t)
	{
		Integer index = oggetti.indexOf(t);
		return index == -1 ? (V) new Integer(0) : frequenze.get(index);
	}
	
	
	/**
	 * Builds a multiset from a source data file. The source data file contains
	 * a number comma separated elements. 
	 * Example_1: ab,ab,ba,ba,ac,ac -->  <{ab:2},{ba:2},{ac:2}>
	 * Example 2: 1,2,4,3,1,3,4,7 --> <{1:2},{2:1},{3:2},{4:2},{7:1}>
	 * 
	 * @param source Path: source of the multiset
	 * */

	@SuppressWarnings("unchecked")
	public void buildFromFile(Path source) throws IOException {

		// Controllo delle eccezioni
		if (source == null)
			throw new IOException("Method should be invoked with a non null file path");
		else
		{
			String firstLine = Files.readAllLines(source).get(0);
			buildFromCollection(
					(List<? extends T>) Stream.of(firstLine.split(","))
										.collect(Collectors.toList())
			);
		}
	}

	/**
	 * Same as before with the difference being the source type.
	 * @param source List<T>: source of the multiset
	 * */

	@SuppressWarnings("unchecked")
	public void buildFromCollection(List<? extends T> source) throws IllegalArgumentException {

		// Controllo delle eccezioni per source
		if (source == null)
			throw new IllegalArgumentException("Method should be invoked with a non null file path");
		else
			// Aggiungo gli elementi nella hash multiset
			source.stream()
					.forEach(element -> this.addElement(element));
	}
	
	/**
	 * Produces a linearized, unordered version of the MultiSet data structure.
	 * Example: <{1:2},{2:1}, {3:3}> -> 1 1 2 3 3 3
	 * 
	 * @return List<T>: linearized version of the multiset represented by this object.
	 */
	public List<T> linearize() {

		List<T> valoriReali = new ArrayList<>();

		// Linearizzo la lista multiset usando due cicli for

		for(int i=0; i < oggetti.size(); i++)
		{
			for(int j=0; j < (Integer) frequenze.get(i); j++)
			{
				valoriReali.add(oggetti.get(i));
			}
		}

		return valoriReali;
	}
	
	
}
