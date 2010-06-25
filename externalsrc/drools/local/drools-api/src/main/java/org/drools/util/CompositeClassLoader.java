package org.drools.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class CompositeClassLoader extends ClassLoader {
    /* Assumption: modifications are really rare, but iterations are frequent. */
    private final List<ClassLoader>       classLoaders = new CopyOnWriteArrayList<ClassLoader>();
    private final AtomicReference<Loader> loader       = new AtomicReference<Loader>();

    public CompositeClassLoader(final ClassLoader parentClassLoader) {
	// BEGIN cmg
	// On Android parent class loader CANNOT be null
        //super( null );
	super( ClassLoader.getSystemClassLoader() );
	// END cmg
        loader.set( new DefaultLoader() );
    }

    public synchronized void setCachingEnabled(boolean enabled) {
        if ( enabled && loader.get() instanceof DefaultLoader ) {
            loader.set( new CachingLoader() );
        } else if ( !enabled && loader.get() instanceof CachingLoader ) {
            loader.set( DefaultLoader.INSTANCE );
        }
    }

    public synchronized void addClassLoader(final ClassLoader classLoader) {
        /* NB: we need synchronized here even though we use a COW list:
         *     two threads may try to add the same new class loader, so we need
         *     to protect over a bigger area than just a single iteration.
         */
        // don't add duplicate ClassLoaders;
        for ( final ClassLoader cl : this.classLoaders ) {
            if ( cl == classLoader ) {
                return;
            }
        }
        this.classLoaders.add( classLoader );
        this.loader.get().reset();
    }

    public synchronized void removeClassLoader(final ClassLoader classLoader) {
        /* synchronized to protect against concurrent runs of 
         * addClassLoader(x) and removeClassLoader(x).
         */
        classLoaders.remove( classLoader );
        this.loader.get().reset();
    }

    /**
     * This ClassLoader never has classes of it's own, so only search the child ClassLoaders
     * and the parent ClassLoader if one is provided
     */
    public Class< ? > loadClass(final String name,
                                final boolean resolve) throws ClassNotFoundException {
        return loader.get().load( this,
                                  name,
                                  resolve );
    }

    /**
     * This ClassLoader never has classes of it's own, so only search the child ClassLoaders
     * and the parent ClassLoader if one is provided
     */
    public InputStream getResourceAsStream(final String name) {
        for ( final ClassLoader classLoader : this.classLoaders ) {
            InputStream stream = classLoader.getResourceAsStream( name );
            if ( stream != null ) {
                return stream;
            }
        }

        return null;
    }

    @Override
    public URL getResource(String name) {
        for ( final ClassLoader classLoader : this.classLoaders ) {
            URL url = classLoader.getResource( name );
            if ( url != null ) {
                return url;
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        CompositeEnumeration<URL> enumerations = new CompositeEnumeration<URL>();

        for ( final ClassLoader classLoader : this.classLoaders ) {
            Enumeration<URL> e = classLoader.getResources( name );
            if ( e != null ) {
                enumerations.addEnumeration( e );
            }
        }

        if ( enumerations.size() == 0 ) {
            return null;
        } else {
            return enumerations;
        }
    }

    public void dumpStats() {
        System.out.println( loader.toString() );
    }

    private static interface Loader {
        public Class< ? > load(final CompositeClassLoader cl,
                               final String name,
                               final boolean resolve);

        public void reset();
    }

    private static class DefaultLoader
        implements
        Loader {

        // this class is stateless, so lets make a singleton of it
        public static final DefaultLoader INSTANCE = new DefaultLoader();

        private DefaultLoader() {
        }

        public Class< ? > load(final CompositeClassLoader cl,
                               final String name,
                               final boolean resolve) {
            // search the child ClassLoaders
            Class< ? > cls = null;

            for ( final ClassLoader classLoader : cl.classLoaders ) {
                try {
                    cls = Class.forName( name,
                                         true,
                                         classLoader );
                } catch ( ClassNotFoundException e ) {
                    // swallow as we need to check more classLoaders
                }
                if ( cls != null ) {
                    break;
                }
            }

            if ( resolve ) {
                cl.resolveClass( cls );
            }

            return cls;
        }

        public void reset() {
            // nothing to do
        }
    }

    private static class CachingLoader
        implements
        Loader {

        private final Map<String, Object> classLoaderResultMap = new HashMap<String, Object>();
        public long                       successfulCalls      = 0;
        public long                       failedCalls          = 0;
        public long                       cacheHits            = 0;

        public Class< ? > load(final CompositeClassLoader cl,
                               final String name,
                               final boolean resolve) {
            if ( classLoaderResultMap.containsKey( name ) ) {
                cacheHits++;
                return (Class< ? >) classLoaderResultMap.get( name );
            }
            // search the child ClassLoaders
            Class< ? > cls = null;

            for ( final ClassLoader classLoader : cl.classLoaders ) {
                try {
                    cls = Class.forName( name,
                                         true,
                                         classLoader );
                } catch ( ClassNotFoundException e ) {
                    // swallow as we need to check more classLoaders
                }
                if ( cls != null ) {
                    break;
                }
            }

            if ( resolve ) {
                cl.resolveClass( cls );
            }

            classLoaderResultMap.put( name,
                                      cls );
            if ( cls != null ) {
                this.successfulCalls++;
            } else {
                this.failedCalls++;
            }

            return cls;
        }

        public void reset() {
            this.successfulCalls = this.failedCalls = this.cacheHits = 0;
        }

        public String toString() {
            return new StringBuilder().append( "TotalCalls: " ).append( successfulCalls + failedCalls + cacheHits ).append( " CacheHits: " ).append( cacheHits ).append( " successfulCalls: " ).append( successfulCalls ).append( " FailedCalls: " ).append( failedCalls ).toString();
        }
    }

    private static class CompositeEnumeration<URL>
        implements
        Enumeration<URL> {
        private List<URL>     list;
        private Iterator<URL> it;

        public void addEnumeration(Enumeration<URL> enumeration) {
            if ( !enumeration.hasMoreElements() ) {
                // don't add it, if it's empty
                return;
            }

            if ( this.it != null ) {
                throw new IllegalStateException( "cannot add more enumerations while iterator" );
            }

            if ( this.list == null ) {
                this.list = new ArrayList<URL>();
            }

            while ( enumeration.hasMoreElements() ) {
                this.list.add( enumeration.nextElement() );
            }
        }

        public int size() {
            if ( this.list == null ) {
                return 0;
            } else {
                return this.list.size();
            }
        }

        public boolean hasMoreElements() {
            if ( this.it == null ) {
                if ( this.list == null ) {
                    return false;
                } else {
                    this.it = this.list.iterator();
                }
            }
            return it.hasNext();
        }

        public URL nextElement() {
            if ( this.it == null ) {
                if ( this.list == null ) {
                    throw new NoSuchElementException();
                } else {
                    this.it = this.list.iterator();
                }
            }
            return it.next();
        }
    }
}
