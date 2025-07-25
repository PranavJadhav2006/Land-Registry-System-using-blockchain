
package com.aditya.bro.ipfs;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class IpfsService {

    private final IPFS ipfs;

    public IpfsService(@Value("${ipfs.api.host}") String host, @Value("${ipfs.api.port}") int port) {
        this.ipfs = new IPFS(host, port);
    }

    public String addFile(MultipartFile file) throws IOException {
        NamedStreamable.ByteArrayWrapper stream = new NamedStreamable.ByteArrayWrapper(file.getOriginalFilename(), file.getBytes());
        MerkleNode result = ipfs.add(stream).get(0);
        return result.hash.toString();
    }

    public byte[] getFile(String hash) throws IOException {
        Multihash filePointer = Multihash.fromBase58(hash);
        return ipfs.cat(filePointer);
    }
}
