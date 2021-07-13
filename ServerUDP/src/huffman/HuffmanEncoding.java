package huffman;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;



/*********************************************************************************************
 *  Ejecución: 
 *  	% Comprimir:    java entrada_salida.PlantillaCodificacionHuffman -c filePathIn filePathOut
 *      % Decomprimir:  java entrada_salida.PlantillaCodificacionHuffman -d filePathIn filePathOut
 *  
 *  Utilidad: Permite la compresión/descompresión usando el algoritmo de Huffman
 *  de un archivo de entrada hacia un archivo de salida. 
 *  
 *
 *********************************************************************************************/

public class HuffmanEncoding {

	
	// Constructor
	public HuffmanEncoding(){
		
	}
	
	/*
	* Se lee el archivo de entrada (filePathIn, a comprimir) como secuencia de palabras de 8 bits 
	* usando LectorBinario, después se codifica con el algoritmo de Huffman y el resultado 
	* se escribe usando la clase EscritorBinario hacia el archivo de salida (filePathOut, comprimido).
	*/
    public void encoding(String filePathIn, String filePathOut) {
		
    	LectorBinario lector = new LectorBinario(filePathIn);
		// Leer archivo de entrada y almacenar en una cadena
		StringBuilder sb = new StringBuilder();
		while (!lector.esVacio()) {
			char b = lector.leerPalabra();
			sb.append(b); 	// OJO! leerPalabra() devuelve una palabra 
							// de 8 bits y el tipo char es de 16 bits
		}
		char[] input = sb.toString().toCharArray();

		///////////////////////TAREA1.1///////////////////////
		// Generar tabla de frecuencias (freq) a partir del array de tipo char input.
		Map <Character,Integer> freq = new HashMap<Character, Integer>();
		
		/* Recorremos el array input, almacenando en el Map (tabla de frecuencias) 
		 * el carácter como clave y la frecuencia como valor */
		for (int i = 0; i < input.length; i++) {
			
			/* En caso de que el carácter se haya introducido anteriormente, se recupera
			 * su frecuencia hasta ese momento y se le suma uno */
			if(freq.containsKey(input[i])) {
				freq.put(input[i], freq.get(input[i]) + 1);
			}
			
			/* Si es la primera vez que aparece el carácter, se introduce con frecuencia = 1*/
			else {
				freq.put(input[i], 1);
			}
		}
		
		//////////////////////////////////////////////////////
		
		// Construir árbol de Huffman.
        ArbolHuffman arbol = construirArbol(freq); 
		
		// Construir diccionario de búsqueda -> Pares (símbolo,código).
		// diccionarioCodigos será una estructura de tipo Map, Hashtable, String[], ...,
		// dependiendo de la implementación elegida.
        Map<Character, String> diccionarioCodigos= new Hashtable<Character, String>();
        
        construirCodigos(diccionarioCodigos,arbol,"");
		
		// Codificar la trama (char[]input) usando el diccionario de códigos.
        codificar(input,diccionarioCodigos,filePathOut,arbol);
	}
	
   /* 
    * Construir arbol de Huffman a partir de la tabla de frecuencias.
    * (Si se ha usado una estructura Map para albergar la tabla de frecuencias).
    */
 	public ArbolHuffman construirArbol(Map<Character,Integer> freq) {
 		char clave;
 		int valor;
 		ArbolHuffman arbol, subarbol1, subarbol2, nuevoArbol = new ArbolHuffman();
 		///////////////////////TAREA1.2///////////////////////
 		PriorityQueue<ArbolHuffman> cola = new PriorityQueue<ArbolHuffman>();
        //////////////////////////////////////////////////////
    	
 		
    	///////////////////////TAREA1.3///////////////////////
 		
 		/* Iterator en el que almacenaremos las claves del Map de frecuencias*/
 		Iterator<Character> it = freq.keySet().iterator();
 		
 		/* Recorremos el Iterador, creamos un arbol de huffman por cada clave del Map
 		 * e incluimos dicho arbol en la cola de prioridad */
 		while(it.hasNext()) {
 			clave = (char) it.next();
 			valor = (int) freq.get(clave);
 			arbol = new ArbolHuffman(clave, valor, null, null);
 			cola.offer(arbol);
 		}
        //////////////////////////////////////////////////////
 		
    	///////////////////////TAREA1.4///////////////////////
    	
 		/* Construimos nuestro arbol de Huffman emparejando 
 		 * el par de subarboles con menores frecuencias */
 		while(cola.size() > 1){
    		subarbol1 = cola.poll();
    		subarbol2 = cola.poll();
    		
    		nuevoArbol = new ArbolHuffman('\0',subarbol1.getFrecuencia() + subarbol2.getFrecuencia(), subarbol1, subarbol2);
    		
    		cola.offer(nuevoArbol);
    		
    	}
    	
        //////////////////////////////////////////////////////  
    	
 		// Sustituir este objeto retornando el árbol de Huffman final 
 		// construido en la TAREA1.4
     	return nuevoArbol; 
 	}
    
