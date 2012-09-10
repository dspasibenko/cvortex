package org.cvortex.util.id;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.cvortex.util.id.Id;
import org.junit.Assert;
import org.junit.Test;

public class IdTest extends Assert {

    @Test
    public void equalsStrTest() {
        Id id = new Id();
        String strId = id.toString();
        assertEquals(id, new Id(strId.toUpperCase()));
    }

    @Test
    public void equalsIdTest() {
        Id id = new Id();
        assertEquals(id, new Id(id));
    }
    
    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        Id id = new Id();
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        ObjectOutput out = new ObjectOutputStream(bos) ;
        out.writeObject(id);
        out.close();
        
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Id id2 = (Id) in.readObject();
        in.close();
        
        assertEquals(id, id2);
    }
    
    @Test
    public void wrongString() {
        try {
            new Id((String) null);
            fail("Should fail");
        } catch(NullPointerException npe) {
            // ok
        }
    }
    
    @Test
    public void wrongString2() {
        try {
            new Id("1234abc");
            fail("Should fail - wrong length");
        } catch(IllegalArgumentException iae) {
            // ok
        }
    }
    
    @Test
    public void wrongString3() {
        try {
            new Id("0123456789abcdef0123456h");
            fail("Should fail - wrong letter");
        } catch(IllegalArgumentException iae) {
            // ok
        }
    }

    @Test
    public void goodString() {
        goodStringTest("00123456789ABcDeF0123456");
        goodStringTest("000000000000000000000000");
        goodStringTest("FFFFFFFFFFFFFFFFFFFFFFFF");
        goodStringTest("FFFFFFFFFFFFF00000000000");
    }
    
    private void goodStringTest(String strId) {
        Id id = new Id("00123456789ABcDeF0123456");
        Id id2 = new Id(id);
        assertEquals("00123456789abcdef0123456", id2.toString());
        assertEquals(id, id2);
    }

    @Test
    public void wrongId() {
        try {
            new Id((Id) null);
            fail("Should fail - wrong id");
        } catch(NullPointerException npe) {
            // ok
        }
    }
}
