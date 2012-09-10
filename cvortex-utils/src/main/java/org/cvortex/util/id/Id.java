package org.cvortex.util.id;

import java.io.Serializable;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pseudo unique identifier. The implementation is optimistically supposes
 * that the probability of independent creating two or more same identifiers
 * is extremely small. The identifier consists of 3 integers by 4 bytes each. 
 *
 * @author Dmitry Spasibenko
 *
 */
public final class Id implements Serializable {

    private static final long serialVersionUID = -312887954703779006L;

    private transient static final Random random = new Random();
    
    private transient static final AtomicInteger atomicInt = new AtomicInteger(random.nextInt());
    
    private transient static final int salt = getSalt();
    
    private final int id[];
    
    private String stringId;
    
    private int hashCode;
    
    public Id() {
        this.id = getNewId();
        this.stringId = convert(id);
    }
    
    public Id(String id) {
        this.stringId = id.toLowerCase();
        this.id = convert(stringId); 
    }
    
    public Id(Id id) {
        this.id = Arrays.copyOf(id.id, id.id.length);
        this.stringId = id.stringId;
    }

    @Override
    public boolean equals(Object id) {
        if (id instanceof Id) {
            return Arrays.equals(this.id, ((Id) id).id);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(id);
        }
        return hashCode;
    }
    
    @Override
    public String toString() {
        if (this.stringId == null) {
            this.stringId = convert(this.id);
        }
        return stringId;
    }    
    
    private static boolean isValid(String id) {
        if (id.length() != 24) {
            return false;
        }
        for (int idx = 0; idx < id.length(); idx++) {
            char c = id.charAt(idx);
            if (c >='0' && c <= '9') {
                continue;
            }
            if (c >= 'a' && c <= 'f') {
                continue;
            }
            return false;
        }
        return true;
    }
    
    private static int[] getNewId() {
        int id[] = new int[3];
        id[2] = (int)(System.currentTimeMillis() >> 10);
        id[1] = atomicInt.getAndIncrement();
        id[0] = salt;
        return id;
    }
    
    private static int[] convert(String stringId) {
        if (!isValid(stringId)) {
            throw new IllegalArgumentException("Invalid identifier " + stringId + ", it must contain 24 hexadecimal digits.");
        }
        int id[] = new int[3];
        for (int i = 0; i < 3; i++) {
            int startIdx = i << 3;
            int endIdx = startIdx + 8;
            // To avoid overflow issue which is not good for Integer.parseInt()
            long l = Long.parseLong(stringId.substring(startIdx, endIdx), 16);
            id[i] = (int) l;
        }
        return id;
    }
    
    private static String convert(int id[]) {
        StringBuilder sb = new StringBuilder(24);
        for (int idx = 0; idx < id.length; idx++) {
            String s = Integer.toHexString(id[idx]);
            if (s.length() < 8) {
                char[] zeros = new char[8 - s.length()];
                Arrays.fill(zeros, '0');
                sb.append(new String(zeros));
            }
            sb.append(s);
        }
        return sb.toString();
    }
    
    private static int getSalt() {
        StringBuilder sb = new StringBuilder(128);
        addRandomInt(sb);
        addNetworkInterfacesDesc(sb);
        addProcessDesc(sb);
        addLoaderHash(sb);
        return sb.toString().hashCode();
    }
    
    private static void addRandomInt(StringBuilder sb) {
        sb.append(Integer.toHexString(random.nextInt()));
    }
        
    private static void addNetworkInterfacesDesc(StringBuilder sb) {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            //oops
        }
        if (interfaces == null) {
            addRandomInt(sb);
            return;
        }
        while (interfaces.hasMoreElements() ){
            NetworkInterface intf = interfaces.nextElement();
            sb.append(intf.toString());
        }
    }
    
    private static void addProcessDesc(StringBuilder sb) {
        try {
            sb.append(java.lang.management.ManagementFactory.getRuntimeMXBean().getName());
        } catch (RuntimeException re) {
            addRandomInt(sb);
        }
    }
    
    private static void addLoaderHash(StringBuilder sb) {
        ClassLoader loader = Id.class.getClassLoader();
        if (loader != null) {
            sb.append(Integer.toHexString(System.identityHashCode(loader)));
        }
    }
    
}
