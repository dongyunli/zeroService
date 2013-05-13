/**
 * CopyRight (C), 2013
 */

package net.dongyunli.zeroservice;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;

import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class JmDNSService extends ZeroService {
    private static final String TAG = JmDNSService.class.getSimpleName();
    private static final String TYPE_PADDING = "local.";
    private static final int LOCAL_SERVER_WEIGHT = 1;
    private static final int LOCAL_SERVER_PROIORITY = 1;
    private ZeroRegistrationListener mServiceListener;
    private ZeroServiceDiscoverListener mDiscoverListener;
    private JmDNS mJmDNS;
    private ZeroServiceInfo mZsinfo;
    private String mName;
    private String mType;
    private boolean mHost;
    private boolean mWifiEnable;
    private InetAddress mAddress;

    // WIFI related
    private WifiManager mWifiManager;
    private MulticastLock mWifiMulticastLock = null;

    public JmDNSService(Context conext, boolean host, boolean wifiEnable, InetAddress addr, String name) {
        if (null != mJmDNS) {
            destroyService(mZsinfo);
        }
        mHost = host;
        mWifiEnable = wifiEnable;
        mName = name;
        mAddress = addr;
        mWifiManager = (WifiManager) conext.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void addRegistrationListener(ZeroRegistrationListener listener) {
        mServiceListener = listener;
    }

    @Override
    public void addServiceDiscoverListener(ZeroServiceDiscoverListener listener) {
        mDiscoverListener = listener;
    }

    @Override
    public void createService(String name, String type, int port) {
        if (!type.contains(TYPE_PADDING)) {
            type = type + TYPE_PADDING;
        }
        mType = type;

        acquireMulticastLock();
        try {
            if (null != mJmDNS) {
                destroyService(null, false);
            }
            if (mHost) {
                mJmDNS = JmDNS.create(mWifiEnable);
            } else {
                if (null != mAddress) {
                    mJmDNS = JmDNS.create(mAddress, name);
                } else {
                    mJmDNS = JmDNS.create(mWifiEnable);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to create the service", e);
            if (null != mServiceListener) {
                mServiceListener.onServiceRegisteredFailed(new ZeroServiceInfo(null, null));
            }
        }
        if (null != mJmDNS) {
            try {
                ServiceInfo si = ServiceInfo.create(mType, name, port, LOCAL_SERVER_WEIGHT, LOCAL_SERVER_PROIORITY, true,
                        "android_jmdns_service");
                mJmDNS.registerService(si);
            } catch (Exception e) {
                Log.e(TAG, "failed to cast the service", e);
            }
        }
    }

    @Override
    public void destroyService(ZeroServiceInfo zsi) {
        destroyService(zsi, true);
    }

    @Override
    public void listenService(String type) {
        listenService(type, true);
    }

    @Override
    public void unListenService(String type) {
        unListenService(type, true);
    }

    private ServiceListener mJmDNSListener = new ServiceListener() {

        @Override
        public void serviceAdded(ServiceEvent event) {
            Log.d(TAG, "serviceAdded:" + event);
            ZeroServiceInfo zsi = new ZeroServiceInfo(null, event);
            String name = event.getName();
            if (mName.equals(name)) {
                mZsinfo = zsi;
                if (null != mServiceListener) {
                    mServiceListener.onServiceRegistered(zsi);
                }
            } else {
                ServiceInfo sinfo = event.getInfo();
                if (null != sinfo) {
                    InetAddress[] addrs = sinfo.getInetAddresses();
                    if (null != addrs && addrs.length > 0) {
                        for (InetAddress addr : addrs) {
                            Log.d(TAG, "the addr is:" + addr.getHostName());
                        }
                        if (null != mDiscoverListener) {
                            mDiscoverListener.onServiceFound(zsi);
                        }
                    }
                }
            }
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            Log.d(TAG, "serviceRemoved:" + event);
            ZeroServiceInfo zsi = new ZeroServiceInfo(null, event);
            String name = event.getName();
            if (mName.equals(name)) {
                if (null != mServiceListener) {
                    mServiceListener.onServiceUnregistered(zsi);
                }
            } else {
                if (null != mDiscoverListener) {
                    mDiscoverListener.onServiceLost(zsi);
                }
            }
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            Log.d(TAG, "serviceResolved:" + event);
            ZeroServiceInfo zsi = new ZeroServiceInfo(null, event);
            String name = event.getName();
            if (mName.equals(name)) {
                if (null != mServiceListener) {
                    mServiceListener.onServiceRegistered(zsi);
                }
            } else {
                ServiceInfo sinfo = event.getInfo();
                if (null != sinfo) {
                    InetAddress[] addrs = sinfo.getInetAddresses();
                    if (null != addrs && addrs.length > 0) {
                        for (InetAddress addr : addrs) {
                            Log.d(TAG, "the addr is:" + addr.getHostName());
                        }
                        if (null != mDiscoverListener) {
                            mDiscoverListener.onServiceFound(zsi);
                        }
                    }
                }
            }
        }

    };

    private void destroyService(ZeroServiceInfo zsi, boolean notify) {
        if (null == mJmDNS) {
            if (notify) {
                mServiceListener.onServiceUnregistered(zsi);
            }
            return;
        }
        try {
            releaseMulticastLock();
            mJmDNS.removeServiceListener(mType, mJmDNSListener);
            mJmDNS.unregisterAllServices();
            mJmDNS.close();
            mJmDNS = null;
            if (null != mServiceListener && notify) {
                mServiceListener.onServiceUnregistered(zsi);
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to close the JmDNS service");
            if (null != mServiceListener && notify) {
                mServiceListener.onServiceUnregisteredFailed(zsi);
            }
        }
    }

    private void listenService(String type, boolean notify) {
        if (null != mDiscoverListener && notify) {
            mDiscoverListener.onDiscoveryStarted(type);
        }
        if (null == mJmDNS) {
            if (null != mDiscoverListener && notify) {
                mDiscoverListener.onStartDiscoveryFailed(type, "empty service.");
            }
            return;
        }
        try {
            mJmDNS.addServiceListener(mType, mJmDNSListener);
        } catch (Exception e) {
            Log.e(TAG, "failed to listen the service", e);
            if (null != mDiscoverListener) {
                mDiscoverListener.onStartDiscoveryFailed(type, e.getMessage());
            }
        }
    }

    private void unListenService(String type, boolean notify) {
        if (null != mDiscoverListener && notify) {
            mDiscoverListener.onDiscoverStopped(type);
        }
        if (null != mJmDNS) {
            mJmDNS.removeServiceListener(mType, mJmDNSListener);
        }
    }

    private void acquireMulticastLock() {
        if (null != mWifiMulticastLock && mWifiMulticastLock.isHeld()) {
            mWifiMulticastLock.release();
        }

        mWifiMulticastLock = mWifiManager.createMulticastLock(TAG);
        mWifiMulticastLock.setReferenceCounted(true);
        mWifiMulticastLock.acquire();
    }

    private void releaseMulticastLock() {
        if (null != mWifiMulticastLock && mWifiMulticastLock.isHeld()) {
            mWifiMulticastLock.release();
            mWifiMulticastLock = null;
        }
    }
}
