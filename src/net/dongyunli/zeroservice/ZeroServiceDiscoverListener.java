/**
 * CopyRight (C), 2013, www.winchannel.net
 */

package net.dongyunli.zeroservice;

public interface ZeroServiceDiscoverListener {
    public void onDiscoveryStarted(String type);

    public void onServiceFound(ZeroServiceInfo zsi);

    public void onServiceLost(ZeroServiceInfo zsi);

    public void onDiscoverStopped(String type);

    public void onStartDiscoveryFailed(String type, String error);

    public void onStopDiscoveryFailed(String type, String error);
}
