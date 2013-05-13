/**
 * CopyRight (C), 2013, www.winchannel.net
 */

package net.dongyunli.zeroservice;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdService extends ZeroService {
    private static final String TAG = NsdService.class.getSimpleName();
    private ZeroRegistrationListener mServiceListener;
    private ZeroServiceDiscoverListener mDiscoverListener;
    private NsdManager mNsdManager;
    private String mName;
    private String mType;
    private int mPort;
    private InetAddress mAddress;

    public NsdService(Context conext, InetAddress addr) {
        mAddress = addr;
        mNsdManager = (NsdManager) conext.getSystemService(Context.NSD_SERVICE);
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
        mName = name;
        mType = type;
        mPort = port;
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType(type);
        serviceInfo.setPort(mPort);
        if (null != mAddress) {
            serviceInfo.setHost(mAddress);
        }

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegisterListener);
    }

    @Override
    public void destroyService(ZeroServiceInfo si) {
        mNsdManager.unregisterService(mRegisterListener);
    }

    @Override
    public void listenService(String type) {
        mNsdManager.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, mNsdDiscoverListener);
    }

    @Override
    public void unListenService(String type) {
        mNsdManager.stopServiceDiscovery(mNsdDiscoverListener);
    }

    private NsdManager.RegistrationListener mRegisterListener = new NsdManager.RegistrationListener() {

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            if (null != mServiceListener) {
                Log.d(TAG, "failed register NSD service: " + errorCode);
                ZeroServiceInfo zsi = new ZeroServiceInfo(serviceInfo, null);
                mServiceListener.onServiceRegisteredFailed(zsi);
            }
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            if (null != mServiceListener) {
                Log.d(TAG, "failed unregister NSD service: " + errorCode);
                ZeroServiceInfo zsi = new ZeroServiceInfo(serviceInfo, null);
                mServiceListener.onServiceUnregisteredFailed(zsi);
            }
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            if (null != mServiceListener) {
                Log.d(TAG, "success register NSD service: " + serviceInfo);
                mName = serviceInfo.getServiceName();
                ZeroServiceInfo zsi = new ZeroServiceInfo(serviceInfo, null);
                mServiceListener.onServiceRegistered(zsi);
            }
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            if (null != mServiceListener) {
                Log.d(TAG, "success unregister NSD service: ");
                ZeroServiceInfo zsi = new ZeroServiceInfo(serviceInfo, null);
                mServiceListener.onServiceUnregistered(zsi);
            }
        }
    };

    private NsdManager.DiscoveryListener mNsdDiscoverListener = new NsdManager.DiscoveryListener() {

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "start discovery failed:" + errorCode);
                mDiscoverListener.onStartDiscoveryFailed(serviceType, String.valueOf(errorCode));
            }
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "stop discovery failed:" + errorCode);
                mDiscoverListener.onStopDiscoveryFailed(serviceType, String.valueOf(errorCode));
            }
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "start discover the \"" + "\"" + serviceType + " services");
                mDiscoverListener.onDiscoveryStarted(serviceType);
            }
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "stop discover the \"" + "\"" + serviceType + " services");
                mDiscoverListener.onDiscoverStopped(serviceType);
            }
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "service found in NSD:" + serviceInfo);
                if (!serviceInfo.getServiceType().equals(mType)) {
                    Log.d(TAG, "type mismatch, ignore this info");
                } else if (serviceInfo.getServiceName().equals(mName)) {
                    Log.d(TAG, "service is me, igonore it");
                } else {
                    mNsdManager.resolveService(serviceInfo, mResolverListener);
                }
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "service lost in NSD:" + serviceInfo);
                if (!serviceInfo.getServiceType().equals(mType)) {
                    Log.d(TAG, "type mismatch, ignore this info");
                } else if (serviceInfo.getServiceName().equals(mName)) {
                    Log.d(TAG, "service is me, me lost???");
                } else {
                    ZeroServiceInfo zsi = new ZeroServiceInfo(serviceInfo, null);
                    mDiscoverListener.onServiceLost(zsi);
                }
            }
        }
    };

    private NsdManager.ResolveListener mResolverListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(TAG, "service resolved failed in NSD:" + serviceInfo + " error:" + errorCode);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            if (null != mDiscoverListener) {
                Log.d(TAG, "service resolved in NSD:" + serviceInfo);
                ZeroServiceInfo zsi = new ZeroServiceInfo(serviceInfo, null);
                mDiscoverListener.onServiceFound(zsi);
            }
        }
    };
}
