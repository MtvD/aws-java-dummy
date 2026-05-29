package com.filestorage.service.impl;

import com.filestorage.model.AccessLevel;
import com.filestorage.model.FileEntity;
import com.filestorage.service.AccessValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Service
public class NetworkAccessValidator implements AccessValidator {

    private final List<String> internalIpRanges;

    public NetworkAccessValidator(@Value("${network.internal-ip-ranges}") List<String> internalIpRanges) {
        this.internalIpRanges = internalIpRanges;
    }

    @Override
    public boolean isAccessAllowed(FileEntity file, String clientIp) {
        if (file.getAccessLevel() == AccessLevel.PUBLIC) {
            return true;
        }
        return isInternalIp(clientIp);
    }

    public boolean isInternalIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(clientIp);
            byte[] ipBytes = address.getAddress();

            for (String cidr : internalIpRanges) {
                if (isIpInCidr(ipBytes, cidr)) {
                    return true;
                }
            }
        } catch (UnknownHostException e) {
            return false;
        }
        return false;
    }

    private boolean isIpInCidr(byte[] ipBytes, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress networkAddress = InetAddress.getByName(parts[0]);
            byte[] networkBytes = networkAddress.getAddress();
            int prefixLength = Integer.parseInt(parts[1]);

            if (ipBytes.length != networkBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (ipBytes[i] != networkBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0 && fullBytes < ipBytes.length) {
                int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                if ((ipBytes[fullBytes] & mask) != (networkBytes[fullBytes] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }
}
