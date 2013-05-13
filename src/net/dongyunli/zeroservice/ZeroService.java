/**
 * CopyRight (C), 2013
 */

package net.dongyunli.zeroservice;

import android.content.Context;

import java.net.InetAddress;

public abstract class ZeroService {
    /**
     * get the network service instance. <br>
     * The suitable service instance. After API Level 15, Android supports the
     * NSD in SDK, so use it instead of the JmnDNS solution.
     * 
     * @return The NsService instance.
     */
    public static ZeroService create(Context conext, boolean host, boolean wifiEnable, InetAddress addr, String name) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return new JmDNSService(conext, host, wifiEnable, addr, name);
        } else {
            return new NsdService(conext, addr);
        }
    }

    /**
     * add a listener to listen the service registration status
     * 
     * @param listener
     */
    public abstract void addRegistrationListener(ZeroRegistrationListener listener);

    /**
     * add a listener to listen the discovery result
     * 
     * @param listener
     */
    public abstract void addServiceDiscoverListener(ZeroServiceDiscoverListener listener);

    /**
     * create a service and broadcast in the local network
     * 
     * @param name the service name
     * @param type the service type
     * @param port the local server socket port
     */
    public abstract void createService(String name, String type, int port);

    /**
     * stop the local broadcast service
     * 
     * @param si the service info
     */
    public abstract void destroyService(ZeroServiceInfo si);

    /**
     * listen the specified type of service
     * 
     * @param type the service type
     */
    public abstract void listenService(String type);

    /**
     * stop listen the local service
     * 
     * @param type
     */
    public abstract void unListenService(String type);
}
