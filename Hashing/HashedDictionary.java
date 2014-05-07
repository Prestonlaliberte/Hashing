package Hashing;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;
public class HashedDictionary < K, V > implements Serializable
{
    private TableEntry < K, V > [] hashTable; // dictionary entries
    private int numberOfEntries;
    private int locationsUsed; // number of table locations not null
    private static final int DEFAULT_SIZE = 101; // must be prime
    private static final double MAX_LOAD_FACTOR = 0.5; // fraction of hash
    // table that can be filled


    public HashedDictionary ()
    {
        this (DEFAULT_SIZE); // call next constructor
    } // end default constructor


    public HashedDictionary (int tableSize)
    {
        int primeSize = getNextPrime(tableSize);
        hashTable = new TableEntry [primeSize];
        numberOfEntries = 0;
        locationsUsed = 0;
    } // end constructor

    public int getNextPrime(int size) {
        return size + 1;
    }

    public int getHashIndex(K key) {
        String s = key.toString();  // convert key to a string
        return s.hashCode() % hashTable.length;
    }

    public V getValue (K key)
    {
        V result = null;
        int index = getHashIndex(key);
        index = locate (index, key);
        if (index != -1)
            result = hashTable [index].getValue (); // key found; get value
        // else key not found; return null
        return result;
    } // end getValue


    public V remove (K key)
    {
        V removedValue = null;
        int index = getHashIndex (key);
        index = locate (index, key);
        if (index != -1)
        { // key found; flag entry as removed and return its value
            removedValue = hashTable [index].getValue ();
            hashTable [index].setToRemoved ();
            numberOfEntries--;
        } // end if
        // else key not found; return null
        return removedValue;
    } // end remove


    private int locate (int index, K key)
    {
        boolean found = false;
        while (!found && (hashTable [index] != null))
        {
            if (hashTable [index].isIn () &&
                    key.equals (hashTable [index].getKey ()))
                found = true; // key found
            else // follow probe sequence
                index = (index + 1) % hashTable.length; // linear probing
        } // end while
        // Assertion: either key or null is found at hashTable[index]
        int result = -1;
        if (found)
            result = index;
        return result;
    } // end locate


    public boolean isHashTableTooFull() {
        return  numberOfEntries >= (.85 * hashTable.length);
    }

    public V add (K key, V value)
    {
        V oldValue; // value to return
        if (isHashTableTooFull ())
            rehash ();
        int index = getHashIndex (key);
        index = probe (index, key); // check for and resolve collision
        // Assertion: index is within legal range for hashTable
        assert (index >= 0) && (index < hashTable.length);
        if ((hashTable [index] == null) || hashTable [index].isRemoved ())
        { // key not found, so insert new entry
            hashTable [index] = new TableEntry < K, V > (key, value);
            numberOfEntries++;
            locationsUsed++;
            oldValue = null;
        }
        else
        { // key found; get old value for return and then replace it
            oldValue = hashTable [index].getValue ();
            hashTable [index].setValue (value);
        } // end if
        return oldValue;
    } // end add


    private int probe (int index, K key)
    {
        boolean found = false;
        int removedStateIndex = -1; // index of first location in
        // removed state
        while (!found && (hashTable [index] != null))
        {
            if (hashTable [index].isIn ())
            {
                if (key.equals (hashTable [index].getKey ()))
                    found = true; // key found
                else // follow probe sequence
                    index = (index + 1) % hashTable.length; // linear probing
            }
            else // skip entries that were removed
            {
                // save index of first location in removed state
                if (removedStateIndex == -1)
                    removedStateIndex = index;
                index = (index + 1) % hashTable.length; // linear probing
            } // end if
        } // end while
        // Assertion: either key or null is found at hashTable[index]
        if (found || (removedStateIndex == -1))
            return index; // index of either key or null
        else
            return removedStateIndex; // index of an available location
    } // end probe


    private class KeyIterator implements Iterator < K >
    {
        private int currentIndex; // current position in hash table
        private int numberLeft; // number of entries left in iteration
        private KeyIterator ()
        {
            currentIndex = 0;
            numberLeft = numberOfEntries;
        } // end default constructor
        public boolean hasNext ()
        {
            return numberLeft > 0;
        } // end hasNext
        public K next ()
        {
            K result = null;
            if (hasNext ())
            {
                // find index of next entry
                while ((hashTable [currentIndex] == null) ||
                        hashTable [currentIndex].isRemoved ())
                {
                    currentIndex++;
                } // end while
                result = hashTable [currentIndex].getKey ();
                numberLeft--;
                currentIndex++;
            }
            else
                throw new NoSuchElementException ();
            return result;
        } // end next
        public void remove ()
        {
            throw new UnsupportedOperationException ();
        } // end remove
    } // end KeyIterator



    private void rehash ()
    {

        System.out.println("About to rehash");

        TableEntry < K, V > [] oldTable = hashTable;
        int oldSize = hashTable.length;
        int newSize = getNextPrime (oldSize + oldSize);
        hashTable = new TableEntry [newSize]; // increase size of array
        numberOfEntries = 0; // reset number of dictionary entries, since
        // it will be incremented by add during rehash
        locationsUsed = 0;
        // rehash dictionary entries from old array to the new and bigger
        // array; skip both null locations and removed entries
        for (int index = 0 ; index < oldSize ; index++)
        {
            if ((oldTable [index] != null) && oldTable [index].isIn ())
                add (oldTable [index].getKey (), oldTable [index].getValue ());
        } // end for
    } // end rehash


    private class TableEntry < S, T > implements Serializable
    {
        private S key;
        private T value;
        private boolean inTable; // true if entry is in hash table

        private TableEntry (S searchKey, T dataValue)
        {
            key = searchKey;
            value = dataValue;
            inTable = true;
        } // end constructor

        private void setKey(S inK) { key = inK; }
        private void setValue(T inV) { value = inV; }
        private void setToIn(boolean inIn) { inTable = inIn; }
        private void setToRemoved () { inTable = false; }

        private S getKey() { return key; }
        private T getValue() { return value; }
        private boolean isIn() { return inTable; }
        private boolean isRemoved() { return !inTable; }
    } // end TableEntry

    public static void main(String [] args) {
        HashedDictionary<Integer,String> hd = new HashedDictionary(101);
        hd.add(new Integer(5), new String("Hello"));
        System.out.println(hd.getValue(new Integer(5)));
    }
} // end HashedDictionary
