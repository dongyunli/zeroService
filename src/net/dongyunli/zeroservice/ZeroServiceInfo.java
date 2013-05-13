/**
 * CopyRight (C), 2013
 */

package net.dongyunli.zeroservice;

import android.net.nsd.NsdServiceInfo;

import java.net.InetAddress;

import javax.jmdns.ServiceEvent;

public class ZeroServiceInfo {
    private static final int LOCAL_SERVER_PORT = 56789;
    private NsdServiceInfo mServiceInfo;
    private ServiceEvent mServiceEvent;

    public ZeroServiceInfo(NsdServiceInfo ni, ServiceEvent se) {
        mServiceInfo = ni;
        mServiceEvent = se;
    }

    public String getName() {
        String name = null;
        if (null != mServiceInfo) {
            name = mServiceInfo.getServiceName();
        } else if (null != mServiceEvent) {
            name = mServiceEvent.getName();
        }
        return name;
    }

    public String getType() {
        String type = null;
        if (null != mServiceInfo) {
            type = mServiceInfo.getServiceType();
        } else if (null != mServiceEvent) {
            type = mServiceEvent.getType();
        }
        return type;
    }

    public String getHost() {
        String host = null;
        if (null != mServiceInfo) {
            InetAddress addr = mServiceInfo.getHost();
            if (null != addr) {
                host = addr.getHostAddress();
            }
        } else if (null != mServiceEvent) {
            String[] addr = mServiceEvent.getInfo().getHostAddresses();
            if (null != addr) {
                host = addr[0];
            }
        }
        return host;
    }

    public int getPort() {
        int port = LOCAL_SERVER_PORT;
        if (null != mServiceInfo) {
            port = mServiceInfo.getPort();
        } else if (null != mServiceEvent) {
            port = mServiceEvent.getInfo().getPort();
        }
        return port;
    }
}
