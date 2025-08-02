package com.aditya.bro.ipfs;

import io.ipfs.api.IPFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpfsConfig {

    @Value("${ipfs.api.host}")
    private String ipfsHost;

    @Value("${ipfs.api.port}")
    private int ipfsPort;

    @Bean
    public IPFS ipfs() {
        return new IPFS("/ip4/" + ipfsHost + "/tcp/" + ipfsPort);
    }
}