   /* 
    * Construir diccionario de búsqueda -> Pares (símbolo,código).
    * (Si se usa una estructura Map para albergar el diccionario de códigos).
    */
    public void construirCodigos(Map<Character,String>  diccionarioCodigos, ArbolHuffman arbol,String codigoCamino){
    	
    	///////////////////////TAREA1.5///////////////////////
    	
    	/* Construimos mediante llamadas recursivas a este mismo método, el
    	 * diccionario de códigos. Si es un hijo izquierdo, el valor asignado es 0. 
    	 * Si es hijo derecho, 1. En caso de que se alcance un nodo hoja, se procede
    	 * a almacenar en el diccionario el carácter y su código. */
    	if(!arbol.esHoja()) {
    		construirCodigos(diccionarioCodigos, arbol.getIzquierdo(), codigoCamino + "0");
    		construirCodigos(diccionarioCodigos, arbol.getDerecho(), codigoCamino + "1");
    	}
    	else {
    		diccionarioCodigos.put(arbol.getSimbolo(), codigoCamino);
    	}
        //////////////////////////////////////////////////////
    }
    
   /* 
    * Codificar la trama (char[]input) usando el diccionario de códigos y escribirla en el
    * archivo de salida cuyo path (String filePathOut) se facilita como argumento.
    * (Si se usa una estructura Map para albergar el diccionario de códigos).
    */
    public void codificar(char[] input, Map<Character,String> diccionarioCodigos, String filePathOut, ArbolHuffman arbol){
    	
    	EscritorBinario escritor = new EscritorBinario(filePathOut);
    	
    	// Serializar árbol de Huffman para recuperarlo posteriormente en la descompresión.
        serializarArbol(arbol,escritor);
        
        // Escribir también el número de bytes del mensaje original (sin comprimir).
        escritor.escribirEntero(input.length);
    	
    	///////////////////////TAREA1.6///////////////////////
        // Codificación usando el diccionario de códigos y escritura en el archivo de salida. 
        
        /* Recorremos la trama de caracteres almacenada en input y obtenermos el código asignado para
         * cada carácter. Después escribimos el código de cada carácter bit a bit */
        for(int i = 0; i < input.length; i++) {
        	String codificacion = diccionarioCodigos.get(input[i]);
        	for(int j = 0; j < codificacion.length(); j++) {
        		if(codificacion.charAt(j) == '0'){
        			escritor.escribirBit(false);
        		}
        		else {
        			escritor.escribirBit(true);
        		}
            	
        	}
        }
        //////////////////////////////////////////////////////
        
    	escritor.cerrarFlujo();
    }
    
   /* 
    * Serializar árbol de Huffman para recuperarlo posteriormente en la descompresión. Se
    * escribe en la parte inicial del archivo de salida.
    */
    public void serializarArbol(ArbolHuffman arbol, EscritorBinario escritor){
    	
    	if (arbol.esHoja()) {
    		escritor.escribirBit(true);
    		escritor.escribirPalabra(arbol.getSimbolo()); //Escribir palabra de 8bits
    		return;
    	}
    	escritor.escribirBit(false);
    	serializarArbol(arbol.getIzquierdo(),escritor);
    	serializarArbol(arbol.getDerecho(),escritor);
    }
    
    /*
    * Se lee el archivo de entrada (filePathIn, a descomprimir) como secuencia de bits 
    * usando LectorBinario, después se descodifica usando el árbol final de Huffman y el resultado 
    * se escribe con la clase EscritorBinario en el archivo de salida (filePathOut, descomprimido).
    */
    public void decode(String filePathIn, String filePathOut) {
    
    	LectorBinario lector = new LectorBinario(filePathIn);
    	EscritorBinario escritor = new EscritorBinario(filePathOut);

    	ArbolHuffman arbol = leerArbol(lector);

    	// Númerod e bytes a escribir
    	int length = lector.leerEntero();

    	///////////////////////TAREA1.7///////////////////////
    	// Decodificar usando el árbol de Huffman.
    	for (int i = 0; i < length; i++) {
    		ArbolHuffman x = arbol;
    		while (!x.esHoja()) {
    			boolean bit = lector.leerBit();
    			if (bit) x = x.getDerecho();
    			else     x = x.getIzquierdo();
    		}
    		escritor.escribirPalabra(x.getSimbolo());
    	}
    	//////////////////////////////////////////////////////

    	escritor.cerrarFlujo();
    }
    
    public ArbolHuffman leerArbol(LectorBinario lector) {
    	
    	boolean esHoja = lector.leerBit();
    	if (esHoja) {
    		char simbolo = lector.leerPalabra();
    		return new ArbolHuffman(simbolo, -1, null, null);
    	}
    	else {
    		return new ArbolHuffman('\0', -1, leerArbol(lector), leerArbol(lector));
    	}
    }

//	public static void main(String[] args) {
//
//		PlantillaCodificacionHuffman huffman = new PlantillaCodificacionHuffman();
//		String path1=("C:\\redes2\\Codificacion-Huffman\\src\\entrada_salida\\in.txt");
//		String path2=("C:\\redes2\\Codificacion-Huffman\\src\\entrada_salida\\out.txt");
////				huffman.comprimir(path1,path2);
//
//				huffman.descomprimir(path2,path1);
//	}

}
