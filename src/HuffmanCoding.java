

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class HuffmanCoding {

	/**
	 * Used to denote seperation of each key in the binary data
	 */
    private static String KEY_SEPARATOR = "ÿÿ";
	
    /**
     * Builds a tree of character keys based on their frequencies
     * Modifed from: http://rosettacode.org/wiki/Huffman_coding#Java
     * @param toEncode The string to produce the keys for
     * @return A tree containing the keys
     */
    public static HuffmanTree buildTree(String toEncode) {
    	//Get the frequency of each character in the string to encode
        int[] frequencies = new int[256];
        Arrays.fill(frequencies, 0);
        for (char c : toEncode.toCharArray())
        	try {
        		frequencies[(int)c]++;
        	} catch (ArrayIndexOutOfBoundsException ex) {
        		System.out.println("Input to Huffman coding must contain only ASCII values 0 - 256");
        		throw ex;
        	}
        		
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
        // initially, we have a forest of leaves
        // one for each non-empty character
        for (int i = 0; i < frequencies.length; i++)
            if (frequencies[i] > 0)
                trees.offer(new HuffmanLeaf(frequencies[i], (char)i));
 
        assert trees.size() > 0;
        // loop until there is only one tree left
        while (trees.size() > 1) {
            // two trees with least frequency
            HuffmanTree a = trees.poll();
            HuffmanTree b = trees.poll();
 
            // put into new node and re-insert into queue
            trees.offer(new HuffmanNode(a, b));
        }
        return trees.poll();
    }
 
    /**
     * Returns text that shows the keys produced by the tree
     * Modified from: http://rosettacode.org/wiki/Huffman_coding#Java
     * Produces text:
     * x=010101011{KEY_SEPARATOR}t=01010{KEY_SEPARATOR
     * @param tree The tree with the keys of characters
     * @param prefix The current key as the tree is traversed
     * @return Key string
     */
    private static String getTextRepresentingKeys(HuffmanTree tree, StringBuffer prefix) {
    	StringBuilder encoded = new StringBuilder();
        
    	if (tree instanceof HuffmanLeaf)
            encoded.append(((HuffmanLeaf)(tree)).character).append(prefix.toString()).append(KEY_SEPARATOR );
    	else if (tree instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)tree;
 
            //Traverse left down tree
            prefix.append('0');
            encoded.append(getTextRepresentingKeys(node.left, prefix));
            prefix.deleteCharAt(prefix.length()-1);
 
            //Traverse right down tree
            prefix.append('1');
            encoded.append(getTextRepresentingKeys(node.right, prefix));
            prefix.deleteCharAt(prefix.length()-1);
        }
    	
    	return encoded.toString();
    }
    
    /**
     * A tree of the bit strings to represent a character
     * Modified from: http://rosettacode.org/wiki/Huffman_coding#Java
     * @author Alex
     *
     */
    private static abstract class HuffmanTree implements Comparable<HuffmanTree> {
    	/**
    	 * Weight of the node
    	 */
        public final int frequency;
        
        public HuffmanTree(int frequency) { 
        	this.frequency = frequency; 
        }
     
        /**
         * Frequency comparator
         */
        public int compareTo(HuffmanTree other) {
            return Integer.compare(frequency, other.frequency);
        }
    }
     
    /**
     * Represents a leaf on the encoding tree
     * @author Alex
     *
     */
    private static class HuffmanLeaf extends HuffmanTree {
    	/**
    	 * The character the leaf encodes
    	 */
        public char character;
     
        /**
         * Creates a leaf
         * @param frequency Weight of the node
         * @param character Encoded character
         */
        public HuffmanLeaf(int frequency, char character) {
            super(frequency);
            this.character = character;
        }
    }
     
    /**
     * Represents a node on the encoding tree
     * @author Alex
     *
     */
    private static class HuffmanNode extends HuffmanTree {
    	/**
    	 * Left subtree
    	 */
        public final HuffmanTree left;
        
        /**
         * Right subtree
         */
        public final HuffmanTree right;
     
        /**
         * Calculates the weight of both subtrees
         * @param l
         * @param r
         */
        public HuffmanNode(HuffmanTree l, HuffmanTree r) {
            super(l.frequency + r.frequency);
            left = l;
            right = r;
        }
    }
    
    /**
     * Maps a character to its bit string key
     * @author Alex
     *
     */
    private static class MapCharacterToKey {
    	/**
    	 * Character
    	 */
    	public char character;
    	
    	/**
    	 * Bit string
    	 */
    	public String key;
    	
    	/**
    	 * Retrieves the map from a text line i.e. b0101010 => b mapped to 0101010
    	 * @param mapTextLine
    	 */
    	public MapCharacterToKey(String mapTextLine) {
    		this(mapTextLine.charAt(0), mapTextLine.substring(1, mapTextLine.length() ));
    	}
    	
    	public MapCharacterToKey(char character, String key) {
    		this.character = character;
    		this.key = key;
    	}
    	
    	/**
    	 * Takes in input such as
    	 * a=1010
    	 * b=10
    	 * c=10010
    	 * Maps all the characters to the keys
    	 * @param keyText
    	 * @return
    	 */
    	public static List<MapCharacterToKey> retrieveFromString(String keyText) {
    		String[] mapLines = new StringBuilder(keyText).reverse().toString().split(KEY_SEPARATOR );
    		List<MapCharacterToKey> maps = new ArrayList<MapCharacterToKey>();
    		for (String mapLine : mapLines) {
    			try {
    				maps.add(new MapCharacterToKey(new StringBuilder(mapLine).reverse().toString()));
    			} catch (Exception ex) {
    				
    			}
    		}
    		return maps;
    	}
    	
    	/**
    	 * Given the character returns the bitstring key
    	 * @param maps
    	 * @param c
    	 * @return
    	 */
    	public static String encode (HashMap<Character, String> maps, char c) {
    		return maps.get(c);
    	}
    	
    	/**
    	 * Given the bit string key, returns the character
    	 * @param maps
    	 * @param binary
    	 * @return
    	 */
    	public static Character getCharacterFromKey(HashMap<String, Character> maps, String binary) {
    		return maps.get(binary);
    	}
    }
    
    /**
     * Gets a binary representation of the text which displays the keys
     * @param keyText
     * @return
     */
    private static String convertKeyTextToBinary(String keyText) {
    	StringBuilder sb = new StringBuilder();
    	String[] keys = keyText.split(KEY_SEPARATOR);
    	
    	for (String key : keys) {
    		sb.append(AsciiHelper.toBinary(key.charAt(0)));
    		sb.append(key.substring(1,	key.length()));
    		sb.append(AsciiHelper.toBinary(KEY_SEPARATOR));
    	}
    	
    	return sb.toString();
    }
    
    /**
     * Encodes the string 
     * @param toEncode
     * @return
     */
    public static String encode(String toEncode) {
    	//~ is a special character used to decode the binary representation
    	if (toEncode.contains("~")) {
    		System.out.println("Huffman algorithm cannot encode ~");
        	throw new ArrayIndexOutOfBoundsException();
        }
    	//Must have more than one character
    	if (toEncode.length() < 2) {
    		System.out.println("Encoded text must have more than 1 character");
    		throw new ArrayIndexOutOfBoundsException();
    	}
    	
        HuffmanTree tree = buildTree(toEncode);
        //Get the key representation in a text format
        String keyText = getTextRepresentingKeys(tree, new StringBuffer());
        List<MapCharacterToKey> maps = MapCharacterToKey.retrieveFromString(keyText);
        
        //Encode data using the keys
    	//Convert list to hashmap
    	HashMap mapsHash = new HashMap<Character, String>();
    	for (MapCharacterToKey map : maps)
    		mapsHash.put(map.character, map.key);
    	
        StringBuilder newData = new StringBuilder();
        char[] toEncodeArr = toEncode.toCharArray();
        for (char c : toEncodeArr)
        	newData.append(MapCharacterToKey.encode(mapsHash, c));
    	
        return convertKeyTextToBinary(keyText) +  AsciiHelper.toBinary("\n\n") + newData;
    }
    
    /**
     * Decodes the string
     * @param toDecode
     * @return
     */
    public static String decode(String toDecode) {
    	//Separate keyText from actual encoded data
    	//Two newlines are between the keyText and encoded data, the first occurance of three new lines will be the separation position
    	String[] sections = toDecode.split(AsciiHelper.toBinary("\n\n"));
    	String binaryKeyText = sections[0];
    	String[] keysInBinary = new StringBuilder(binaryKeyText).reverse().toString().split(AsciiHelper.toBinary(KEY_SEPARATOR));
    	HashMap<String, Character> maps = new HashMap<String, Character>();

    	
    	for (String keyInBinary : keysInBinary) {
    		//First 8 bytes = character
    		//Remaining bytes = key
    		keyInBinary = new StringBuilder(keyInBinary).reverse().toString();
    		try {
    			maps.put(keyInBinary.substring(8, keyInBinary.length()) , AsciiHelper.fromBinary(keyInBinary.substring(0,8)).charAt(0));
    		} catch (Exception ex) {
    		}
    	}	  
    	
    	//Get the non key part
    	String nonKey = "";
    	for (int i = 1; i < sections.length; i++)
    		nonKey += sections[i];
    	
    	StringBuilder decoded = new StringBuilder();
    	
    	StringBuilder currentCoded = new StringBuilder();
    	
    	for (int i = 0; i < nonKey.length(); i ++) {
    		currentCoded.append(nonKey.charAt(i));
    		Character potentialCharacter = MapCharacterToKey.getCharacterFromKey(maps, currentCoded.toString());
    		if (potentialCharacter != null) {
    			currentCoded = new StringBuilder();
    			decoded.append(potentialCharacter);
    		}
    	}
    	
    	return decoded.toString();
    }
}